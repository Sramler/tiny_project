// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import Login from '@/views/Login.vue'
import OidcCallback from '@/views/OidcCallback.vue'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { useAuth, initPromise } from '@/auth/auth' // 确保路径正确
import DefaultView from '@/views/default.vue'
import Error401 from '@/views/exception/401.vue' // 引入 401 页面
import Error403 from '@/views/exception/403.vue' // 引入 403 页面
import Error404 from '@/views/exception/404.vue' // 引入 404 页面
import Error500 from '@/views/exception/500.vue' // 引入 500 页面
import Debug from '@/views/OIDCDebug.vue' // 引入调试页面
import TotpBind from '@/views/security/TotpBind.vue'
import TotpVerify from '@/views/security/TotpVerify.vue'
import { menuTree, type MenuItem } from '@/api/menu' // 引入菜单 API
import logger from '@/utils/logger' // 引入日志工具

// 标记是否已加载菜单路由
let menuRoutesLoaded = false

// 递归生成路由，支持动态加载 component
function generateMenuRoutes(menuList: MenuItem[]) {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const routes: any[] = []
  for (const item of menuList) {
    // 跳过隐藏的菜单项
    if (item.hidden) continue
    // 跳过未启用的菜单项
    if (item.enabled === false) continue
    // 跳过特殊错误页，避免 DefaultView 覆盖
    // 注意：403、404、500 已作为主框架子路由配置，这里跳过避免重复
    if (
      item.url &&
      [
        '/exception/401',
        '/exception/403',
        '/exception/404',
        '/exception/500',
        '/401',
        '/403',
        '/404',
        '/500',
      ].includes(item.url)
    )
      continue
    if (item.url) {
      let component
      if (item.component) {
        // 只需要 ../views/xxx.vue
        const compPath = `../views${item.component.replace('/views', '')}`
        //console.log('注册路由:', item.url, '组件路径:', compPath) // 调试输出
        component = () => import(/* @vite-ignore */ compPath)
      } else {
        // 没有 component 字段时 fallback 到 DefaultView
        component = DefaultView
      }
      routes.push({
        path: item.url, // 使用 url 字段作为路由路径
        component,
        meta: {
          menuInfo: item,
          requiresAuth: true, // 菜单路由默认需要认证
          title: item.title,
        },
      })
    }
    if (item.children && item.children.length > 0) {
      routes.push(...generateMenuRoutes(item.children))
    }
  }
  return routes
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9000'
const baseUrl = apiBaseUrl.endsWith('/') ? apiBaseUrl.slice(0, -1) : apiBaseUrl

async function checkBackendSession(): Promise<boolean> {
  try {
    const { fetchWithTraceId } = await import('@/utils/traceId')
    // 使用 skipAuthError，避免在路由守卫中触发跳转（路由守卫会自己处理）
    const resp = await fetchWithTraceId(`${baseUrl}/self/security/status`, {
      method: 'GET',
      credentials: 'include',
      skipAuthError: true, // 跳过自动跳转，由路由守卫处理
    })
    if (resp.ok) {
      const data = await resp.json()
      const valid = !('success' in data) || data.success !== false
      logger.debug('[Auth] 后端会话检测成功:', { valid, data })
      return valid
    }
    logger.warn('[Auth] 后端会话检测失败，状态码:', resp.status)
  } catch (error) {
    logger.error('检查后端会话失败:', error)
  }
  return false
}

// 路由配置
const routes = [
  // 登录页和回调页不使用主布局
  { path: '/login', name: 'Login', component: Login, meta: { title: '登录' } },
  {
    path: '/self/security/totp-bind',
    name: 'TotpBind',
    component: TotpBind,
    meta: { title: '绑定二步验证' },
  },
  {
    path: '/self/security/totp-verify',
    name: 'TotpVerify',
    component: TotpVerify,
    meta: { title: '二步验证' },
  },
  { path: '/callback', name: 'OidcCallback', component: OidcCallback },
  // 401 页面保持独立（登录状态失效，不需要布局）
  {
    path: '/exception/401',
    name: 'Error401',
    component: Error401,
    meta: { title: '401', requiresAuth: false },
  },
  // 主框架路由，所有需要布局的页面作为子路由
  {
    path: '/',
    name: 'mainLayout', // 给主布局路由命名，便于动态添加子路由
    component: BasicLayout, // 使用主布局
    children: [
      {
        path: '',
        name: 'Home',
        component: HomeView,
        meta: { requiresAuth: true, title: '工作台' },
      },
      // 错误页面作为主框架的子路由（使用布局）
      {
        path: 'exception/403',
        name: 'Error403',
        component: Error403,
        meta: { title: '403', requiresAuth: true },
      },
      {
        path: 'exception/404',
        name: 'Error404',
        component: Error404,
        meta: { title: '404', requiresAuth: true },
      },
      {
        path: 'exception/500',
        name: 'Error500',
        component: Error500,
        meta: { title: '500', requiresAuth: true },
      },
      // {
      //   path: 'about',
      //   name: 'About',
      //   component: AboutView,
      //   meta: { requiresAuth: true, title: '分析页' },
      // },
      // {
      //   path: 'modeling',
      //   name: 'modeling',
      //   component: Modeling,
      //   meta: { requiresAuth: true, title: '流程建模' },
      // },
      // {
      //   path: 'definition',
      //   name: 'definition',
      //   component: Definition,
      //   meta: { requiresAuth: true, title: '流程定义' },
      // },
      // {
      //   path: 'deployment',
      //   name: 'deployment',
      //   component: Deployment,
      //   meta: { requiresAuth: true, title: '流程部署' },
      // },
      // {
      //   path: 'instance',
      //   name: 'instance',
      //   component: () => import('@/views/process/Instance.vue'),
      //   meta: { requiresAuth: true, title: '流程实例' },
      // },

      {
        path: 'OIDCDebug',
        name: 'OIDCDebug',
        component: Debug,
        meta: { requiresAuth: true, title: 'OIDC 调试工具' },
      },
      // 菜单路由将在动态加载时添加，这里先留空
    ],
  },
  // 全局兜底 404（重定向到主框架的 404 页面）
  { path: '/:pathMatch(.*)*', name: 'NotFound', redirect: '/exception/404' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 动态加载菜单路由
async function loadMenuRoutes() {
  if (menuRoutesLoaded) {
    return // 已经加载过，不再重复加载
  }

  try {
    logger.log('🔄 开始从后端加载菜单路由...')
    const menuData = await menuTree()

    if (menuData && Array.isArray(menuData) && menuData.length > 0) {
      const routes = generateMenuRoutes(menuData)

      // 使用 router.addRoute 在主布局路由下添加子路由
      let addedCount = 0
      let skippedCount = 0

      routes.forEach((route) => {
        // 检查路由是否已存在，避免重复添加
        // 注意：某些基础路由（如 /）可能在初始配置中已存在，这是正常的
        const existingRoute = router.getRoutes().find((r) => r.path === route.path)
        if (!existingRoute) {
          router.addRoute('mainLayout', route)
          addedCount++
          logger.debug('✅ 已添加菜单路由:', route.path, route.meta?.title)
        } else {
          skippedCount++
          logger.debug('ℹ️ 路由已存在（初始配置），跳过:', route.path)
        }
      })

      if (addedCount > 0 || skippedCount > 0) {
        logger.log(`✅ 菜单路由处理完成: 新增 ${addedCount} 个，跳过 ${skippedCount} 个（已存在）`)
      }
      menuRoutesLoaded = true
      logger.log('✅ 菜单路由加载完成，共', routes.length, '个路由')
    } else {
      logger.warn('⚠️ 菜单数据为空，无法生成路由')
    }
  } catch (error) {
    logger.error('❌ 加载菜单路由失败:', error)
    // 加载失败不影响应用运行，只是菜单路由不可用
    // 可以在这里添加错误提示或降级处理
  }
}

// 路由守卫，处理鉴权
router.beforeEach(async (to, from, next) => {
  // ⚠️ 重要：401 页面必须最先检查，在任何认证逻辑之前
  // 这样可以确保即使认证状态初始化失败，401 页面也能正常访问
  if (to.path === '/exception/401') {
    logger.log('访问 401 页面，直接放行（跳过所有认证检查）:', to.path)
    next()
    return
  }

  const { isAuthenticated, login } = useAuth()

  // 等待认证状态初始化完成
  try {
    await initPromise
  } catch (error) {
    logger.error('认证状态初始化失败:', error)
    // 即使初始化失败，也继续执行（异常页面已经在上面的检查中处理了）
  }

  // 如果用户已认证，尝试加载菜单路由
  if (isAuthenticated.value && !menuRoutesLoaded) {
    await loadMenuRoutes()
    // 菜单路由加载完成后，如果当前路径应该匹配新加载的路由，需要重新导航
    // 使用 replace: true 避免在历史记录中留下中间状态
    if (menuRoutesLoaded) {
      // 重新导航到当前路径，让 Vue Router 重新匹配路由
      next({ ...to, replace: true })
      return
    }
  }

  // 如果用户已认证且访问登录页，重定向到首页
  if (to.path === '/login' && isAuthenticated.value) {
    logger.log('用户已认证，重定向到首页')
    next('/')
    return
  }

  // 如果访问需要认证的页面但用户未认证，重定向到登录页
  if (to.meta.requiresAuth && !isAuthenticated.value) {
    // 检查是否已经在 OIDC 回调流程中（避免重复处理）
    const urlParams = new URLSearchParams(window.location.search)
    if (urlParams.has('code') || urlParams.has('error')) {
      logger.log('检测到 OIDC 回调参数，不进行重定向')
      next()
      return
    }

    logger.log('用户未认证，重定向到登录页')

    // 检查后端会话
    const backendSession = await checkBackendSession()
    if (backendSession) {
      logger.log('检测到有效的后端会话，但缺少 OIDC token，尝试自动完成授权流程')

      try {
        await login()
        return // login() 会触发页面跳转，不需要调用 next()
      } catch (error) {
        logger.error('基于后端会话触发 OIDC 授权失败:', error)
        next('/login')
        return
      }
    }

    // 没有后端会话，尝试登录
    try {
      await login()
      // login() 会触发页面跳转，不需要调用 next()
      return
    } catch (error) {
      logger.error('登录重定向失败:', error)
      // 如果登录失败，跳转到登录页
      next('/login')
      return
    }
  }

  // 其他情况正常放行
  next()
})

export default router
