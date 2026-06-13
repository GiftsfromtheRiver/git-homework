<template>
  <div class="task-list-page">
    <van-nav-bar
      title="我的任务"
      :border="false"
      left-arrow
      @click-left="onClickLeft"
    >
      <template #right>
        <van-icon name="user-o" size="20" @click="showUserMenu = true" />
      </template>
    </van-nav-bar>
    
    <div class="user-info-card" v-if="userInfo">
      <div class="user-avatar">
        <van-icon name="manager-o" size="32" color="#1989fa" />
      </div>
      <div class="user-detail">
        <div class="user-name">{{ userInfo.realName }}</div>
        <div class="user-grid">负责区域：{{ userInfo.gridAddress }}</div>
      </div>
    </div>
    
    <van-tabs v-model:active="activeTab" sticky offset-top="46px">
      <van-tab title="待检测" name="pending">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="onLoad"
          >
            <div v-if="taskList.length === 0 && !loading" class="empty">
              <van-empty description="暂无待检测任务" />
            </div>
            <div
              v-for="item in taskList"
              :key="item.feedbackId"
              class="task-card"
              @click="goToDetail(item.feedbackId)"
            >
              <div class="task-header">
                <span class="task-id">#{{ item.feedbackId }}</span>
                <van-tag :type="getStatusType(item.status)" size="small">
                  {{ getStatusText(item.status) }}
                </van-tag>
              </div>
              <div class="task-address">
                <van-icon name="location-o" size="14" />
                <span>{{ item.gridAddress }}</span>
              </div>
              <div class="task-info">
                <div class="aqi-value">
                  <span class="label">反馈AQI</span>
                  <span class="value" :style="{ color: getAqiColor(item.aqiValue) }">
                    {{ item.aqiValue }}
                  </span>
                </div>
                <div class="task-time">
                  <van-icon name="clock-o" size="12" />
                  <span>{{ formatTime(item.feedbackTime) }}</span>
                </div>
              </div>
            </div>
          </van-list>
        </van-pull-refresh>
      </van-tab>
      
      <van-tab title="已完成" name="done">
        <van-pull-refresh v-model="refreshingDone" @refresh="onRefreshDone">
          <van-list
            v-model:loading="loadingDone"
            :finished="finishedDone"
            finished-text="没有更多了"
            @load="onLoadDone"
          >
            <div v-if="doneTaskList.length === 0 && !loadingDone" class="empty">
              <van-empty description="暂无已完成任务" />
            </div>
            <div
              v-for="item in doneTaskList"
              :key="item.feedbackId"
              class="task-card done"
              @click="goToDetail(item.feedbackId)"
            >
              <div class="task-header">
                <span class="task-id">#{{ item.feedbackId }}</span>
                <van-tag type="success" size="small">已完成</van-tag>
              </div>
              <div class="task-address">
                <van-icon name="location-o" size="14" />
                <span>{{ item.gridAddress }}</span>
              </div>
              <div class="task-info">
                <div class="aqi-value">
                  <span class="label">反馈AQI</span>
                  <span class="value" :style="{ color: getAqiColor(item.aqiValue) }">
                    {{ item.aqiValue }}
                  </span>
                </div>
                <div class="task-time">
                  <van-icon name="clock-o" size="12" />
                  <span>{{ formatTime(item.feedbackTime) }}</span>
                </div>
              </div>
            </div>
          </van-list>
        </van-pull-refresh>
      </van-tab>
    </van-tabs>
    
    <van-action-sheet
      v-model:show="showUserMenu"
      :actions="actions"
      cancel-text="取消"
      @select="onSelectAction"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getTaskList } from '@/api/grid'
import { getAQILevel } from '@/utils/aqi'

const router = useRouter()
const activeTab = ref('pending')
const showUserMenu = ref(false)

const userInfo = ref(null)

// 待检测任务
const taskList = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = 10

// 已完成任务
const doneTaskList = ref([])
const loadingDone = ref(false)
const finishedDone = ref(false)
const refreshingDone = ref(false)
const donePageNum = ref(1)

const actions = [
  { name: '退出登录', subname: '退出当前账号', type: 'danger' }
]

