<template>
  <div class="task-detail-page">
    <van-nav-bar
      title="任务详情"
      left-arrow
      @click-left="onBack"
      :border="false"
    />
    
    <div v-if="taskDetail" class="content">
      <!-- 基础信息卡片 -->
      <div class="info-card">
        <div class="card-title">
          <van-icon name="info-o" size="16" color="#1989fa" />
          <span>反馈信息</span>
        </div>
        <div class="info-row">
          <span class="label">任务编号</span>
          <span class="value">#{{ taskDetail.feedbackId }}</span>
        </div>
        <div class="info-row">
          <span class="label">网格地址</span>
          <span class="value address">{{ taskDetail.gridAddress }}</span>
        </div>
        <div class="info-row">
          <span class="label">反馈时间</span>
          <span class="value">{{ formatFullTime(taskDetail.feedbackTime) }}</span>
        </div>
        <div class="info-row">
          <span class="label">任务状态</span>
          <van-tag :type="getStatusType(taskDetail.status)" size="small">
            {{ taskDetail.statusText }}
          </van-tag>
        </div>
      </div>
      
      <!-- 反馈AQI卡片 -->
      <div class="aqi-card" :style="{ borderLeftColor: feedbackAqiLevel.color }">
        <div class="aqi-main">
          <div class="aqi-label">反馈 AQI</div>
          <div class="aqi-value" :style="{ color: feedbackAqiLevel.color }">
            {{ taskDetail.aqiValue }}
          </div>
          <div class="aqi-level" :style="{ backgroundColor: feedbackAqiLevel.color }">
            {{ feedbackAqiLevel.level }}
          </div>
        </div>
      </div>
      
      <!-- 填写实测数据 -->
      <div class="form-card" v-if="taskDetail.status === 1">
        <div class="card-title">
          <van-icon name="edit" size="16" color="#1989fa" />
          <span>填写实测数据</span>
        </div>
        
        <van-form @submit="onSubmit">
          <van-cell-group inset class="form-group">
            <van-field
              v-model.number="form.pm25"
              type="digit"
              label="PM2.5"
              placeholder="请输入PM2.5浓度"
              :rules="[{ required: true, message: '请输入PM2.5浓度' }]"
            >
              <template #right-text>μg/m³</template>
            </van-field>
            
            <van-field
              v-model.number="form.pm10"
              type="digit"
              label="PM10"
              placeholder="请输入PM10浓度"
              :rules="[{ required: true, message: '请输入PM10浓度' }]"
            >
              <template #right-text>μg/m³</template>
            </van-field>
            
            <van-field
              v-model.number="form.so2"
              type="digit"
              label="SO₂"
              placeholder="请输入SO₂浓度"
              :rules="[{ required: true, message: '请输入SO₂浓度' }]"
            >
              <template #right-text>μg/m³</template>
            </van-field>
            
            <van-field
              v-model.number="form.no2"
              type="digit"
              label="NO₂"
              placeholder="请输入NO₂浓度"
              :rules="[{ required: true, message: '请输入NO₂浓度' }]"
            >
              <template #right-text>μg/m³</template>
            </van-field>
            
            <van-field
              v-model.number="form.co"
              type="digit"
              label="CO"
              placeholder="请输入CO浓度"
              :rules="[{ required: true, message: '请输入CO浓度' }]"
            >
              <template #right-text>mg/m³</template>
            </van-field>
            
            <van-field
              v-model.number="form.o3"
              type="digit"
              label="O₃"
              placeholder="请输入O₃浓度"
              :rules="[{ required: true, message: '请输入O₃浓度' }]"
            >
              <template #right-text>μg/m³</template>
            </van-field>
          </van-cell-group>
          
          <!-- 计算结果展示 -->
          <div v-if="calculatedResult.aqi > 0" class="result-card">
            <div class="result-title">
              <van-icon name="bar-chart-o" size="16" color="#07c160" />
              <span>计算结果</span>
            </div>
            
            <div class="result-aqi">
              <div class="result-aqi-value" :style="{ color: calculatedResult.aqiLevel.color }">
                {{ calculatedResult.aqi }}
              </div>
              <div class="result-aqi-level" :style="{ backgroundColor: calculatedResult.aqiLevel.color }">
                {{ calculatedResult.aqiLevel.level }}
              </div>
            </div>
            
            <div v-if="calculatedResult.primaryPollutant" class="primary-pollutant">
              首要污染物：{{ calculatedResult.primaryPollutant }}
            </div>
            
            <div class="pollutant-levels">
              <div
                v-for="item in pollutantLevelList"
                :key="item.key"
                class="pollutant-item"
              >
                <span class="pollutant-name">{{ item.name }}</span>
                <span class="pollutant-iaqi">{{ item.iaqi }}</span>
                <span class="pollutant-level-tag" :style="{ backgroundColor: item.level.color }">
                  {{ item.level.level }}
                </span>
              </div>
            </div>
          </div>
          
          <!-- 一致性确认 -->
          <div class="confirm-section">
            <div class="confirm-label">实测结果与反馈是否一致</div>
            <van-radio-group v-model="form.confirmResult" direction="horizontal">
              <van-radio :name="1">一致</van-radio>
              <van-radio :name="2">不一致</van-radio>
            </van-radio-group>
          </div>
          
          <div class="submit-btn-wrap">
            <van-button round block type="primary" native-type="submit" :loading="submitting">
              提交实测数据
            </van-button>
          </div>
        </van-form>
      </div>
      
      <!-- 已完成状态提示 -->
      <div v-else-if="taskDetail.status === 2" class="completed-tip">
        <van-icon name="checked" size="48" color="#07c160" />
        <p>该任务已完成检测</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getTaskDetail, submitMeasureData } from '@/api/grid'
