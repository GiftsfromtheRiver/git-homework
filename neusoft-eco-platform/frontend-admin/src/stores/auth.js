import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { adminLogin } from '@/api/admin'

export const useAuthStore = defineStore('auth', () => {
  // 状态：从localStorage恢复
  const token = ref(localStorage.getItem('adminToken') || '')
  const adminInfo = ref(JSON.parse(localStorage.getItem('adminInfo') || 'null'))

  // 计算属性
  const isLogin = computed(() => !!token.value)

  /**
   * 登录
   */
  async function login(loginData) {
    const res = await adminLogin(loginData)
    const data = res.data

    token.value = data.token
    adminInfo.value = {
      adminId: data.adminId,
      adminCode: data.adminCode,
      role: data.role
    }

    // 持久化
    localStorage.setItem('adminToken', data.token)
    localStorage.setItem('adminInfo', JSON.stringify(adminInfo.value))

    return data
  }

  /**
   * 登出
   */
  function logout() {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminInfo')
  }

  return {
    token,
    adminInfo,
    isLogin,
    login,
    logout
  }
})
