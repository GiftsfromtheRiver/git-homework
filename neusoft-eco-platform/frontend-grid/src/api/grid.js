import axios from 'axios'
import request from '@/utils/request'

// 登录接口单独用 axios，不走 request 拦截器（避免循环依赖）
const authRequest = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

/**
 * 网格员登录
 * @param {Object} data - { username, password }
 */
export function gridLogin(data) {
  return authRequest.post('/auth/grid/login', data)
}

/**
 * 获取任务列表
 * @param {Object} params - { gridMemberId, pageNum, pageSize }
 */
export function getTaskList(params) {
  return request.get('/grid/tasks/list', { params })
}

/**
 * 获取任务详情
 * @param {Number|String} id - 任务ID
 */
export function getTaskDetail(id) {
  return request.get(`/grid/tasks/${id}`)
}

/**
 * 提交实测数据
 * @param {Object} data - 实测数据
 */
export function submitMeasureData(data) {
  return request.post('/grid/measure/confirm', data)
}
