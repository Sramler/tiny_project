<template>
  <a-form
    :model="form"
    :rules="rules"
    ref="formRef"
    layout="horizontal"
    :label-col="{ span: 6 }"
    :wrapper-col="{ span: 16 }"
  >
    <a-form-item label="角色名" name="name" required>
      <a-input v-model:value="form.name" placeholder="请输入角色名" />
    </a-form-item>
    <a-form-item label="描述" name="description">
      <a-input v-model:value="form.description" placeholder="请输入描述" />
    </a-form-item>
    <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
      <a-button @click="onCancel">取消</a-button>
      <a-button type="primary" style="margin-left:8px;" @click="onSubmit">保存</a-button>
    </a-form-item>
  </a-form>
</template>
<script setup lang="ts">
import { ref, watch, defineProps, defineEmits } from 'vue'
import { message } from 'ant-design-vue'

// 定义props
const props = defineProps<{
  mode: 'create' | 'edit', // 表单模式
  roleData?: any           // 角色数据
}>()
// 定义emit
const emit = defineEmits(['submit', 'cancel'])
// 表单数据
const form = ref({
  id: '',
  name: '',
  description: ''
})
// 校验规则
const rules = {
  name: [
    { required: true, message: '角色名不能为空', trigger: 'blur' },
    { min: 2, max: 50, message: '长度2-50字符', trigger: 'blur' }
  ],
  description: [
    { max: 100, message: '描述最多100字符', trigger: 'blur' }
  ]
}
// 表单ref
const formRef = ref()
// 监听props变化，回填数据
watch(() => props.roleData, (val) => {
  if (val) {
    form.value = { ...val }
  } else {
    form.value = { id: '', name: '', description: '' }
  }
}, { immediate: true })
// 取消
function onCancel() {
  emit('cancel')
}
// 提交
function onSubmit() {
  console.log('保存按钮点击，开始处理...'); // 日志1: 函数被调用
  console.log('当前的表单引用(formRef):', formRef.value); // 日志2: 检查formRef
  
  if (!formRef.value) {
    message.error('表单实例未准备好，请稍后重试');
    console.error('formRef.value is null or undefined. Cannot validate form.');
    return;
  }

  formRef.value.validate()
    .then(() => {
      console.log('表单验证成功，准备提交数据:', { ...form.value }); // 日志3: 验证成功
      emit('submit', { ...form.value });
    })
    .catch((errorInfo: any) => {
      console.error('表单验证失败:', errorInfo); // 日志4: 验证失败
      message.warning('请检查表单，有必填项未填写或格式不正确');
    });
}
</script> 