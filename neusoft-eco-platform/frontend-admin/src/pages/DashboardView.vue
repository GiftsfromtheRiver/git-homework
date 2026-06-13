<template>
  <div class="dashboard-view">
    <!-- 顶部标题栏 -->
    <div class="dashboard-header">
      <h1 class="dashboard-title">NEPV 环境监测大屏</h1>
      <div class="header-info">
        <span class="update-time">数据更新时间：{{ lastUpdateTime }}</span>
        <el-switch
          v-model="autoRefresh"
          active-text="自动刷新"
          inactive-text="手动刷新"
          style="margin-left: 20px"
        />
      </div>
    </div>

    <!-- 汇总指标卡片 -->
    <el-row :gutter="20" class="summary-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-label">反馈总数</div>
            <div class="stat-value">{{ summary.total || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-label">平均 AQI</div>
            <div class="stat-value" :style="{ color: getAqiColor(summary.avgAqi) }">
              {{ summary.avgAqi || '--' }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-label">最大 AQI</div>
            <div class="stat-value" :style="{ color: getAqiColor(summary.maxAqi) }">
              {{ summary.maxAqi || '--' }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-label">最小 AQI</div>
            <div class="stat-value" :style="{ color: getAqiColor(summary.minAqi) }">
              {{ summary.minAqi || '--' }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 三个柱状图 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <BarChart
            ref="pm25Chart"
            title="各省份 PM2.5 超标次数"
            :x-data="provinceNames"
            :y-data="pm25OverCounts"
            bar-color="#F56C6C"
            y-axis-name="超标次数"
          />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <BarChart
            ref="so2Chart"
            title="各省份 SO₂ 超标次数"
            :x-data="provinceNames"
            :y-data="so2OverCounts"
            bar-color="#E6A23C"
            y-axis-name="超标次数"
          />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <BarChart
            ref="coChart"
            title="各省份 CO 超标次数"
            :x-data="provinceNames"
            :y-data="coOverCounts"
            bar-color="#67C23A"
            y-axis-name="超标次数"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- AQI分布与趋势 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <BarChart
            title="AQI 等级分布"
            :x-data="aqiLevelNames"
            :y-data="aqiLevelCounts"
            bar-color="#409EFF"
            y-axis-name="数量"
          />
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <BarChart
            title="近12个月 AQI 趋势"
            :x-data="monthNames"
            :y-data="monthlyAvgAqi"
            bar-color="#909399"
            y-axis-name="平均 AQI"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { getDashboardData } from '@/api/statistics'
import BarChart from '@/components/BarChart.vue'

const autoRefresh = ref(true)
const lastUpdateTime = ref('--')
const provinceStats = ref([])
const aqiDistribution = ref([])
const monthlyTrend = ref([])
const summary = reactive({
  total: 0,
  avgAqi: null,
  maxAqi: null,
  minAqi: null
})

let refreshTimer = null
const REFRESH_INTERVAL = 5000 // 5秒轮询

// 计算属性：省份名称数组
const provinceNames = computed(() => {
  return provinceStats.value.map(item => item.provinceName || item.province_name || '未知')
})

// PM2.5超标次数
const pm25OverCounts = computed(() => {
  return provinceStats.value.map(item => item.pm25OverCount || item.pm25_over_count || 0)
})

// SO2超标次数
const so2OverCounts = computed(() => {
  return provinceStats.value.map(item => item.so2OverCount || item.so2_over_count || 0)
})

// CO超标次数
const coOverCounts = computed(() => {
  return provinceStats.value.map(item => item.coOverCount || item.co_over_count || 0)
})

// AQI等级名称
const aqiLevelNames = computed(() => {
  return aqiDistribution.value.map(item => item.level || item.description || '')
})

// AQI等级数量
const aqiLevelCounts = computed(() => {
  return aqiDistribution.value.map(item => item.count || 0)
})

// 月份名称
const monthNames = computed(() => {
  return monthlyTrend.value.map(item => item.month || '')
})

// 月度平均AQI
const monthlyAvgAqi = computed(() => {
  return monthlyTrend.value.map(item => item.avgAqi || item.avg_aqi || 0)
})

// 根据AQI值获取颜色
function getAqiColor(aqi) {
  if (!aqi) return '#909399'
  if (aqi <= 50) return '#67C23A'
  if (aqi <= 100) return '#E6A23C'
  if (aqi <= 150) return '#F56C6C'
  if (aqi <= 200) return '#C0392B'
  if (aqi <= 300) return '#8E44AD'
  return '#7B241C'
}

// 加载数据
async function loadData() {
  try {
    const res = await getDashboardData()
    const data = res.data || {}

    provinceStats.value = data.provincePollutantStats || []
    aqiDistribution.value = data.aqiDistribution || []
    monthlyTrend.value = data.monthlyTrend || []

    const sum = data.summary || {}
    summary.total = sum.total || sum.totalCount || 0
    summary.avgAqi = sum.avgAqi || sum.avg_aqi || null
    summary.maxAqi = sum.maxAqi || sum.max_aqi || null
    summary.minAqi = sum.minAqi || sum.min_aqi || null

    // 更新时间
    const now = new Date()
    lastUpdateTime.value = now.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (e) {
    console.error('加载大屏数据失败:', e)
  }
}

// 启动/停止自动刷新
function startAutoRefresh() {
  stopAutoRefresh()
  if (autoRefresh.value) {
    refreshTimer = setInterval(() => {
      loadData()
    }, REFRESH_INTERVAL)
  }
}

function stopAutoRefresh() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 监听自动刷新开关
function onAutoRefreshChange(val) {
  if (val) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

onMounted(() => {
  loadData()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.dashboard-view {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
}

.dashboard-title {
  margin: 0;
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-info {
  display: flex;
  align-items: center;
  color: #909399;
  font-size: 14px;
}

.update-time {
  font-family: 'Courier New', monospace;
}

.summary-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  text-align: center;
  padding: 10px 0;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  font-family: 'DIN', 'Arial', sans-serif;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
  height: 420px;
}

.chart-card :deep(.el-card__body) {
  height: calc(100% - 20px);
  padding: 15px;
}
</style>
