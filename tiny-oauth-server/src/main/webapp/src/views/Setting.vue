<template>
  <div class="setting-page">
    <a-card title="个人设置" :bordered="false">
      <a-tabs v-model:activeKey="activeKey">
        <!-- 基本信息 -->
        <a-tab-pane key="basic" tab="基本信息">
          <a-form
            :model="basicForm"
            :rules="basicRules"
            :label-col="{ span: 4 }"
            :wrapper-col="{ span: 16 }"
            @finish="handleBasicSubmit"
          >
            <a-form-item label="用户名" name="username">
              <a-input v-model:value="basicForm.username" disabled />
              <div class="form-help-text">用户名不可修改</div>
            </a-form-item>
            <a-form-item label="昵称" name="nickname">
              <a-input v-model:value="basicForm.nickname" placeholder="请输入昵称" />
            </a-form-item>
            <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
              <a-button type="primary" html-type="submit" :loading="basicLoading">
                保存
              </a-button>
              <a-button style="margin-left: 8px" @click="resetBasicForm">
                重置
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <!-- 修改密码 -->
        <a-tab-pane key="password" tab="修改密码">
          <a-form
            :model="passwordForm"
            :rules="passwordRules"
            :label-col="{ span: 4 }"
            :wrapper-col="{ span: 16 }"
            @finish="handlePasswordSubmit"
          >
            <a-form-item label="原密码" name="oldPassword">
              <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入原密码" />
            </a-form-item>
            <a-form-item label="新密码" name="newPassword">
              <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码（6-20个字符）" />
            </a-form-item>
            <a-form-item label="确认密码" name="confirmPassword">
              <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
            </a-form-item>
            <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
              <a-button type="primary" html-type="submit" :loading="passwordLoading">
                修改密码
              </a-button>
              <a-button style="margin-left: 8px" @click="resetPasswordForm">
                重置
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <!-- 安全设置 -->
        <a-tab-pane key="security" tab="安全设置">
          <a-space direction="vertical" size="large" style="width: 100%">
            <a-card title="两步验证" size="small">
              <a-space direction="vertical" style="width: 100%">
                <div>
                  <p>两步验证可以为您的账户提供额外的安全保护。</p>
                  <a-button type="primary" @click="handleBindTotp" style="margin-top: 16px">
                    绑定两步验证
                  </a-button>
                </div>
              </a-space>
            </a-card>
          </a-space>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { Rule } from 'ant-design-vue/es/form'
import { message } from 'ant-design-vue'
import { userApi } from '@/api/process'
import { updateUser } from '@/api/user'

// 路由
const router = useRouter()

// 当前激活的标签页
const activeKey = ref('basic')

// 基本信息表单
const basicForm = reactive({
  id: '',
  username: '',
  nickname: ''
})

// 基本信息表单验证规则
const basicRules: Record<string, Rule[]> = {
  nickname: [
    { required: true, message: '请输入昵称' },
    { max: 50, message: '昵称长度不能超过50个字符' }
  ]
}

// 基本信息加载状态
const basicLoading = ref(false)

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码表单验证规则
const passwordRules: Record<string, Rule[]> = {
  oldPassword: [
    { required: true, message: '请输入原密码' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码' },
    { min: 6, max: 20, message: '密码长度为6-20个字符' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码' },
    {
      validator: (_rule: any, value: string) => {
        if (!value) {
          return Promise.reject('请确认新密码')
        }
        if (value !== passwordForm.newPassword) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      }
    }
  ]
}

// 密码修改加载状态
const passwordLoading = ref(false)

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const data = await userApi.getCurrentUser()
    basicForm.id = data.id
    basicForm.username = data.username
    basicForm.nickname = data.nickname || ''
  } catch (error) {
    console.error('加载用户信息失败:', error)
    message.error('加载用户信息失败')
  }
}

// 提交基本信息
const handleBasicSubmit = async () => {
  basicLoading.value = true
  try {
    await updateUser(basicForm.id, {
      nickname: basicForm.nickname
    })
    message.success('基本信息更新成功')
    await loadUserInfo()
  } catch (error) {
    console.error('更新基本信息失败:', error)
    message.error('更新基本信息失败')
  } finally {
    basicLoading.value = false
  }
}

// 重置基本信息表单
const resetBasicForm = async () => {
  await loadUserInfo()
}

// 提交密码修改
const handlePasswordSubmit = async () => {
  passwordLoading.value = true
  try {
    // TODO: 这里需要调用修改密码的API，可能需要先验证原密码
    // 目前先使用更新用户接口，后端需要支持密码修改
    await updateUser(basicForm.id, {
      password: passwordForm.newPassword,
      oldPassword: passwordForm.oldPassword
    })
    message.success('密码修改成功，请重新登录')
    // 清空密码表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    // 延迟跳转到登录页
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    console.error('修改密码失败:', error)
    message.error('修改密码失败，请检查原密码是否正确')
  } finally {
    passwordLoading.value = false
  }
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

// 绑定两步验证
const handleBindTotp = () => {
  router.push('/self/security/totp-bind')
}

// 组件挂载时加载数据
onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.setting-page {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.form-help-text {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  margin-top: 4px;
}
</style>