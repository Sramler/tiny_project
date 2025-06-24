<template>
  <div class="edit-user-page">
    <a-card :title="props.mode === 'create' ? '新建用户' : props.mode === 'edit' ? '编辑用户' : '查看用户'" bordered>
      <a-form
        :model="form"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
        @finish="throttledSubmit"
      >
        <a-form-item label="ID" v-if="form.id">
          <a-input v-model:value="form.id" disabled />
        </a-form-item>
        <a-form-item label="用户名" name="username" required>
          <a-input v-model:value="form.username" :disabled="props.mode === 'view'" />
        </a-form-item>
        <a-form-item label="密码" name="password" :required="props.mode === 'create'">
          <a-input v-model:value="form.password" type="password" :disabled="props.mode === 'view'" :placeholder="props.mode === 'edit' ? '留空表示不修改' : ''" />
        </a-form-item>
        <a-form-item label="昵称" name="nickname" required>
          <a-input v-model:value="form.nickname" :disabled="props.mode === 'view'" />
        </a-form-item>
        <a-form-item label="是否启用" name="enabled">
          <a-switch v-model:checked="form.enabled" :disabled="props.mode === 'view'" />
        </a-form-item>
        <a-form-item label="账号未过期" name="account_non_expired">
          <a-switch v-model:checked="form.account_non_expired" :disabled="props.mode === 'view'" />
        </a-form-item>
        <a-form-item label="账号未锁定" name="account_non_locked">
          <a-switch v-model:checked="form.account_non_locked" :disabled="props.mode === 'view'" />
        </a-form-item>
        <a-form-item label="密码未过期" name="credentials_non_expired">
          <a-switch v-model:checked="form.credentials_non_expired" :disabled="props.mode === 'view'" />
        </a-form-item>
        <a-form-item label="最后登录时间" name="last_login_at" v-if="form.id">
          <a-input v-model:value="form.last_login_at" disabled />
        </a-form-item>
        <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
          <a-button v-if="props.mode !== 'view'" type="primary" html-type="submit">保存</a-button>
          <a-button style="margin-left: 8px" @click="throttledCancel">返回</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Rule } from 'ant-design-vue/es/form'
import { useThrottleFn } from '@/utils/throttle'

const form = ref({
  id: '',
  username: '',
  password: '',
  nickname: '',
  enabled: true,
  account_non_expired: true,
  account_non_locked: true,
  credentials_non_expired: true,
  last_login_at: ''
})

const rules: Record<string, Rule[]> = {
  username: [
    { required: true, message: '请输入用户名' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符' }
  ],
  password: [
    { required: true, message: '请输入密码' },
    { min: 6, max: 20, message: '密码长度为6-20个字符' }
  ],
  nickname: [
    { required: true, message: '请输入昵称' }
  ]
}

const props = defineProps({
  mode: {
    type: String as () => 'create' | 'edit' | 'view',
    default: 'view'
  },
  userData: {
    type: Object as () => any | null,
    default: () => null
  }
})

const emit = defineEmits(['submit', 'cancel'])

watch(() => props.userData, (newData) => {
  if (newData) {
    Object.assign(form.value, newData)
    // 编辑模式下不清空密码，让用户选择是否修改
    if (props.mode === 'edit') {
        form.value.password = ''
    }
  } else {
    // 新建模式或无数据时重置表单
    Object.assign(form.value, {
      id: '',
      username: '',
      password: '',
      nickname: '',
      enabled: true,
      account_non_expired: true,
      account_non_locked: true,
      credentials_non_expired: true,
      last_login_at: ''
    })
  }
}, { immediate: true, deep: true })


function onSubmit() {
  emit('submit', form.value)
}

function onCancel() {
  emit('cancel')
}

const throttledSubmit = useThrottleFn(onSubmit, 1000)
const throttledCancel = useThrottleFn(onCancel, 500)
</script>

<style scoped>
.edit-user-page {
  padding: 0;
  height: 100%;
  background: #fff;
}
</style>