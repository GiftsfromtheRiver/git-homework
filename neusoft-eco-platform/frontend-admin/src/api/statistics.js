import request from './request'

/**
 * 获取大屏总览数据
 */
export function getDashboardData() {
  return request.get('/statistics/dashboard')
}
