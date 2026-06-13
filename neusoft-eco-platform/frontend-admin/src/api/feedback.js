import request from './request'

/**
 * 获取待指派反馈列表
 */
export function getUnassignedFeedback(pageNum = 1, pageSize = 10) {
  return request.get('/admin/feedback/unassigned', {
    params: { pageNum, pageSize }
  })
}

/**
 * 指派反馈给网格员
 */
export function assignFeedback(data) {
  return request.put('/admin/feedback/assign', data)
}

/**
 * 获取网格员列表（用于指派下拉选择）
 */
export function getGridMembers(cityId) {
  return request.get('/admin/grid-members', {
    params: { cityId }
  })
}

/**
 * 获取AQI统计数据
 */
export function getAqiStatistics(params) {
  return request.get('/decision/statistics/aqi', { params })
}

/**
 * 获取反馈详情
 */
export function getFeedbackDetail(id) {
  return request.get(`/admin/feedback/${id}`)
}
