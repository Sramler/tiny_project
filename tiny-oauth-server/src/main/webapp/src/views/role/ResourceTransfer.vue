<template>
  <!-- 用 a-modal 包裹穿梭组件 -->
  <a-modal v-model:open="visible" :title="title" @ok="handleOk" @cancel="handleCancel" width="900px">
    <div style="min-height: 400px;">
      <!-- 使用 a-transfer 组件实现树形穿梭 -->
      <a-transfer
        v-model:target-keys="rightKeys"
        class="tree-transfer"
        :data-source="transferDataSource"
        :render="(item: any) => item.title"
        :show-select-all="false"
        :disabled="loading"
        :titles="['可选资源', '已分配资源']"
        style="height: 400px;"
      >
        <template #children="{ direction, selectedKeys, onItemSelect }">
          <!-- 左侧树形结构 -->
          <a-tree
            v-if="direction === 'left'"
            block-node
            checkable
            check-strictly
            default-expand-all
            :checked-keys="[...selectedKeys, ...rightKeys]"
            :tree-data="leftTreeData"
            :field-names="{ title: 'title', key: 'key', children: 'children' }"
            @check="(checkedKeys: any, info: any) => handleTreeCheck(info, [...selectedKeys, ...rightKeys], onItemSelect)"
            @select="(selectedKeys: any, info: any) => handleTreeSelect(info, [...selectedKeys, ...rightKeys], onItemSelect)"
          />
          <!-- 右侧树形结构 -->
          <a-tree
            v-if="direction === 'right'"
            block-node
            checkable
            check-strictly
            default-expand-all
            :checked-keys="selectedKeys"
            :tree-data="rightTreeData"
            :field-names="{ title: 'title', key: 'key', children: 'children' }"
            @check="(checkedKeys: any, info: any) => handleTreeCheck(info, selectedKeys, onItemSelect)"
            @select="(selectedKeys: any, info: any) => handleTreeSelect(info, selectedKeys, onItemSelect)"
          />
        </template>
      </a-transfer>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
// 引入Vue API
import { ref, computed, watch, defineProps, defineEmits, onMounted } from 'vue';
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

// 原始树形数据（从后端加载）
const originalTreeData = ref<any[]>([]);

// 已分配资源key
const rightKeys = ref<number[]>([]);

// 扁平化的transfer数据源
const transferDataSource = ref<any[]>([]);

// 加载资源树数据
async function loadResourceTree() {
  loading.value = true
  try {
    const tree = await getResourceTree()
    // 转换数据格式，确保key为number类型
    originalTreeData.value = transformTreeData(tree)
    // 扁平化数据用于transfer组件
    flattenTreeData(originalTreeData.value)
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

// 扁平化树形数据为transfer组件需要的格式
function flattenTreeData(list: any[] = []) {
  transferDataSource.value = [];
  list.forEach(item => {
    transferDataSource.value.push({
      key: item.key,
      title: item.title,
      disabled: false
    });
    if (item.children) {
      flattenTreeData(item.children);
    }
  });
}

// 检查节点是否被选中
function isChecked(selectedKeys: (string | number)[], eventKey: string | number) {
  return selectedKeys.indexOf(eventKey) !== -1;
}

// 处理树形数据，为已分配的节点设置disabled状态
function handleTreeData(treeNodes: any[], targetKeys: (string | number)[] = []): any[] {
  return treeNodes.map(({ children, ...props }) => ({
    ...props,
    disabled: targetKeys.includes(props.key as string | number),
    children: handleTreeData(children ?? [], targetKeys),
  }));
}

// 递归过滤树，仅保留key在rightKeys中的节点（已分配的资源）
function filterAssignedTree(nodes: any[], assignedKeys: (string | number)[]): any[] {
  return nodes
    .map(node => {
      if (assignedKeys.includes(node.key)) {
        const children = node.children ? filterAssignedTree(node.children, assignedKeys) : [];
        return { ...node, children: children.length > 0 ? children : undefined };
      } else if (node.children) {
        const children = filterAssignedTree(node.children, assignedKeys);
        if (children.length > 0) {
          return { ...node, children };
        }
      }
      return null;
    })
    .filter(Boolean);
}

// 递归过滤树，仅保留key不在rightKeys中的节点（未分配的资源）
function filterUnassignedTree(nodes: any[], assignedKeys: (string | number)[]): any[] {
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
const leftTreeData = computed(() => filterUnassignedTree(originalTreeData.value, rightKeys.value));

// 右侧树数据 (只显示已分配的资源)
const rightTreeData = computed(() => filterAssignedTree(originalTreeData.value, rightKeys.value));

// 处理树节点选中事件
const handleTreeCheck = (info: any, checkedKeys: (string | number)[], onItemSelect: (n: any, c: boolean) => void) => {
  const { eventKey } = info.node;
  if (eventKey !== undefined) {
    onItemSelect(eventKey, !isChecked(checkedKeys, eventKey));
  }
};

// 处理树节点选择事件
const handleTreeSelect = (info: any, selectedKeys: (string | number)[], onItemSelect: (n: any, c: boolean) => void) => {
  const { eventKey } = info.node;
  if (eventKey !== undefined) {
    onItemSelect(eventKey, !isChecked(selectedKeys, eventKey));
  }
};

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
/* 自定义transfer组件样式 */
.tree-transfer .ant-transfer-list:first-child {
  width: 50%;
  flex: none;
}

.tree-transfer .ant-transfer-list {
  height: 400px;
}

.tree-transfer .ant-transfer-list-body {
  height: 350px;
}

/* 树组件样式优化 */
:deep(.ant-tree) {
  background: transparent;
}

:deep(.ant-tree-node-content-wrapper) {
  padding: 4px 8px;
  border-radius: 4px;
}

:deep(.ant-tree-node-content-wrapper:hover) {
  background-color: #f5f5f5;
}

:deep(.ant-tree-checkbox) {
  margin-right: 8px;
}

/* 禁用状态的节点样式 */
:deep(.ant-tree-treenode-disabled .ant-tree-node-content-wrapper) {
  color: #bfbfbf;
  cursor: not-allowed;
}

:deep(.ant-tree-treenode-disabled .ant-tree-checkbox) {
  cursor: not-allowed;
}
</style> 