<template>
  <!-- 用 a-modal 包裹树-树穿梭内容 -->
  <a-modal v-model:open="visible" :title="title" @ok="handleOk" @cancel="handleCancel" width="800px">
    <div style="min-height: 400px;">
      <!-- 树-树穿梭内容 -->
      <div style="display: flex; gap: 32px;">
        <!-- 左侧资源树 -->
        <div style="flex: 1;">
          <div style="font-weight: bold; margin-bottom: 8px;">可选资源</div>
          <a-tree
            :tree-data="leftTree"
            checkable
            :checked-keys="leftCheckedKeys"
            @check="onLeftCheck"
            :defaultExpandAll="true"
            :field-names="{ title: 'title', key: 'key', children: 'children' }"
            :loading="loading"
          />
        </div>
        <!-- 穿梭按钮 -->
        <div style="display: flex; flex-direction: column; justify-content: center; gap: 8px;">
          <a-button @click="moveToRight" :disabled="leftCheckedKeys.length === 0">
            <Icon icon="RightOutlined" />
          </a-button>
          <a-button @click="moveToLeft" :disabled="rightCheckedKeys.length === 0">
            <Icon icon="LeftOutlined" />
          </a-button>
        </div>
        <!-- 右侧已选资源树 -->
        <div style="flex: 1;">
          <div style="font-weight: bold; margin-bottom: 8px;">已分配资源</div>
          <a-tree
            :tree-data="rightTree"
            checkable
            :checked-keys="rightCheckedKeys"
            @check="onRightCheck"
            :defaultExpandAll="true"
            :field-names="{ title: 'title', key: 'key', children: 'children' }"
            :loading="loading"
          />
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
// 引入Vue API
import { ref, computed, watch, defineProps, defineEmits, onMounted } from 'vue';
import Icon from '@/components/Icon.vue' // 引入自定义图标组件
import { getResourceTree } from '@/api/resource' // 引入资源API
import { getRoleResources } from '@/api/role' // 引入角色API

// props 和 emits
const props = defineProps({
  open: Boolean,
  title: { type: String, default: '配置资源' },
  roleId: { type: Number, required: true } // 角色ID
})
const emit = defineEmits(['update:open', 'submit'])

// 控制弹窗显示
const visible = ref(props.open)
watch(() => props.open, v => visible.value = v)
watch(visible, v => emit('update:open', v))

// 加载状态
const loading = ref(false)

// 资源树数据（从后端加载）
const resourceTree = ref<any[]>([]);

// 左侧树选中key
const leftCheckedKeys = ref<number[]>([]);
// 右侧树选中key
const rightCheckedKeys = ref<number[]>([]);
// 已分配资源key
const rightKeys = ref<number[]>([]);

// 加载资源树数据
async function loadResourceTree() {
  loading.value = true
  try {
    const tree = await getResourceTree()
    // 转换数据格式，确保key为number类型
    resourceTree.value = transformTreeData(tree)
  } catch (error) {
    console.error('加载资源树失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载角色已分配资源
async function loadRoleResources() {
  if (!props.roleId) return
  try {
    const resourceIds = await getRoleResources(props.roleId)
    rightKeys.value = resourceIds || []
  } catch (error) {
    console.error('加载角色资源失败:', error)
    rightKeys.value = []
  }
}

// 转换树数据格式
function transformTreeData(nodes: any[]): any[] {
  return nodes.map(node => ({
    key: Number(node.id), // 确保key为number类型
    title: node.title || node.name, // 优先使用title，备选使用name
    children: node.children ? transformTreeData(node.children) : undefined
  }))
}

// 递归查找所有子节点key
function getAllKeys(nodes: any[]): number[] {
  let keys: number[] = [];
  for (const node of nodes) {
    keys.push(node.key);
    if (node.children) {
      keys = keys.concat(getAllKeys(node.children));
    }
  }
  return keys;
}

// 递归过滤树，仅保留key在rightKeys中的节点
function filterTree(nodes: any[], keys: number[]): any[] {
  return nodes
    .map(node => {
      if (keys.includes(node.key)) {
        const children = node.children ? filterTree(node.children, keys) : [];
        return { ...node, children: children.length > 0 ? children : undefined };
      } else if (node.children) {
        const children = filterTree(node.children, keys);
        if (children.length > 0) {
          return { ...node, children };
        }
      }
      return null;
    })
    .filter(Boolean);
}

// 递归过滤树，仅保留key不在rightKeys中的节点（未分配的资源）
function filterUnassignedTree(nodes: any[], assignedKeys: number[]): any[] {
  return nodes
    .map(node => {
      if (!assignedKeys.includes(node.key)) {
        const children = node.children ? filterUnassignedTree(node.children, assignedKeys) : [];
        return { ...node, children: children.length > 0 ? children : undefined };
      } else if (node.children) {
        const children = filterUnassignedTree(node.children, assignedKeys);
        if (children.length > 0) {
          return { ...node, children };
        }
      }
      return null;
    })
    .filter(Boolean);
}

// 左侧树数据 (只显示未分配的资源)
const leftTree = computed(() => filterUnassignedTree(resourceTree.value, rightKeys.value));

// 右侧树数据 (只显示已分配的资源)
const rightTree = computed(() => filterTree(resourceTree.value, rightKeys.value));

// 左侧树选中
function onLeftCheck(checked: any) {
  leftCheckedKeys.value = checked.checked || checked;
}

// 右侧树选中
function onRightCheck(checked: any) {
  rightCheckedKeys.value = checked.checked || checked;
}

// 穿梭到右侧
function moveToRight() {
  // 选中节点及其所有子节点都加入rightKeys
  const addKeys = new Set(rightKeys.value);
  function addAll(keys: number[], nodes: any[]) {
    for (const node of nodes) {
      if (keys.includes(node.key)) {
        addKeys.add(node.key);
        if (node.children) addAll(getAllKeys(node.children), node.children);
      } else if (node.children) {
        addAll(keys, node.children);
      }
    }
  }
  addAll(leftCheckedKeys.value, resourceTree.value);
  rightKeys.value = Array.from(addKeys);
  leftCheckedKeys.value = [];
}

// 从右侧移除
function moveToLeft() {
  // 移除选中的key及其所有子节点
  const removeSet = new Set<number>();
  function collectRemoveKeys(keys: number[], nodes: any[]) {
    for (const node of nodes) {
      if (keys.includes(node.key)) {
        removeSet.add(node.key);
        if (node.children) collectRemoveKeys(getAllKeys(node.children), node.children);
      } else if (node.children) {
        collectRemoveKeys(keys, node.children);
      }
    }
  }
  collectRemoveKeys(rightCheckedKeys.value, rightTree.value);
  rightKeys.value = rightKeys.value.filter(key => !removeSet.has(key));
  rightCheckedKeys.value = [];
}

// 点击确定
function handleOk() {
  emit('submit', rightKeys.value) // rightKeys.value 是已分配资源key数组
  visible.value = false
}

// 点击取消
function handleCancel() {
  visible.value = false
}

// 监听弹窗打开，加载数据
watch(() => props.open, async (newVal) => {
  if (newVal) {
    // 弹窗打开时加载数据
    await loadResourceTree()
    await loadRoleResources()
  }
})

// 组件挂载时也加载一次数据
onMounted(async () => {
  if (props.open) {
    await loadResourceTree()
    await loadRoleResources()
  }
})
</script>

<style scoped>
</style> 