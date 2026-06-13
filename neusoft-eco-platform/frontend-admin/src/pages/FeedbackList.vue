<template>
  <div class="feedback-list">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>反馈列表</span>
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

      <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="gridAddress" label="网格地址" min-width="200" />
        <el-table-column prop="aqiValue" label="AQI" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getAqiTagType(row.aqiValue)" size="small">
              {{ row.aqiValue }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="feedbackTime" label="反馈时间" width="180" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              @click="openAssignDialog(row)"
            >
              指派
            </el-button>
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
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 指派对话框 -->
    <el-dialog v-model="assignDialogVisible" title="指派网格员" width="460px">
      <el-form label-width="90px">
        <el-form-item label="反馈ID">
          <el-input :model-value="currentFeedback?.id" disabled />
        </el-form-item>
        <el-form-item label="网格地址">
          <el-input :model-value="currentFeedback?.gridAddress" disabled />
        </el-form-item>
        <el-form-item label="选择网格员">
          <el-select v-model="selectedGridMemberId" placeholder="请选择网格员" style="width: 100%">
            <el-option
              v-for="gm in gridMembers"
              :key="gm.id"
              :label="`${gm.realName} - ${gm.gridAddress}`"
              :value="gm.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="handleAssign">确认指派</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref(null)

const assignDialogVisible = ref(false)
const currentFeedback = ref(null)
const gridMembers = ref([])
const selectedGridMemberId = ref(null)
const assigning = ref(false)

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterStatus.value !== null && filterStatus.value !== '') {
      params.status = filterStatus.value
    }
    const res = await request.get('/admin/feedback/list', { params })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    // 静默处理
  } finally {
    loading.value = false
  }
}

function viewDetail(row) {
  router.push(`/feedback/${row.id}`)
}

async function openAssignDialog(row) {
  currentFeedback.value = row
  selectedGridMemberId.value = null
  assignDialogVisible.value = true
  // 加载网格员列表
  try {
    const res = await request.get('/admin/grid-members')
    gridMembers.value = res.data || []
  } catch (e) {
    gridMembers.value = []
  }
}

async function handleAssign() {
  if (!selectedGridMemberId.value) {
    ElMessage.warning('请选择网格员')
    return
  }
  assigning.value = true
  try {
    await request.put('/admin/feedback/assign', {
      adminId: 1, // 简化，实际从store取
      feedbackId: currentFeedback.value.id,
      gridMemberId: selectedGridMemberId.value
    })
    ElMessage.success('指派成功')
    assignDialogVisible.value = false
    loadData()
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    assigning.value = false
  }
}

function statusText(s) {
  return ['未指派', '已指派', '已确认'][s] || '未知'
}

function statusTagType(s) {
  return ['warning', 'primary', 'success'][s] || 'info'
}

function getAqiTagType(v) {
  if (v <= 50) return 'success'
  if (v <= 100) return ''
  if (v <= 150) return 'warning'
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
