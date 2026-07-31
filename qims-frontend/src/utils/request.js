import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api', // 通过 vite 代理或直接访问
  timeout: 10000 // 请求超时时间
})

// 标记是否正在刷新 Token，避免并发刷新
let isRefreshing = false

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 统一附加 JWT Token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 校验响应状态码
    if (res.code !== 200) {
      ElMessage({
        message: res.message || 'Error',
        type: 'error',
        duration: 5 * 1000
      })

      // 401 表示 token 失效或未登录
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('roleId')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      // 检查响应头中是否标记 Token 即将过期，如果是则静默刷新
      const expiringSoon = response.headers['x-token-expiring-soon']
      if (expiringSoon === 'true' && !isRefreshing) {
        isRefreshing = true
        // 静默刷新 Token
        axios.post(`${service.defaults.baseURL}/auth/refresh-token`, {}, {
          headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        }).then(refreshRes => {
          if (refreshRes.data && refreshRes.data.code === 200 && refreshRes.data.data.token) {
            localStorage.setItem('token', refreshRes.data.data.token)
            console.log('Token 已静默刷新')
          }
        }).catch(() => {
          // 刷新失败，忽略，下次请求会再次尝试或最终被 401 踢出
        }).finally(() => {
          isRefreshing = false
        })
      }

      // 成功直接返回 data 内容
      return res.data
    }
  },
  error => {
    ElMessage({
      message: error.message || '网络异常',
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

/**
 * 从 JWT Token 中解析用户角色（替代直接从 localStorage 读取，防止篡改）
 * @returns {number|null} roleId (1-管理员, 2-检测员, 3-客户)
 */
export function getRoleFromToken() {
  const token = localStorage.getItem('token')
  if (!token) return null
  try {
    const payload = token.split('.')[1]
    const decoded = JSON.parse(atob(payload))
    return decoded.roleId || null
  } catch (e) {
    return null
  }
}

export default service
