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
import Debug from '@/views/Debug.vue' // 引入调试页面
import { defineAsyncComponent } from 'vue' // 引入 defineAsyncComponent 用于动态加载组件

// 递归生成路由，支持动态加载 component
function generateMenuRoutes(menuList: any[]): any[] {
  const routes: any[] = []
  for (const item of menuList) {
    // 跳过特殊错误页，避免 DefaultView 覆盖
    if (['/403', '/404', '/500'].includes(item.url)) continue
    if (item.url) {
      let component
      if (item.component) {
        // 只需要 ../views/xxx.vue
        const compPath = `../views${item.component.replace('/views', '')}`
        console.log('注册路由:', item.url, '组件路径:', compPath) // 调试输出
        component = () => import(/* @vite-ignore */ compPath)
      } else {
        // 没有 component 字段时 fallback 到 DefaultView
        component = DefaultView
      }
      routes.push({
        path: item.url, // 使用 url 字段作为路由路径
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
  // 登录页和回调页不使用主布局
  { path: '/login', name: 'Login', component: Login, meta: { title: '登录' } },
  { path: '/callback', name: 'OidcCallback', component: OidcCallback },

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
      {
        path: 'debug',
        name: 'Debug',
        component: Debug,
        meta: { requiresAuth: true, title: 'OIDC 调试工具' },
      },
      // 自动生成的菜单页面
      ...menuRoutes,
      // 403、404、500 错误页面
      { path: '403', name: 'Error403', component: Error403, meta: { title: '403' } },
      { path: '404', name: 'Error404', component: Error404, meta: { title: '404' } },
      { path: '500', name: 'Error500', component: Error500, meta: { title: '500' } },
    ],
  },
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

  // 如果用户已认证且访问登录页，重定向到首页
  if (to.path === '/login' && isAuthenticated.value) {
    console.log('用户已认证，重定向到首页')
    next('/')
    return
  }

  // 如果访问需要认证的页面但用户未认证，重定向到登录页
  if (to.meta.requiresAuth && !isAuthenticated.value) {
    console.log('用户未认证，重定向到登录页')
    // 保存原始路径，登录成功后跳转回去
    const returnUrl = to.fullPath
    await login()
    return // 不调用 next()，因为 login() 会触发页面跳转
  }

  // 其他情况正常放行
  next()
})

export default router
