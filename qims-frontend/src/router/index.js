import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRoleFromToken } from '@/utils/request'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue')
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
        meta: { title: '首页大屏' }
      },
      {
        path: 'client/delegate',
        name: 'ClientDelegate',
        component: () => import('../views/client/delegate.vue'),
        meta: { title: '提交委托单' }
      },
      {
        path: 'admin/receive',
        name: 'AdminReceive',
        component: () => import('../views/admin/receive.vue'),
        meta: { title: '收样与盲样派发' }
      },
      {
        path: 'inspector/task',
        name: 'InspectorTask',
        component: () => import('../views/inspector/task.vue'),
        meta: { title: '我的检测任务 (盲样)' }
      },
      {
        path: 'client/report',
        name: 'ClientReport',
        component: () => import('../views/client/report.vue'),
        meta: { title: '报告查询' }
      },
      {
        path: 'admin/review',
        name: 'AdminReview',
        component: () => import('../views/admin/review.vue'),
        meta: { title: '报告审核签发' }
      },
      {
        path: 'sys/standard',
        name: 'SysStandard',
        component: () => import('../views/sys/standard.vue'),
        meta: { title: '检验标准库' }
      },
      {
        path: 'sys/client',
        name: 'SysClient',
        component: () => import('../views/sys/client.vue'),
        meta: { title: '客户管理' }
      },
      {
        path: 'sys/user',
        name: 'SysUser',
        component: () => import('../views/sys/user.vue'),
        meta: { title: '员工管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  // 从 JWT Token 中解析角色
  const roleId = getRoleFromToken()

  if (to.path !== '/login' && !token) {
    // 强制跳转登录页
    ElMessage.warning('请先登录系统')
    next('/login')
  } else {
    // 角色鉴权（基于 JWT Token 中的 roleId，不可被前端篡改）
    if (to.path.startsWith('/admin') && roleId != 1) {
      ElMessage.error('无权限访问管理员模块')
      next('/dashboard')
    } else if (to.path.startsWith('/inspector') && roleId != 2) {
      ElMessage.error('无权限访问实验室模块')
      next('/dashboard')
    } else if (to.path.startsWith('/client') && roleId != 3) {
      ElMessage.error('无权限访问客户模块')
      next('/dashboard')
    } else {
      next()
    }
  }
})

export default router
