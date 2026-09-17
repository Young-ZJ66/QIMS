import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRoleFromToken } from '@/utils/request'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '首页概览', roles: [1, 2, 3] }
      },
      {
        path: 'client/delegate',
        name: 'ClientDelegate',
        component: () => import('../views/client/delegate.vue'),
        meta: { title: '提交委托单', roles: [3] }
      },
      {
        path: 'client/report',
        name: 'ClientReport',
        component: () => import('../views/client/report.vue'),
        meta: { title: '报告查询', roles: [3] }
      },
      {
        path: 'admin/receive',
        name: 'AdminReceive',
        component: () => import('../views/admin/receive.vue'),
        meta: { title: '收样与盲样派发', roles: [1] }
      },
      {
        path: 'admin/review',
        name: 'AdminReview',
        component: () => import('../views/admin/review.vue'),
        meta: { title: '报告审核签发', roles: [1] }
      },
      {
        path: 'inspector/task',
        name: 'InspectorTask',
        component: () => import('../views/inspector/task.vue'),
        meta: { title: '我的检测任务', roles: [2] }
      },
      {
        path: 'sys/standard',
        name: 'SysStandard',
        component: () => import('../views/sys/standard.vue'),
        meta: { title: '检验标准库', roles: [1] }
      },
      {
        path: 'sys/client',
        name: 'SysClient',
        component: () => import('../views/sys/client.vue'),
        meta: { title: '客户管理', roles: [1] }
      },
      {
        path: 'sys/user',
        name: 'SysUser',
        component: () => import('../views/sys/user.vue'),
        meta: { title: '员工管理', roles: [1] }
      }
    ]
  },
  // 404 catch-all
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
let lastMessage = ''

router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - QIMS` : 'QIMS'

  const token = localStorage.getItem('token')

  // 登录页不需要认证
  if (to.path === '/login') {
    next()
    return
  }

  // 未登录跳转登录页
  if (!token) {
    if (lastMessage !== 'login') {
      ElMessage.warning('请先登录系统')
      lastMessage = 'login'
    }
    next('/login')
    return
  }

  // 检查 token 是否过期
  const roleId = getRoleFromToken()
  if (roleId === null) {
    localStorage.removeItem('token')
    localStorage.removeItem('roleId')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('clientId')
    if (lastMessage !== 'expired') {
      ElMessage.warning('登录已过期，请重新登录')
      lastMessage = 'expired'
    }
    next('/login')
    return
  }

  // 角色鉴权（使用 meta.roles 声明式配置）
  const requiredRoles = to.meta.roles
  if (requiredRoles && !requiredRoles.includes(roleId)) {
    if (lastMessage !== 'noauth') {
      ElMessage.error('无权限访问该页面')
      lastMessage = 'noauth'
    }
    next('/dashboard')
    return
  }

  lastMessage = ''
  next()
})

export default router