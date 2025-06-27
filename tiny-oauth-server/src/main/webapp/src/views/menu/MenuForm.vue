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
      
      <a-form-item label="菜单名称" name="title">
        <a-input 
          v-model:value="formData.title" 
          placeholder="请输入菜单显示名称"
          maxlength="100"
          show-count
        />
      </a-form-item>
      
      <a-form-item label="权限资源名" name="name">
        <a-input 
          v-model:value="formData.name" 
          placeholder="请输入后端内部识别名"
          maxlength="100"
          show-count
        />
      </a-form-item>
      
      <a-form-item label="菜单类型" name="type">
        <a-select v-model:value="formData.type" placeholder="请选择菜单类型">
          <a-select-option :value="0">目录</a-select-option>
          <a-select-option :value="1">菜单</a-select-option>
          <a-select-option :value="2">按钮</a-select-option>
        </a-select>
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
      
      <!-- API配置 -->
      <a-divider>API配置</a-divider>
      
      <a-form-item label="API路径" name="uri">
        <a-input 
          v-model:value="formData.uri" 
          placeholder="请输入后端API路径，如：/api/user"
          maxlength="200"
          show-count
        />
      </a-form-item>
      
      <a-form-item label="HTTP方法" name="method">
        <a-select v-model:value="formData.method" placeholder="请选择HTTP方法">
          <a-select-option value="GET">GET</a-select-option>
          <a-select-option value="POST">POST</a-select-option>
          <a-select-option value="PUT">PUT</a-select-option>
          <a-select-option value="DELETE">DELETE</a-select-option>
          <a-select-option value="PATCH">PATCH</a-select-option>
        </a-select>
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
  TableOutlined,
  FormOutlined,
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  KeyOutlined,
  LockOutlined,
  SafetyOutlined,
  ToolOutlined,
  ApiOutlined,
  DatabaseOutlined,
  CloudOutlined,
  BarChartOutlined,
  PieChartOutlined,
  LineChartOutlined,
  CalendarOutlined,
  ClockCircleOutlined,
  BellOutlined,
  MailOutlined,
  MessageOutlined
} from '@ant-design/icons-vue'

// 定义props
interface Props {
  mode: 'create' | 'edit'
  menuData: any
  parentMenu?: any
}

const props = defineProps<Props>()

// 定义emits
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
  uri: '',
  method: 'GET',
  icon: '',
  showIcon: true,
  sort: 0,
  component: '',
  redirect: '',
  hidden: false,
  keepAlive: false,
  permission: '',
  type: 1,
  parentId: null
})

// 提交状态
const submitting = ref(false)

// 图标选择器
const showIconSelector = ref(false)

// 菜单树数据（用于父菜单选择）
const menuTreeData = ref<any[]>([])

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
  { name: 'TableOutlined', component: TableOutlined },
  { name: 'FormOutlined', component: FormOutlined },
  { name: 'SearchOutlined', component: SearchOutlined },
  { name: 'PlusOutlined', component: PlusOutlined },
  { name: 'EditOutlined', component: EditOutlined },
  { name: 'DeleteOutlined', component: DeleteOutlined },
  { name: 'EyeOutlined', component: EyeOutlined },
  { name: 'KeyOutlined', component: KeyOutlined },
  { name: 'LockOutlined', component: LockOutlined },
  { name: 'SafetyOutlined', component: SafetyOutlined },
  { name: 'ToolOutlined', component: ToolOutlined },
  { name: 'ApiOutlined', component: ApiOutlined },
  { name: 'DatabaseOutlined', component: DatabaseOutlined },
  { name: 'CloudOutlined', component: CloudOutlined },
  { name: 'BarChartOutlined', component: BarChartOutlined },
  { name: 'PieChartOutlined', component: PieChartOutlined },
  { name: 'LineChartOutlined', component: LineChartOutlined },
  { name: 'CalendarOutlined', component: CalendarOutlined },
  { name: 'ClockCircleOutlined', component: ClockCircleOutlined },
  { name: 'BellOutlined', component: BellOutlined },
  { name: 'MailOutlined', component: MailOutlined },
  { name: 'MessageOutlined', component: MessageOutlined }
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
  'TableOutlined': TableOutlined,
  'FormOutlined': FormOutlined,
  'SearchOutlined': SearchOutlined,
  'PlusOutlined': PlusOutlined,
  'EditOutlined': EditOutlined,
  'DeleteOutlined': DeleteOutlined,
  'EyeOutlined': EyeOutlined,
  'KeyOutlined': KeyOutlined,
  'LockOutlined': LockOutlined,
  'SafetyOutlined': SafetyOutlined,
  'ToolOutlined': ToolOutlined,
  'ApiOutlined': ApiOutlined,
  'DatabaseOutlined': DatabaseOutlined,
  'CloudOutlined': CloudOutlined,
  'BarChartOutlined': BarChartOutlined,
  'PieChartOutlined': PieChartOutlined,
  'LineChartOutlined': LineChartOutlined,
  'CalendarOutlined': CalendarOutlined,
  'ClockCircleOutlined': ClockCircleOutlined,
  'BellOutlined': BellOutlined,
  'MailOutlined': MailOutlined,
  'MessageOutlined': MessageOutlined
}

