import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'ant-design-vue/dist/reset.css';
import { initPromise } from './auth/auth'

import App from './App.vue'
import router from './router'
async function bootstrap() {
  await initPromise // 初始化 auth 状态，必须在 app mount 前

  const app = createApp(App)
  app.use(createPinia())
  app.use(router)
  app.mount('#app')
}

bootstrap()
