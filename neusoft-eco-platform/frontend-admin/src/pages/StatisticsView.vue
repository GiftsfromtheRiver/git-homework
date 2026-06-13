<template>
  <div class="statistics-view">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>AQI统计分析</span>
          <div class="filter-bar">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="margin-right: 12px"
            />
            <el-select v-model="dimension" placeholder="统计维度" style="width: 120px; margin-right: 12px">
              <el-option label="按日" value="day" />
              <el-option label="按月" value="month" />
              <el-option label="按季度" value="quarter" />
            </el-select>
            <el-button type="primary" @click="loadStatistics">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
          </div>
        </div>
      </template>

      <!-- 汇总指标卡片 -->
      <el-row :gutter="20" style="margin-bottom: 24px">
        <el-col :span="6">
          <el-statistic title="平均AQI" :value="summary.avgAqi || '--'" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="最大AQI" :value="summary.maxAqi || '--'" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="最小AQI" :value="summary.minAqi || '--'" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="反馈总数" :value="summary.total || 0" />
        </el-col>
      </el-row>

      <!-- 趋势表格 -->
      <el-table :data="trendData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="date" label="时间" width="180" />
        <el-table-column prop="avgAqi" label="平均AQI" width="120" align="center" />
        <el-table-column prop="maxAqi" label="最大AQI" width="120" align="center" />
        <el-table-column prop="minAqi" label="最小AQI" width="120" align="center" />
        <el-table-column prop="count" label="反馈数" width="100" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAqiStatistics } from '@/api/feedback'

const loading = ref(false)
const dateRange = ref(null)
const dimension = ref('day')
const summary = reactive({
  avgAqi: null,
  maxAqi: null,
  minAqi: null,
  total: null
})
const trendData = ref([])

onMounted(() => {
  loadStatistics()
})

async function loadStatistics() {
  loading.value = true
  try {
    const params = { dimension: dimension.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getAqiStatistics(params)
    const data = res.data || {}
    summary.avgAqi = data.avgAqi
    summary.maxAqi = data.maxAqi
    summary.minAqi = data.minAqi
    summary.total = data.total
    trendData.value = data.trend || []
  } catch (e) {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter-bar {
  display: flex;
  align-items: center;
}
</style>
