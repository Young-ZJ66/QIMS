<template>
  <div class="report-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>报告查询</span>
          <el-button type="primary" plain :loading="loading" @click="fetchData">刷新</el-button>
        </div>
      </template>

      <el-form :inline="true" class="query-form">
        <el-form-item label="报告编号">
          <el-input v-model="query.reportNo" placeholder="输入报告编号" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="委托单号">
          <el-input v-model="query.delegationNo" placeholder="输入委托单号" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="filteredList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="reportNo" label="报告编号" width="200" />
        <el-table-column prop="delegationNo" label="委托单号" width="200" />
        <el-table-column prop="sampleName" label="样品名称" min-width="160" />
        <el-table-column prop="finalConclusionLabel" label="结论" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.finalConclusion === 1" type="success">合格</el-tag>
            <el-tag v-else type="danger">不合格</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="issueTimeText" label="签发时间" width="180" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button
              v-if="scope.row.reportFileUrl"
              type="primary"
              size="small"
              @click="openReport(scope.row.reportFileUrl)"
            >
              打开报告
            </el-button>
            <el-button type="info" size="small" plain @click="openDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="报告详情" width="640px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="报告编号">{{ currentDetail.reportNo }}</el-descriptions-item>
        <el-descriptions-item label="委托单号">{{ currentDetail.delegationNo }}</el-descriptions-item>
        <el-descriptions-item label="样品名称">{{ currentDetail.sampleName }}</el-descriptions-item>
        <el-descriptions-item label="结论">
          <el-tag v-if="currentDetail.finalConclusion === 1" type="success">合格</el-tag>
          <el-tag v-else type="danger">不合格</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="签发时间">{{ currentDetail.issueTimeText }}</el-descriptions-item>
        <el-descriptions-item label="报告地址">{{ currentDetail.reportFileUrl || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="currentDetail.reportFileUrl"
          type="primary"
          @click="openReport(currentDetail.reportFileUrl)"
        >
          打开报告
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const reportList = ref([])
const delegationList = ref([])

const query = reactive({
  reportNo: '',
  delegationNo: '',
  applied: {
    reportNo: '',
    delegationNo: ''
  }
})

const detailVisible = ref(false)
const currentDetail = ref({})

const clientId = computed(() => {
  const raw = localStorage.getItem('clientId')
  if (!raw) return null
  const parsed = Number(raw)
  return Number.isFinite(parsed) ? parsed : null
})

const applyFilter = () => {
  query.applied.reportNo = query.reportNo || ''
  query.applied.delegationNo = query.delegationNo || ''
}

const resetFilter = () => {
  query.reportNo = ''
  query.delegationNo = ''
  applyFilter()
}

const formatDateTime = (value) => {
  if (!value) return ''
  const str = String(value)
  if (str.includes('T')) return str.replace('T', ' ').slice(0, 19)
  if (str.length >= 19) return str.slice(0, 19)
  return str
}

const combinedList = computed(() => {
  const delegationMap = new Map((delegationList.value || []).map(d => [d.id, d]))
  let list = (reportList.value || []).map(r => {
    const d = delegationMap.get(r.delegationId) || {}
    return {
      ...r,
      delegationNo: d.delegationNo,
      sampleName: d.sampleName,
      issueTimeText: formatDateTime(r.issueTime)
    }
  })

  if (clientId.value != null) {
    list = list.filter(item => {
      const d = delegationMap.get(item.delegationId)
      return d && d.clientId === clientId.value
    })
  }

  return list
})

const filteredList = computed(() => {
  const reportNo = (query.applied.reportNo || '').trim()
  const delegationNo = (query.applied.delegationNo || '').trim()

  return combinedList.value.filter(item => {
    const okReportNo = reportNo ? String(item.reportNo || '').includes(reportNo) : true
    const okDelegationNo = delegationNo ? String(item.delegationNo || '').includes(delegationNo) : true
    return okReportNo && okDelegationNo
  })
})

const fetchData = async () => {
  loading.value = true
  try {
    const [delegations, reports] = await Promise.all([
      request.get('/biz-delegation'),
      request.get('/biz-report')
    ])
    delegationList.value = delegations || []
    reportList.value = reports || []
    applyFilter()
  } finally {
    loading.value = false
  }
}

const openReport = (url) => {
  // vite 配置了代理，可以直接访问 /api 代理下的资源
  window.open('/api' + url, '_blank')
}

const openDetail = (row) => {
  currentDetail.value = row || {}
  detailVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.report-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.query-form {
  margin-bottom: 16px;
}
</style>

