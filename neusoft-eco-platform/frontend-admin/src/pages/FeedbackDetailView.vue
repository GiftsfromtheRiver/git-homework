<template>
  <div class="feedback-detail">
    <el-page-header @back="goBack" content="返回列表">
      <template #extra>
        <el-button
          type="primary"
          :disabled="detail.status !== 0"
          @click="scrollToAssign"
          v-if="detail.status === 0"
        >
          指派网格员
        </el-button>
      </template>
    </el-page-header>

    <el-card shadow="never" style="margin-top: 16px" v-loading="loading">
      <el-descriptions :column="2" border title="反馈基本信息">
        <el-descriptions-item label="反馈编号">
          #{{ detail.id }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.status)" size="small">
            {{ detail.statusText || '未知' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="反馈者" :span="1">
          {{ detail.supervisorName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话" :span="1">
          {{ detail.supervisorPhone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="城市" :span="1">
          {{ detail.cityName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="反馈时间" :span="1">
          {{ detail.feedbackTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="网格地址" :span="2">
          {{ detail.gridAddress || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-descriptions :column="3" border style="margin-top: 16px" title="空气质量数据">
        <el-descriptions-item label="AQI数值">
          <el-tag :type="getAqiTagType(detail.aqiValue)" effect="dark">
            {{ detail.aqiValue }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预估等级">
          <el-tag :type="getAqiTagType(detail.aqiValue)">
            {{ detail.aqiLevel || '-' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="PM2.5">
          {{ detail.pm25 || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="PM10">
          {{ detail.pm10 || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="SO₂">
          {{ detail.so2 || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="NO₂">
          {{ detail.no2 || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="CO">
          {{ detail.co || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="O₃">
          {{ detail.o3 || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-descriptions :column="2" border style="margin-top: 16px" title="指派信息">
        <el-descriptions-item label="指派网格员">
          {{ detail.assignGridMemberName || '未指派' }}
        </el-descriptions-item>
        <el-descriptions-item label="网格员ID">
          {{ detail.assignGridMemberId || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 智能指派区域（仅未指派时显示） -->
    <el-card shadow="never" style="margin-top: 16px" v-if="detail.status === 0" id="assign-section">
      <template #header>
        <div class="card-header">
          <span class="header-title">智能指派网格员</span>
          <el-tag type="info" size="small" v-if="assignInfo.cityName">
            所在城市：{{ assignInfo.cityName }}
          </el-tag>
        </div>
      </template>

      <div v-loading="assignLoading">
        <!-- 本市网格员区域 -->
        <div class="assign-section">
          <div class="section-header">
            <span class="section-title">
              <el-icon><LocationFilled /></el-icon>
              本市网格员
            </span>
            <el-tag 
              :type="assignInfo.hasLocalMember ? 'success' : 'warning'" 
              size="small"
              effect="light"
            >
              {{ assignInfo.hasLocalMember ? `${assignInfo.localMembers?.length || 0}人可指派` : '暂无人员' }}
            </el-tag>
          </div>

          <div v-if="assignInfo.localMembers && assignInfo.localMembers.length > 0" class="member-grid">
            <div 
              v-for="m in assignInfo.localMembers" 
              :key="m.id"
              class="member-card"
              :class="{ active: selectedLocalMemberId === m.id }"
              @click="selectLocalMember(m.id)"
            >
              <div class="member-avatar">{{ m.realName?.charAt(0) || '网' }}</div>
              <div class="member-content">
                <div class="member-name">{{ m.realName }}</div>
                <div class="member-phone">{{ m.phone }}</div>
                <div class="member-address">
                  <el-icon><Position /></el-icon>
                  {{ m.gridAddress }}
                </div>
              </div>
              <div class="member-check" v-if="selectedLocalMemberId === m.id">
                <el-icon color="#409eff" size="20"><CircleCheckFilled /></el-icon>
              </div>
            </div>
          </div>

          <div v-else class="empty-state">
            <el-empty description="本市暂无可用网格员" :image-size="60">
              <template #image>
                <el-icon size="48" color="#e6a23c"><Warning /></el-icon>
              </template>
            </el-empty>
          </div>

          <el-button 
            type="primary" 
            size="large"
            class="assign-btn"
            :disabled="!selectedLocalMemberId || submitting"
            @click="handleAssign(false)"
            v-if="assignInfo.localMembers && assignInfo.localMembers.length > 0"
          >
            <el-icon v-if="!submitting"><Check /></el-icon>
            {{ submitting ? '指派中...' : '确认指派（本市）' }}
          </el-button>
        </div>

        <!-- 分隔线 -->
        <div class="divider" v-if="assignInfo.otherProvinceMembers && assignInfo.otherProvinceMembers.length > 0">
          <span>跨省调派</span>
        </div>

        <!-- 外省调派区域 -->
        <div class="assign-section">
          <div class="section-header">
            <span class="section-title">
              <el-icon><Connection /></el-icon>
              外省网格员
            </span>
            <el-tag 
              :type="!assignInfo.hasLocalMember ? 'danger' : 'info'" 
              size="small"
              effect="light"
            >
              {{ !assignInfo.hasLocalMember ? '可跨省调派' : '本市有人员时禁用' }}
            </el-tag>
          </div>

          <div v-if="!assignInfo.hasLocalMember" class="cross-province-tip">
            <el-alert 
              title="本市无可用网格员，已自动解锁跨省调派权限" 
              type="warning" 
              :closable="false"
              show-icon
            />
          </div>

          <div 
            v-if="assignInfo.otherProvinceMembers && assignInfo.otherProvinceMembers.length > 0 && !assignInfo.hasLocalMember" 
            class="member-grid"
          >
            <div 
              v-for="m in assignInfo.otherProvinceMembers" 
              :key="m.id"
              class="member-card cross-province"
              :class="{ active: selectedOtherMemberId === m.id }"
              @click="selectOtherMember(m.id)"
            >
              <div class="member-avatar other">{{ m.realName?.charAt(0) || '网' }}</div>
              <div class="member-content">
                <div class="member-name">{{ m.realName }}</div>
                <div class="member-phone">{{ m.phone }}</div>
                <div class="member-address">
                  <el-icon><Position /></el-icon>
                  {{ m.gridAddress }}
                </div>
              </div>
              <div class="member-check" v-if="selectedOtherMemberId === m.id">
                <el-icon color="#e6a23c" size="20"><CircleCheckFilled /></el-icon>
              </div>
            </div>
          </div>

          <div v-else-if="assignInfo.hasLocalMember" class="disabled-state">
            <el-icon size="32" color="#c0c4cc"><Lock /></el-icon>
            <p>本市存在可用网格员，暂不可跨省调派</p>
            <span>请优先选择本市网格员进行指派</span>
          </div>

          <div v-else class="empty-state">
            <el-empty description="暂无外省网格员" :image-size="60" />
          </div>

          <el-button 
            type="warning" 
            size="large"
            class="assign-btn"
            :disabled="!selectedOtherMemberId || submitting || assignInfo.hasLocalMember"
            @click="handleAssign(true)"
            v-if="assignInfo.otherProvinceMembers && assignInfo.otherProvinceMembers.length > 0 && !assignInfo.hasLocalMember"
          >
            <el-icon v-if="!submitting"><Promotion /></el-icon>
            {{ submitting ? '指派中...' : '确认跨省指派' }}
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  LocationFilled, Position, CircleCheckFilled, Warning, 
  Lock, Check, Promotion, Connection 
} from '@element-plus/icons-vue'
import { getFeedbackDetail, getAssignInfo, smartAssignFeedback } from '@/api/admin'

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

onMounted(() => {
  loadDetail()
})

async function loadDetail() {
  const afId = route.params.afId
  if (!afId) {
    ElMessage.error('反馈编号不存在')
    return
  }
  loading.value = true
  try {
    const res = await getFeedbackDetail(afId)
    detail.value = res.data || {}
    
    // 如果未指派，加载指派信息
    if (detail.value.status === 0) {
      loadAssignInfo(afId)
    }
  } catch (e) {
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

async function loadAssignInfo(afId) {
  assignLoading.value = true
  try {
    const res = await getAssignInfo(afId)
    assignInfo.value = res.data || {}
  } catch (e) {
    console.error('加载指派信息失败', e)
    ElMessage.error('加载指派信息失败')
  } finally {
    assignLoading.value = false
  }
}

function selectLocalMember(id) {
  selectedLocalMemberId.value = id
  selectedOtherMemberId.value = null
}

function selectOtherMember(id) {
  selectedOtherMemberId.value = id
  selectedLocalMemberId.value = null
}

async function handleAssign(isCrossProvince) {
  const memberId = isCrossProvince ? selectedOtherMemberId.value : selectedLocalMemberId.value
  if (!memberId) {
    ElMessage.warning('请先选择网格员')
    return
  }

  const tipText = isCrossProvince 
    ? '确认要进行跨省指派吗？指派后将无法撤回。' 
    : '确认要将该反馈指派给此网格员吗？'

  try {
    await ElMessageBox.confirm(tipText, '确认指派', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: isCrossProvince ? 'warning' : 'primary'
    })
  } catch {
    return // 用户取消
  }

  submitting.value = true
  try {
    await smartAssignFeedback({
      feedbackId: route.params.afId,
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

function goBack() {
  router.push('/feedback')
}

function scrollToAssign() {
  const el = document.getElementById('assign-section')
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function statusTagType(status) {
  if (status === 0) return 'warning'
  if (status === 1) return 'primary'
  if (status === 2) return 'success'
  return 'info'
}

function getAqiTagType(aqi) {
  if (!aqi) return 'info'
  if (aqi <= 50) return 'success'
  if (aqi <= 100) return 'warning'
  if (aqi <= 150) return 'warning'
  return 'danger'
}
</script>

<style scoped>
.feedback-detail {
  padding-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.assign-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.section-title .el-icon {
  color: #409eff;
}

.member-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.member-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border: 2px solid #e4e7ed;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  background: #fff;
  position: relative;
}

.member-card:hover {
  border-color: #a0cfff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.member-card.active {
  border-color: #409eff;
  background-color: #ecf5ff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
}

.member-card.cross-province.active {
  border-color: #e6a23c;
  background-color: #fdf6ec;
  box-shadow: 0 4px 16px rgba(230, 162, 60, 0.2);
}

.member-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  flex-shrink: 0;
}

.member-avatar.other {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.member-content {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.member-phone {
  font-size: 13px;
  color: #606266;
  margin-bottom: 2px;
}

.member-address {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.member-check {
  position: absolute;
  top: 8px;
  right: 8px;
}

.assign-btn {
  width: 100%;
  border-radius: 8px;
  font-weight: 500;
}

.empty-state {
  padding: 20px 0;
}

.disabled-state {
  text-align: center;
  padding: 30px 20px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #909399;
}

.disabled-state p {
  margin: 10px 0 4px;
  font-size: 14px;
  color: #606266;
}

.disabled-state span {
  font-size: 12px;
  color: #909399;
}

.cross-province-tip {
  margin-bottom: 12px;
}

.divider {
  text-align: center;
  margin: 20px 0 16px;
  position: relative;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: calc(50% - 50px);
  height: 1px;
  background: #e4e7ed;
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}

.divider span {
  display: inline-block;
  padding: 0 16px;
  color: #909399;
  font-size: 13px;
  background: #fff;
  position: relative;
  z-index: 1;
}
</style>
