import axios from 'axios'
import { showToast, showLoadingToast, closeToast } from 'vant'
import router from '@/router'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

let loadingCount = 0

function showLoading() {
  if (loadingCount === 0) {
    showLoadingToast({
      message: '加载中...',
      forbidClick: true,
      duration: 0
    })
  }
  loadingCount++
}

function hideLoading() {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    closeToast()
  }
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('gridToken')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    if (config.loading !== false) {
      showLoading()
    }
    return config
  },
  error => {
    hideLoading()
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    hideLoading()
    const res = response.data
    if (res.code !== 200) {
      showToast(res.message || '请求失败')
      // 401 未授权，跳转登录
      if (res.code === 401) {
        localStorage.removeItem('gridToken')
        localStorage.removeItem('gridMemberInfo')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    hideLoading()
    const message = error.message || '网络异常'
    showToast(message)
    return Promise.reject(error)
  }
)

export default request
