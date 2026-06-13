<template>
  <div class="feedback-list">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>公众监督反馈列表</span>
          <div class="filter-bar">
            <el-select
              v-model="filterStatus"
              placeholder="状态筛选"
              clearable
              style="width: 140px; margin-right: 12px"
              @change="loadData"
            >
              <el-option label="未指派" :value="0" />
              <el-option label="已指派" :value="1" />
              <el-option label="已确认" :value="2" />
            </el-select>
            <el-button type="primary" @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="tableData"
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="编号" width="80" align="center" />
        <el-table-column prop="supervisorName" label="监督员" width="100" align="center" />
        <el-table-column prop="cityName" label="城市" width="120" align="center" />
        <el-table-column prop="gridAddress" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="预估等级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getAqiTagType(row.aqiValue)" size="small">
              {{ row.aqiLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="feedbackTime" label="反馈日期" width="180" align="center" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getFeedbackPage } from '@/api/admin'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref(null)

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (filterStatus.value !== null && filterStatus.value !== '') {
      params.status = filterStatus.value
    }
    const res = await getFeedbackPage(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function viewDetail(row) {
  router.push(`/feedback/${row.id}`)
}

function handleSizeChange(size) {
  pageSize.value = size
  loadData()
}

function handleCurrentChange(page) {
  pageNum.value = page
  loadData()
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
