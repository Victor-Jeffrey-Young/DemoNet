import axios from 'axios'
import { ElMessage } from 'element-plus'

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
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.warning('登录已过期，请重新登录')
      localStorage.removeItem('token')
      setTimeout(() => { window.location.href = '/login' }, 800)
    }
    return Promise.reject(error)
  },
)

export default request
