<template>
  <div class="feedback-detail">
    <el-page-header @back="router.push('/feedback')" title="返回列表">
      <template #content>反馈详情 #{{ route.params.id }}</template>
    </el-page-header>

    <el-card shadow="never" style="margin-top: 16px" v-loading="loading">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="反馈ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.status)" size="small">
            {{ statusText(detail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="网格地址" :span="2">{{ detail.gridAddress }}</el-descriptions-item>
        <el-descriptions-item label="AQI数值">
          <el-tag :type="getAqiTagType(detail.aqiValue)">{{ detail.aqiValue }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="反馈时间">{{ detail.feedbackTime }}</el-descriptions-item>
        <el-descriptions-item label="PM2.5">{{ detail.pm25 || '-' }}</el-descriptions-item>
        <el-descriptions-item label="PM10">{{ detail.pm10 || '-' }}</el-descriptions-item>
        <el-descriptions-item label="SO₂">{{ detail.so2 || '-' }}</el-descriptions-item>
        <el-descriptions-item label="NO₂">{{ detail.no2 || '-' }}</el-descriptions-item>
        <el-descriptions-item label="CO">{{ detail.co || '-' }}</el-descriptions-item>
        <el-descriptions-item label="O₃">{{ detail.o3 || '-' }}</el-descriptions-item>
        <el-descriptions-item label="指派网格员ID">{{ detail.assignGridMemberId || '未指派' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref({})

onMounted(() => {
  loadDetail()
})

async function loadDetail() {
  loading.value = true
  try {
    const res = await request.get(`/admin/feedback/${route.params.id}`)
    detail.value = res.data || {}
  } catch (e) {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

function statusText(s) {
  return ['未指派', '已指派', '已确认'][s] || '未知'
}

function statusTagType(s) {
  return ['warning', 'primary', 'success'][s] || 'info'
}

function getAqiTagType(v) {
  if (!v) return 'info'
  if (v <= 50) return 'success'
  if (v <= 100) return ''
  if (v <= 150) return 'warning'
  return 'danger'
}
</script>
