/**
 * 格式化日期时间
 * @param {string|Date} value - 日期时间值
 * @returns {string} 格式化后的字符串
 */
export function formatDateTime(value) {
  if (!value) return ''
  const str = String(value)
  if (str.includes('T')) return str.replace('T', ' ').slice(0, 19)
  if (str.length >= 19) return str.slice(0, 19)
  return str
}

/**
 * 格式化日期
 * @param {string|Date} value - 日期值
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(value) {
  if (!value) return ''
  const str = String(value)
  if (str.includes('T')) return str.split('T')[0]
  if (str.length >= 10) return str.slice(0, 10)
  return str
}

/**
 * 从 localStorage 安全获取用户信息
 */
export function getUserInfo() {
  return {
    token: localStorage.getItem('token'),
    userId: localStorage.getItem('userId'),
    username: localStorage.getItem('username'),
    roleId: localStorage.getItem('roleId'),
    clientId: localStorage.getItem('clientId')
  }
}

/**
 * 清除用户登录信息
 */
export function clearUserInfo() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('roleId')
  localStorage.removeItem('userId')
  localStorage.removeItem('clientId')
}

/**
 * 判断是否为管理员
 */
export function isAdmin() {
  return localStorage.getItem('roleId') === '1'
}

/**
 * 判断是否为检测员
 */
export function isInspector() {
  return localStorage.getItem('roleId') === '2'
}

/**
 * 判断是否为客户
 */
export function isClient() {
  return localStorage.getItem('roleId') === '3'
}