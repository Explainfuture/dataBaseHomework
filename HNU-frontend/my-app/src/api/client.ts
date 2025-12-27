import axios from 'axios'
import { clearToken, clearStoredUser, getToken } from '../store/auth'

export type ApiResponse<T> = {
  code: number
  message: string
  data: T
}

export const api = axios.create({
  baseURL: 'http://localhost:8080',
})

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function redirectToLogin() {
  clearToken()
  clearStoredUser()
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

api.interceptors.response.use(
  (response) => {
    const payload = response.data as ApiResponse<unknown> | undefined
    if (payload && typeof payload.code === 'number') {
      if (payload.code === 401) {
        redirectToLogin()
        return Promise.reject(new Error(payload.message || '未登录'))
      }
      if (payload.code !== 200) {
        return Promise.reject(new Error(payload.message || '请求失败'))
      }
      return payload
    }
    return response.data
  },
  (error) => {
    if (error?.response?.status === 401) {
      redirectToLogin()
    }
    return Promise.reject(error)
  },
)