// 获取图标组件
function getIconComponent(iconName: string) {
  return iconMap[iconName] || AppstoreOutlined
}

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { max: 100, message: '菜单名称不能超过100个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入权限资源名', trigger: 'blur' },
    { max: 100, message: '权限资源名不能超过100个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择菜单类型', trigger: 'change' }
  ],
  path: [
    { max: 200, message: '前端路径不能超过200个字符', trigger: 'blur' }
  ],
  component: [
    { max: 200, message: '组件路径不能超过200个字符', trigger: 'blur' }
  ],
  redirect: [
    { max: 200, message: '重定向地址不能超过200个字符', trigger: 'blur' }
  ],
  uri: [
    { max: 200, message: 'API路径不能超过200个字符', trigger: 'blur' }
  ],
  permission: [
    { max: 100, message: '权限标识不能超过100个字符', trigger: 'blur' }
  ],
  icon: [
    { max: 200, message: '图标名称不能超过200个字符', trigger: 'blur' }
  ]
}

// 选择图标
function selectIcon(iconName: string) {
  formData.icon = iconName
  showIconSelector.value = false
}

// 加载菜单树数据
async function loadMenuTree() {
  try {
    // 这里需要调用API获取菜单树数据
    // const res = await getMenuTree()
    // menuTreeData.value = res || []
    
    // 临时模拟数据
    menuTreeData.value = [
      {
        id: 1,
        title: '系统管理',
        children: [
          { id: 2, title: '用户管理' },
          { id: 3, title: '角色管理' }
        ]
      }
    ]
  } catch (error) {
    console.error('加载菜单树失败:', error)
  }
}

// 初始化表单数据
function initFormData() {
  if (props.menuData) {
    Object.assign(formData, props.menuData)
  }
  
  // 如果是添加子菜单，设置父菜单ID
  if (props.parentMenu) {
    formData.parentId = props.parentMenu.id
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

// 取消
function handleCancel() {
  emit('cancel')
}

// 监听props变化
watch(() => props.menuData, () => {
  initFormData()
}, { immediate: true })

// 组件挂载
onMounted(() => {
  loadMenuTree()
  initFormData()
})
</script>

<style scoped>
.menu-form {
  padding: 0;
}

.icon-selector {
  display: flex;
  align-items: center;
}

.icon-preview {
  display: flex;
  align-items: center;
  margin-top: 8px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
}

.preview-icon {
  font-size: 16px;
  color: #1890ff;
  margin-right: 8px;
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
  padding: 12px 8px;
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
  font-size: 20px;
  color: #1890ff;
  margin-bottom: 4px;
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

:deep(.ant-divider) {
  margin: 16px 0;
  font-weight: 500;
  color: #1890ff;
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
}
</style> 