import { calculateAQI, getAQILevel, getPollutantName } from '@/utils/aqi'

const route = useRoute()
const router = useRouter()

const taskDetail = ref(null)
const submitting = ref(false)

const form = ref({
  pm25: null,
  pm10: null,
  so2: null,
  no2: null,
  co: null,
  o3: null,
  confirmResult: 1
})

// 反馈AQI等级
const feedbackAqiLevel = computed(() => {
  if (!taskDetail.value || taskDetail.value.aqiValue == null) {
    return { level: '未知', color: '#999' }
  }
  return getAQILevel(taskDetail.value.aqiValue)
})

// 计算AQI结果
const calculatedResult = computed(() => {
  const data = {
    pm25: form.value.pm25,
    pm10: form.value.pm10,
    so2: form.value.so2,
    no2: form.value.no2,
    co: form.value.co,
    o3: form.value.o3
  }
  return calculateAQI(data)
})

// 各污染物等级列表
const pollutantLevelList = computed(() => {
  const list = []
  const pollutants = ['pm25', 'pm10', 'so2', 'no2', 'co', 'o3']
  
  pollutants.forEach(p => {
    if (calculatedResult.value.iaqiValues[p] !== undefined) {
      list.push({
        key: p,
        name: getPollutantName(p),
        iaqi: calculatedResult.value.iaqiValues[p],
        level: calculatedResult.value.levels[p]
      })
    }
  })
  
  return list
})

onMounted(() => {
  loadTaskDetail()
})

const loadTaskDetail = async () => {
  try {
    const id = route.params.id
    const res = await getTaskDetail(id)
    taskDetail.value = res.data
  } catch (error) {
    console.error('获取任务详情失败:', error)
  }
}

const getStatusType = (status) => {
  if (status === 1) return 'warning'
  if (status === 2) return 'success'
  return 'default'
}

const formatFullTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const onBack = () => {
  router.back()
}

const onSubmit = async () => {
  if (!form.value.confirmResult) {
    showToast('请选择实测结果是否一致')
    return
  }
  
  submitting.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('gridMemberInfo') || '{}')
    
    const data = {
      feedbackId: taskDetail.value.feedbackId,
      gridMemberId: userInfo.gridMemberId,
      aqiValue: calculatedResult.value.aqi,
      pm25: form.value.pm25,
      pm10: form.value.pm10,
      so2: form.value.so2,
      no2: form.value.no2,
      co: form.value.co,
      o3: form.value.o3,
      confirmResult: form.value.confirmResult
    }
    
    await submitMeasureData(data)
    showToast('提交成功')
    
    setTimeout(() => {
      router.back()
    }, 1000)
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.task-detail-page {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 40px;
}

.content {
  padding: 12px;
}

.info-card,
.aqi-card,
.form-card,
.result-card {
  background: white;
  border-radius: 12px;
  margin-bottom: 12px;
  overflow: hidden;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #323233;
  padding: 16px 16px 12px;
}

.info-card {
  padding: 0 16px 16px;
}

.info-card .card-title {
  padding-left: 0;
  padding-right: 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f2f3f5;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  font-size: 14px;
  color: #969799;
}

.info-row .value {
  font-size: 14px;
  color: #323233;
  text-align: right;
  max-width: 60%;
}

.info-row .value.address {
  line-height: 1.4;
}

.aqi-card {
  padding: 20px;
  border-left: 4px solid #1989fa;
}

.aqi-main {
  display: flex;
  align-items: center;
  gap: 16px;
}

.aqi-label {
  font-size: 14px;
  color: #969799;
}

.aqi-value {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}

.aqi-level {
  padding: 4px 12px;
  border-radius: 12px;
  color: white;
  font-size: 12px;
}

.form-group {
  margin: 0 !important;
}

.result-card {
  margin: 16px 12px;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 8px;
}

.result-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 12px;
}

.result-aqi {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.result-aqi-value {
  font-size: 32px;
  font-weight: 700;
}

.result-aqi-level {
  padding: 4px 12px;
  border-radius: 12px;
  color: white;
  font-size: 12px;
}

.primary-pollutant {
  font-size: 13px;
  color: #646566;
  margin-bottom: 12px;
}

.pollutant-levels {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pollutant-item {
  display: flex;
  align-items: center;
  gap: 6px;
  background: white;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 12px;
}

.pollutant-name {
  color: #646566;
  font-weight: 500;
}

.pollutant-iaqi {
  color: #323233;
  font-weight: 600;
}

.pollutant-level-tag {
  padding: 2px 6px;
  border-radius: 4px;
  color: white;
  font-size: 10px;
}

.confirm-section {
  padding: 16px;
}

.confirm-label {
  font-size: 14px;
  color: #323233;
  margin-bottom: 12px;
  font-weight: 500;
}

.submit-btn-wrap {
  padding: 16px;
}

.completed-tip {
  text-align: center;
  padding: 60px 20px;
  color: #969799;
}

.completed-tip .van-icon {
  margin-bottom: 12px;
}

.completed-tip p {
  margin: 0;
  font-size: 14px;
}
</style>