onMounted(() => {
  const info = localStorage.getItem('gridMemberInfo')
  if (info) {
    userInfo.value = JSON.parse(info)
  }
})

const getStatusType = (status) => {
  if (status === 1) return 'warning'
  if (status === 2) return 'success'
  return 'default'
}

const getStatusText = (status) => {
  if (status === 0) return '未指派'
  if (status === 1) return '待检测'
  if (status === 2) return '已完成'
  return '未知'
}

const getAqiColor = (aqi) => {
  return getAQILevel(aqi).color
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 待检测任务列表
const onLoad = async () => {
  try {
    const gridMemberId = userInfo.value?.gridMemberId
    if (!gridMemberId) {
      loading.value = false
      return
    }
    
    const res = await getTaskList({
      gridMemberId,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    
    const records = res.data.records || []
    // 过滤状态为1（待检测）的任务
    const pendingTasks = records.filter(item => item.status === 1)
    
    if (refreshing.value) {
      taskList.value = pendingTasks
      refreshing.value = false
    } else {
      taskList.value = [...taskList.value, ...pendingTasks]
    }
    
    loading.value = false
    
    if (records.length < pageSize.value) {
      finished.value = true
    } else {
      pageNum.value++
    }
  } catch (error) {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = () => {
  finished.value = false
  pageNum.value = 1
  taskList.value = []
  refreshing.value = true
  onLoad()
}

// 已完成任务列表
const onLoadDone = async () => {
  try {
    const gridMemberId = userInfo.value?.gridMemberId
    if (!gridMemberId) {
      loadingDone.value = false
      return
    }
    
    const res = await getTaskList({
      gridMemberId,
      pageNum: donePageNum.value,
      pageSize: pageSize.value
    })
    
    const records = res.data.records || []
    // 过滤状态为2（已完成）的任务
    const doneTasks = records.filter(item => item.status === 2)
    
    if (refreshingDone.value) {
      doneTaskList.value = doneTasks
      refreshingDone.value = false
    } else {
      doneTaskList.value = [...doneTaskList.value, ...doneTasks]
    }
    
    loadingDone.value = false
    
    if (records.length < pageSize.value) {
      finishedDone.value = true
    } else {
      donePageNum.value++
    }
  } catch (error) {
    loadingDone.value = false
    refreshingDone.value = false
  }
}

const onRefreshDone = () => {
  finishedDone.value = false
  donePageNum.value = 1
  doneTaskList.value = []
  refreshingDone.value = true
  onLoadDone()
}

const goToDetail = (id) => {
  router.push(`/task/${id}`)
}

const onClickLeft = () => {
  showUserMenu.value = true
}

const onSelectAction = (action) => {
  if (action.name === '退出登录') {
    showConfirmDialog({
      title: '提示',
      message: '确定要退出登录吗？',
    }).then(() => {
      localStorage.removeItem('gridToken')
      localStorage.removeItem('gridMemberInfo')
      showToast('已退出登录')
      setTimeout(() => {
        router.push('/login')
      }, 500)
    }).catch(() => {})
  }
}
</script>

<style scoped>
.task-list-page {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.user-info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px;
  display: flex;
  align-items: center;
  margin: 12px;
  border-radius: 12px;
  color: white;
}

.user-avatar {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.user-avatar .van-icon {
  color: white !important;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.user-grid {
  font-size: 13px;
  opacity: 0.9;
}

.task-card {
  background: white;
  margin: 12px;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.task-card.done {
  opacity: 0.8;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.task-id {
  font-size: 14px;
  color: #969799;
}

.task-address {
  display: flex;
  align-items: center;
  font-size: 15px;
  color: #323233;
  margin-bottom: 12px;
  line-height: 1.4;
}

.task-address .van-icon {
  margin-right: 6px;
  color: #1989fa;
  flex-shrink: 0;
}

.task-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebedf0;
}

.aqi-value {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.aqi-value .label {
  font-size: 12px;
  color: #969799;
}

.aqi-value .value {
  font-size: 20px;
  font-weight: 600;
}

.task-time {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #969799;
}

.task-time .van-icon {
  margin-right: 4px;
}

.empty {
  padding: 40px 0;
}
</style>
