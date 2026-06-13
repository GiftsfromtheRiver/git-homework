<template>
  <div class="login-container">
    <div class="login-header">
      <div class="logo">
        <van-icon name="environment-o" size="48" color="#1989fa" />
      </div>
      <h2 class="title">NEPG 网格员端</h2>
      <p class="subtitle">环境公众监督平台</p>
    </div>
    
    <div class="login-form">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.username"
            name="username"
            label="账号"
            placeholder="请输入网格员账号"
            :rules="[{ required: true, message: '请输入账号' }]"
            left-icon="user-o"
          />
          <van-field
            v-model="form.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
            left-icon="lock"
          />
        </van-cell-group>
        
        <div class="login-btn-wrap">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            登录
          </van-button>
        </div>
      </van-form>
      
      <div class="tips">
        <p>测试账号：grid01 / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { gridLogin } from '@/api/grid'

const router = useRouter()
const loading = ref(false)

const form = ref({
  username: '',
  password: ''
})

const onSubmit = async (values) => {
  loading.value = true
  try {
    const res = await gridLogin(values)
    if (res.data.code === 200) {
      const data = res.data.data
      localStorage.setItem('gridToken', data.token)
      localStorage.setItem('gridMemberInfo', JSON.stringify(data))
      showToast('登录成功')
      setTimeout(() => {
        router.push('/')
      }, 500)
    } else {
      showToast(res.data.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-top: 80px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  width: 80px;
  height: 80px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.title {
  color: white;
  font-size: 24px;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.subtitle {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin: 0;
}

.login-form {
  padding: 0 16px;
}

.login-btn-wrap {
  margin-top: 32px;
  padding: 0 8px;
}

.tips {
  margin-top: 24px;
  text-align: center;
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

.tips p {
  margin: 4px 0;
}
</style>
