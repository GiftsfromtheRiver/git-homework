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
        <el-descriptions-item label="所属城市">{{ detail.cityName || '-' }}</el-descriptions-item>
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
        <el-descriptions-item label="指派网格员">
          <span v-if="detail.assignGridMemberName">{{ detail.assignGridMemberName }} (ID: {{ detail.assignGridMemberId }})</span>
          <span v-else>未指派</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 智能指派区域（仅未指派时显示） -->
    <el-card shadow="never" style="margin-top: 16px" v-if="detail.status === 0">
      <template #header>
        <div class="card-header">
          <span>智能指派网格员</span>
          <el-tag type="info" size="small">{{ assignInfo.cityName }}</el-tag>
        </div>
      </template>

      <div v-loading="assignLoading">
        <!-- 本市网格员区域 -->
        <div class="assign-section">
          <div class="section-title">
            <span>本市网格员</span>
            <el-tag v-if="assignInfo.hasLocalMember" type="success" size="small">可指派</el-tag>
            <el-tag v-else type="warning" size="small">暂无人员</el-tag>
          </div>
          <div v-if="assignInfo.localMembers && assignInfo.localMembers.length > 0">
            <el-radio-group v-model="selectedLocalMemberId" class="member-list">
              <el-radio v-for="m in assignInfo.localMembers" :key="m.id" :value="m.id" class="member-item">
                <div class="member-info">
                  <span class="member-name">{{ m.realName }}</span>
                  <span class="member-phone">{{ m.phone }}</span>
                  <span class="member-address">{{ m.gridAddress }}</span>
                </div>
              </el-radio>
            </el-radio-group>
            <el-button 
              type="primary" 
              class="assign-btn"
              :disabled="!selectedLocalMemberId || submitting"
              @click="handleAssign(false)"
            >
              {{ submitting ? '指派中...' : '确认指派（本市）' }}
            </el-button>
          </div>
          <div v-else class="empty-tip">
            <el-empty description="本市暂无可用网格员" :image-size="80">
              <el-button type="primary" size="small" @click="showOtherProvince = true">
                查看外省网格员
              </el-button>
            </el-empty>
          </div>
        </div>

        <!-- 外省调派区域 -->
        <div class="assign-section" v-if="showOtherProvince || !assignInfo.hasLocalMember">
          <div class="section-title">
            <span>外省调派网格员</span>
            <el-tag 
              :type="assignInfo.hasLocalMember ? 'info' : 'warning'" 
              size="small"
            >
              {{ assignInfo.hasLocalMember ? '仅本市无人员时可调用' : '可跨省调派' }}
            </el-tag>
          </div>
          
          <div v-if="!assignInfo.hasLocalMember">
            <el-alert 
              title="本市无可用网格员，已解锁跨省调派权限" 
              type="warning" 
              :closable="false"
              style="margin-bottom: 12px"
            />
            <el-radio-group v-model="selectedOtherMemberId" class="member-list">
              <el-radio 
                v-for="m in assignInfo.otherProvinceMembers" 
                :key="m.id" 
                :value="m.id" 
                class="member-item"
              >
                <div class="member-info">
                  <span class="member-name">{{ m.realName }}</span>
                  <span class="member-phone">{{ m.phone }}</span>
                  <span class="member-address">{{ m.gridAddress }}</span>
                </div>
              </el-radio>
            </el-radio-group>
            <el-button 
              type="warning" 
              class="assign-btn"
              :disabled="!selectedOtherMemberId || submitting"
              @click="handleAssign(true)"
            >
              {{ submitting ? '指派中...' : '确认跨省指派' }}
            </el-button>
          </div>
          <div v-else class="disabled-tip">
            <el-icon><Lock /></el-icon>
            <span>本市存在可用网格员，暂不可跨省调派</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const assignLoading = ref(false)
const submitting = ref(false)
const detail = ref({})
const assignInfo = ref({
  cityName: '',
  hasLocalMember: false,
  localMembers: [],
  otherProvinceMembers: []
})
const selectedLocalMemberId = ref(null)
const selectedOtherMemberId = ref(null)
const showOtherProvince = ref(false)

onMounted(() => {
  loadDetail()
})

async function loadDetail() {
  loading.value = true
  try {
    const res = await request.get(`/admin/feedback/${route.params.id}`)
    detail.value = res.data || {}
    
    // 如果未指派，加载指派信息
    if (detail.value.status === 0) {
      loadAssignInfo()
    }
  } catch (e) {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

async function loadAssignInfo() {
  assignLoading.value = true
  try {
    const res = await request.get(`/admin/feedback/${route.params.id}/assign-info`)
    assignInfo.value = res.data || {}
  } catch (e) {
    console.error('加载指派信息失败', e)
  } finally {
    assignLoading.value = false
  }
}

async function handleAssign(isCrossProvince) {
  const memberId = isCrossProvince ? selectedOtherMemberId.value : selectedLocalMemberId.value
  if (!memberId) {
    ElMessage.warning('请选择网格员')
    return
  }

  submitting.value = true
  try {
    await request.put('/admin/feedback/smart-assign', {
      feedbackId: route.params.id,
      gridMemberId: memberId,
      isCrossProvince: isCrossProvince
    })
    ElMessage.success('指派成功')
    loadDetail() // 刷新详情
  } catch (e) {
    // 拦截器已处理
  } finally {
    submitting.value = false
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

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.assign-section {
  margin-bottom: 24px;
}

.assign-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.member-item {
  margin: 0;
  padding: 12px 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.2s;
}

.member-item:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-left: 8px;
}

.member-name {
  font-weight: 500;
  color: #303133;
  min-width: 60px;
}

.member-phone {
  color: #606266;
  font-size: 14px;
}

.member-address {
  color: #909399;
  font-size: 13px;
}

.assign-btn {
  margin-top: 16px;
  width: 100%;
}

.empty-tip {
  padding: 20px 0;
}

.disabled-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 30px;
  background-color: #f5f7fa;
  border-radius: 8px;
  color: #909399;
  font-size: 14px;
}
</style>
