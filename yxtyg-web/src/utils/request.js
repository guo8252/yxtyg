import axios from 'axios'
import { Message, Notification } from 'element-ui'
import store from '@/store'

// 创建axios实例
const service = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = store.state.user.token
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    if (response.config.responseType === 'blob') {
      return response.data
    }
    const res = response.data
    if (res.code !== 200) {
      // 业务错误（非200），使用顶部通知框提示，3秒后消失
      Notification({
        title: '提示',
        message: res.message || '请求失败',
        type: 'warning',
        duration: 3000
      })
      // 返回数据，让调用方自行处理
      return res
    }
    return res
  },
  error => {
    console.error('响应错误：', error)
    if (error.response && error.response.status === 401) {
      store.dispatch('user/logout')
      window.location.href = '/login'
    } else {
      // 系统错误（网络错误、服务器500等），使用消息框提示
      Message.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
