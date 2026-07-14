import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    // If we have a standardized API response structure
    if (res && res.code !== undefined) {
      if (res.code === 200) {
        return res.data
      } else {
        ElMessage.error(res.message || '业务处理失败')
        return Promise.reject(new Error(res.message || 'Error'))
      }
    }
    // Fallback for endpoints that haven't been wrapped yet
    return res
  },
  (error) => {
    // 404 静态资源错误（uploads/actuator/swagger）— 静默降级，不弹窗打扰用户
    if (error.response?.status === 404 && isStaticResourceError(error.config?.url)) {
      // 仅在开发环境记录 debug 日志
      if (import.meta.env.DEV) {
        console.debug('[RequestInterceptor] Static resource not found:', error.config.url)
      }
      // 静默返回，不弹窗
      return Promise.reject(error)
    }

    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.warning('登录已过期，请重新登录')
      localStorage.removeItem('token')
      router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
    } else {
      // Show backend error message
      const msg = error.response?.data?.message || error.message || '请求失败'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  },
)

/**
 * 判断是否为静态资源错误路径
 * @param {string} url - 请求 URL
 * @returns {boolean} 是否为静态资源
 */
function isStaticResourceError(url) {
  if (!url) return false
  return url.startsWith('/uploads/') ||
         url.startsWith('/actuator/') ||
         url.startsWith('/swagger-ui/') ||
         url.startsWith('/v3/api-docs/')
}

export default request
