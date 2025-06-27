<template>
  <div class="menu-form">
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
      layout="horizontal"
    >
      <!-- 基本信息 -->
      <a-divider>基本信息</a-divider>
      
      <a-form-item label="菜单名称" name="name">
        <a-input 
          v-model:value="formData.name" 
          placeholder="请输入菜单名称"
          maxlength="100"
          show-count
        />
      </a-form-item>
      
      <a-form-item label="菜单标题" name="title">
        <a-input 
          v-model:value="formData.title" 
          placeholder="请输入菜单显示标题"
          maxlength="100"
          show-count
        />
      </a-form-item>
      
      <a-form-item label="父级菜单" name="parentId">
        <a-tree-select
          v-model:value="formData.parentId"
          :tree-data="menuTreeData"
          placeholder="请选择父级菜单"
          allow-clear
          tree-default-expand-all
          :field-names="{ children: 'children', label: 'title', value: 'id' }"
        />
      </a-form-item>
      
      <a-form-item label="排序权重" name="sort">
        <a-input-number 
          v-model:value="formData.sort" 
          :min="0" 
          :max="9999"
          placeholder="数字越小越靠前"
          style="width: 100%"
        />
      </a-form-item>
      
      <!-- 路由配置 -->
      <a-divider>路由配置</a-divider>
      
      <a-form-item label="前端路径" name="path">
        <a-input 
          v-model:value="formData.path" 
          placeholder="请输入前端路由路径，如：/user"
          maxlength="200"
          show-count
        />
      </a-form-item>
      
      <a-form-item label="组件路径" name="component">
        <a-input 
          v-model:value="formData.component" 
          placeholder="请输入Vue组件路径，如：@/views/user/User.vue"
          maxlength="200"
          show-count
        />
      </a-form-item>
      
      <a-form-item label="重定向地址" name="redirect">
        <a-input 
          v-model:value="formData.redirect" 
          placeholder="父菜单重定向地址，如：/user/list"
          maxlength="200"
          show-count
        />
      </a-form-item>
      
      <a-form-item label="权限标识" name="permission">
        <a-input 
          v-model:value="formData.permission" 
          placeholder="请输入权限标识，如：user:list"
          maxlength="100"
          show-count
        />
      </a-form-item>
      
      <!-- 显示配置 -->
      <a-divider>显示配置</a-divider>
      
      <a-form-item label="菜单图标" name="icon">
        <div class="icon-selector">
          <a-input 
            v-model:value="formData.icon" 
            placeholder="请输入图标名称"
            maxlength="200"
            style="flex: 1; margin-right: 8px;"
          />
          <a-button @click="showIconSelector = true" type="primary">
            选择图标
          </a-button>
        </div>
        <div v-if="formData.icon" class="icon-preview">
          <component :is="getIconComponent(formData.icon)" class="preview-icon" />
          <span>{{ formData.icon }}</span>
        </div>
      </a-form-item>
      
      <a-form-item label="显示图标" name="showIcon">
        <a-switch v-model:checked="formData.showIcon" />
      </a-form-item>
      
      <a-form-item label="侧边栏隐藏" name="hidden">
        <a-switch v-model:checked="formData.hidden" />
      </a-form-item>
      
      <a-form-item label="页面缓存" name="keepAlive">
        <a-switch v-model:checked="formData.keepAlive" />
      </a-form-item>
    </a-form>
    
    <!-- 图标选择器弹窗 -->
    <a-modal
      v-model:open="showIconSelector"
      title="选择图标"
      width="800px"
      :footer="null"
    >
      <div class="icon-grid">
        <div
          v-for="icon in iconList"
          :key="icon.name"
          class="icon-item"
          :class="{ active: formData.icon === icon.name }"
          @click="selectIcon(icon.name)"
        >
          <component :is="icon.component" class="grid-icon" />
          <span class="icon-name">{{ icon.name }}</span>
        </div>
      </div>
    </a-modal>
    
    <!-- 表单操作按钮 -->
    <div class="form-actions">
      <a-button @click="handleCancel">取消</a-button>
      <a-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ mode === 'create' ? '创建' : '更新' }}
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
// 引入图标
import {
  HomeOutlined,
  UserOutlined,
  SettingOutlined,
  DashboardOutlined,
  TeamOutlined,
  FileOutlined,
  FolderOutlined,
  AppstoreOutlined,
  MenuOutlined,
  ApiOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
// 引入菜单API
import { getMenuTree, type MenuItem } from '@/api/menu'

// 定义组件属性
interface Props {
  mode: 'create' | 'edit'
  menuData?: MenuItem | null
  parentMenu?: MenuItem | null
}

const props = withDefaults(defineProps<Props>(), {
  menuData: null,
  parentMenu: null
})

// 定义事件
const emit = defineEmits<{
  submit: [data: any]
  cancel: []
}>()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  title: '',
  path: '',
  icon: '',
  showIcon: true,
  sort: 0,
  component: '',
  redirect: '',
  hidden: false,
  keepAlive: false,
  permission: '',
  parentId: null as number | null
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入菜单标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序权重', trigger: 'blur' },
    { type: 'number', min: 0, max: 9999, message: '排序权重必须在 0-9999 之间', trigger: 'blur' }
  ]
}

