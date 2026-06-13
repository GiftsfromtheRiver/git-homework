import request from '@/api/request'
import axios from 'axios'

// 登录接口单独用axios，不走request拦截器（避免循环依赖）
const authRequest = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

/**
 * 管理员登录
 * @param {Object} data - { adminCode, password }
 */
export function adminLogin(data) {
  return authRequest.post('/auth/admin/login', data)
}

/**
 * 获取反馈分页列表
 * @param {Object} params - { pageNum, pageSize, status }
 */
export function getFeedbackPage(params) {
  return request.get('/admin/feedback/list', { params })
}

/**
 * 获取反馈详情
 * @param {Number|String} afId - 反馈编号
 */
export function getFeedbackDetail(afId) {
  return request.get(`/admin/feedback/${afId}`)
}

/**
 * 获取网格员列表
 */
export function getGridMemberList(params) {
  return request.get('/admin/grid-members', { params })
}

/**
 * 获取反馈指派信息（本市+外省网格员）
 * @param {Number|String} afId - 反馈编号
 */
export function getAssignInfo(afId) {
  return request.get(`/admin/feedback/${afId}/assign-info`)
}

/**
 * 智能指派网格员
 * @param {Object} data - { feedbackId, gridMemberId, isCrossProvince }
 */
export function smartAssignFeedback(data) {
  return request.put('/admin/feedback/smart-assign', data)
}
