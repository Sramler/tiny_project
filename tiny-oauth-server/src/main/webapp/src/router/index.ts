// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import AboutView from '@/views/AboutView.vue'
import Login from '@/views/Login.vue'
import OidcCallback from '@/views/OidcCallback.vue'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { useAuth } from '@/auth/auth' // 确保路径正确
import DefaultView from '@/views/default.vue'
import menuData from '../../public/menu.json'
import Error403 from '@/views/403.vue' // 引入 403 页面
import Error404 from '@/views/404.vue' // 引入 404 页面
import Error500 from '@/views/500.vue' // 引入 500 页面
import { defineAsyncComponent } from 'vue' // 引入 defineAsyncComponent 用于动态加载组件

// 递归生成路由，支持动态加载 component
function generateMenuRoutes(menuList: any[]): any[] {
  const routes: any[] = []
  for (const item of menuList) {
    // 跳过特殊错误页，避免 DefaultView 覆盖
    if (['/403', '/404', '/500'].includes(item.path)) continue
    if (item.path) {
      let component
      if (item.component) {
        // 只需要 ../views/xxx.vue
        const compPath = `../views${item.component.replace('/views', '')}`
        console.log('注册路由:', item.path, '组件路径:', compPath) // 调试输出
        component = () => import(/* @vite-ignore */ compPath)
      } else {
        // 没有 component 字段时 fallback 到 DefaultView
        component = DefaultView
      }
      routes.push({
        path: item.path, // 保持和菜单 path 完全一致
        component,
        meta: { menuInfo: item },
      })
    }
    if (item.children) {
      routes.push(...generateMenuRoutes(item.children))
    }
  }
  return routes
}

const menuRoutes = generateMenuRoutes(menuData)

// 路由配置
const routes = [
  // 主框架路由，所有需要布局的页面作为子路由
  {
    path: '/',
    component: BasicLayout, // 使用主布局
    children: [
      {
        path: '',
        name: 'Home',
        component: HomeView,
        meta: { requiresAuth: true, title: '工作台' },
      },
      {
        path: 'about',
        name: 'About',
        component: AboutView,
        meta: { requiresAuth: true, title: '分析页' },
      },
      { path: 'login', name: 'LoginPage', component: Login, meta: { title: '基础详情页' } },
      // 自动生成的菜单页面
      ...menuRoutes,
      // 403、404、500 错误页面
      { path: '403', name: 'Error403', component: Error403, meta: { title: '403' } },
      { path: '404', name: 'Error404', component: Error404, meta: { title: '404' } },
      { path: '500', name: 'Error500', component: Error500, meta: { title: '500' } },
    ],
  },
  // 登录页和回调页不使用主布局
  { path: '/login', name: 'Login', component: Login },
  { path: '/callback', name: 'OidcCallback', component: OidcCallback },
  // 全局兜底 404
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: Error404 },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})
// 路由守卫，处理鉴权
router.beforeEach(async (to, from, next) => {
  const { isAuthenticated, login } = useAuth()
  // 不在 /login、/callback 路由下才处理
  if (
    to.meta.requiresAuth &&
    !isAuthenticated.value &&
    !['/login', '/callback'].includes(to.path)
  ) {
    await login()
  } else {
    next()
  }
})

export default router