// 菜单树数据
const menuTreeData = ref<MenuItem[]>([])

// 图标选择器
const showIconSelector = ref(false)

// 提交状态
const submitting = ref(false)

// 图标列表
const iconList = [
  { name: 'HomeOutlined', component: HomeOutlined },
  { name: 'UserOutlined', component: UserOutlined },
  { name: 'SettingOutlined', component: SettingOutlined },
  { name: 'DashboardOutlined', component: DashboardOutlined },
  { name: 'TeamOutlined', component: TeamOutlined },
  { name: 'FileOutlined', component: FileOutlined },
  { name: 'FolderOutlined', component: FolderOutlined },
  { name: 'AppstoreOutlined', component: AppstoreOutlined },
  { name: 'MenuOutlined', component: MenuOutlined },
  { name: 'ApiOutlined', component: ApiOutlined },
  { name: 'PlusOutlined', component: PlusOutlined },
  { name: 'EditOutlined', component: EditOutlined },
  { name: 'DeleteOutlined', component: DeleteOutlined },
  { name: 'SearchOutlined', component: SearchOutlined },
  { name: 'ReloadOutlined', component: ReloadOutlined }
]

// 图标组件映射
const iconMap: Record<string, any> = {
  'HomeOutlined': HomeOutlined,
  'UserOutlined': UserOutlined,
  'SettingOutlined': SettingOutlined,
  'DashboardOutlined': DashboardOutlined,
  'TeamOutlined': TeamOutlined,
  'FileOutlined': FileOutlined,
  'FolderOutlined': FolderOutlined,
  'AppstoreOutlined': AppstoreOutlined,
  'MenuOutlined': MenuOutlined,
  'ApiOutlined': ApiOutlined,
  'PlusOutlined': PlusOutlined,
  'EditOutlined': EditOutlined,
  'DeleteOutlined': DeleteOutlined,
  'SearchOutlined': SearchOutlined,
  'ReloadOutlined': ReloadOutlined
}

// 获取图标组件
function getIconComponent(iconName: string) {
  return iconMap[iconName] || MenuOutlined
}

// 选择图标
function selectIcon(iconName: string) {
  formData.icon = iconName
  showIconSelector.value = false
}

// 加载菜单树数据
async function loadMenuTree() {
  try {
    const data = await getMenuTree()
    menuTreeData.value = data || []
  } catch (error) {
    console.error('加载菜单树失败:', error)
    message.error('加载菜单树失败')
  }
}

// 初始化表单数据
function initFormData() {
  if (props.menuData) {
    Object.assign(formData, props.menuData)
  } else {
    // 重置表单数据
    Object.assign(formData, {
      id: '',
      name: '',
      title: '',
      path: '',
      icon: '',
      showIcon: true,
      sort: 0,
      component: '',
      redirect: '',
      hidden: false,
      keepAlive: false,
      permission: '',
      parentId: null
    })
  }
  
  // 如果是添加子菜单，设置父级菜单ID
  if (props.parentMenu) {
    formData.parentId = props.parentMenu.id || null
  }
}

// 提交表单
async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    
    const submitData = { ...formData }
    emit('submit', submitData)
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 取消操作
function handleCancel() {
  emit('cancel')
}

// 监听菜单数据变化
watch(() => props.menuData, () => {
  initFormData()
}, { immediate: true })

// 监听父级菜单变化
watch(() => props.parentMenu, () => {
  if (props.parentMenu) {
    formData.parentId = props.parentMenu.id || null
  }
}, { immediate: true })

// 组件挂载时加载菜单树
onMounted(() => {
  loadMenuTree()
})
</script>

<style scoped>
.menu-form {
  padding: 0;
}

.icon-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
}

.preview-icon {
  font-size: 16px;
  color: #1890ff;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-item:hover {
  border-color: #1890ff;
  background: #f0f8ff;
}

.icon-item.active {
  border-color: #1890ff;
  background: #e6f7ff;
}

.grid-icon {
  font-size: 24px;
  color: #1890ff;
  margin-bottom: 8px;
}

.icon-name {
  font-size: 12px;
  color: #666;
  text-align: center;
  word-break: break-all;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
}

:deep(.ant-divider) {
  margin: 24px 0 16px 0;
  font-weight: 500;
  color: #1890ff;
}
</style> 