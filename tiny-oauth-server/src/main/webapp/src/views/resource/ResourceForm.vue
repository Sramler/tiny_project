<template>
  <div class="resource-form">
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
      
      <a-form-item label="资源名称" name="name">
        <a-input 
          v-model:value="formData.name" 
          placeholder="请输入资源名称"
          maxlength="100"
          show-count
        />
      </a-form-item>
      
      <a-form-item label="显示标题" name="title">
        <a-input 
          v-model:value="formData.title" 
          placeholder="请输入显示标题"
          maxlength="100"
          show-count
        />
      </a-form-item>
      
      <a-form-item label="资源类型" name="type">
        <a-select v-model:value="formData.type" placeholder="请选择资源类型">
          <a-select-option :value="0">目录</a-select-option>
          <a-select-option :value="1">菜单</a-select-option>
          <a-select-option :value="2">按钮</a-select-option>
        </a-select>
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
      
      <!-- 显示配置 -->
      <a-divider>显示配置</a-divider>
      
      <a-form-item label="菜单图标" name="icon">
        <a-input 
          v-model:value="formData.icon" 
          placeholder="请输入图标名称，如：UserOutlined"
          maxlength="200"
          show-count
        />
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
import { ref, reactive, onMounted, watch } from 'vue'
import type { FormInstance } from 'ant-design-vue'

// 定义props
interface Props {
  mode: 'create' | 'edit'
  resourceData: any
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
  type: 2,
  parentId: null
})

// 提交状态
const submitting = ref(false)

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入资源名称', trigger: 'blur' },
    { max: 100, message: '资源名称不能超过100个字符', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入显示标题', trigger: 'blur' },
    { max: 100, message: '显示标题不能超过100个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ],
  uri: [
    { max: 200, message: 'API路径不能超过200个字符', trigger: 'blur' }
  ],
  permission: [
    { max: 100, message: '权限标识不能超过100个字符', trigger: 'blur' }
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
  icon: [
    { max: 200, message: '图标名称不能超过200个字符', trigger: 'blur' }
  ]
}

// 初始化表单数据
function initFormData() {
  if (props.resourceData) {
    Object.assign(formData, props.resourceData)
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
watch(() => props.resourceData, () => {
  initFormData()
}, { immediate: true })

// 组件挂载
onMounted(() => {
  initFormData()
})
</script>

<style scoped>
.resource-form {
  padding: 0;
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