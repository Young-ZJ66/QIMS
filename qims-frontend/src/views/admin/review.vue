<template>
  <div class="review-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>报告审核签发</span>
          <el-button type="primary" plain :loading="loading" @click="fetchData">刷新</el-button>
        </div>
      </template>

      <el-form :inline="true" class="query-form">
        <el-form-item label="委托单号">
          <el-input v-model="query.delegationNo" placeholder="输入委托单号" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="样品名称">
          <el-input v-model="query.sampleName" placeholder="输入样品名称" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 160px">
            <el-option label="待收样" :value="0" />
            <el-option label="检测中" :value="1" />
            <el-option label="审核中" :value="2" />
            <el-option label="已出报告" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="filteredList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="delegationNo" label="委托单号" min-width="160" />
        <el-table-column prop="sampleName" label="样品名称" min-width="160" />
        <el-table-column prop="status" label="流程状态" min-width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">待收样</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="warning">检测中</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="primary">审核中</el-tag>
            <el-tag v-else type="success">已出报告</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTimeText" label="提交时间" min-width="160" />
        <el-table-column prop="reportNo" label="报告编号" min-width="160">
          <template #default="scope">
            <span v-if="scope.row.reportNo" style="font-weight: 600">{{ scope.row.reportNo }}</span>
            <el-tag v-else type="info">未签发</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="issueTimeText" label="签发时间" min-width="160" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button
              v-if="!scope.row.reportId"
              type="primary"
              size="small"
              @click="openIssueDialog(scope.row)"
            >
              签发报告
            </el-button>
            <el-button v-else type="warning" size="small" @click="openEditDialog(scope.row)">重新签发</el-button>
            <el-button
              v-if="scope.row.reportFileUrl"
              type="primary"
              link
              @click="openReport(scope.row.reportFileUrl)"
            >
              <el-icon><Document /></el-icon> 查看报告
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '签发新报告' : '重新签发报告'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="委托单号">
          <el-input v-model="form.delegationNo" disabled />
        </el-form-item>
        <el-form-item label="样品名称">
          <el-input v-model="form.sampleName" disabled />
        </el-form-item>

        <!-- 实测数据展示 -->
        <el-form-item label="盲样实测数据">
          <el-table :data="inspectionRecords" border style="width: 100%" v-loading="recordLoading">
            <el-table-column prop="blindSampleCode" label="盲样编号" width="160" />
            <el-table-column prop="itemName" label="检测项目" width="150" />
            <el-table-column label="实测值/定性文本">
              <template #default="scope">
                <span v-if="scope.row.measuredText">{{ scope.row.measuredText }}</span>
                <span v-else>{{ scope.row.measuredValue }}</span>
              </template>
            </el-table-column>
            <el-table-column label="现场照片" width="100" align="center">
              <template #default="scope">
                <el-image 
                  v-if="scope.row.photoUrl" 
                  style="width: 50px; height: 50px" 
                  :src="'/api' + scope.row.photoUrl" 
                  :preview-src-list="['/api' + scope.row.photoUrl]" 
                  preview-teleported
                />
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="系统初判" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.result === 1 ? 'success' : 'danger'">
                  {{ scope.row.result === 1 ? '合格' : '不合格' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>

        <el-divider />

        <el-form-item label="报告编号" prop="reportNo">
          <el-input v-model="form.reportNo" placeholder="例如：R202604140001" />
        </el-form-item>
        <el-form-item label="综合结论" prop="finalConclusion">
          <el-radio-group v-model="form.finalConclusion">
            <el-radio :value="1">合格</el-radio>
            <el-radio :value="0">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="签发时间">
          <el-input v-model="form.issueTimeText" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitIssue">提交签发</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const inspectionRecords = ref([])
const recordLoading = ref(false)

const delegationList = ref([])
const reportList = ref([])

const query = reactive({
  delegationNo: '',
  sampleName: '',
  status: null,
  applied: {
    delegationNo: '',
    sampleName: '',
    status: null
  }
})

const applyFilter = () => {
  query.applied.delegationNo = query.delegationNo || ''
  query.applied.sampleName = query.sampleName || ''
  query.applied.status = query.status
}

const resetFilter = () => {
  query.delegationNo = ''
  query.sampleName = ''
  query.status = null
  applyFilter()
}

const formatDateTime = (value) => {
  if (!value) return ''
  const str = String(value)
  if (str.includes('T')) return str.replace('T', ' ').slice(0, 19)
  if (str.length >= 19) return str.slice(0, 19)
  return str
}

const nowLocalDateTime = () => {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const generateReportNo = () => {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  const ymd = `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}`
  const suffix = String(Math.floor(Math.random() * 10000)).padStart(4, '0')
  return `R${ymd}${suffix}`
}

const combinedList = computed(() => {
  const reportByDelegation = new Map((reportList.value || []).map(r => [r.delegationId, r]))
  return (delegationList.value || []).map(d => {
    const r = reportByDelegation.get(d.id)
    return {
      ...d,
      submitTimeText: formatDateTime(d.submitTime),
      reportId: r?.id,
      reportNo: r?.reportNo,
      issueTimeText: formatDateTime(r?.issueTime),
      reportFileUrl: r?.reportFileUrl,
      finalConclusion: r?.finalConclusion,
      reviewerId: r?.reviewerId
    }
  })
})

const filteredList = computed(() => {
  const delegationNo = (query.applied.delegationNo || '').trim()
  const sampleName = (query.applied.sampleName || '').trim()
  const status = query.applied.status

  return combinedList.value.filter(item => {
    const okDelegationNo = delegationNo ? String(item.delegationNo || '').includes(delegationNo) : true
    const okSampleName = sampleName ? String(item.sampleName || '').includes(sampleName) : true
    const okStatus = status === null || status === undefined ? true : item.status === status
    return okDelegationNo && okSampleName && okStatus
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
  window.open('/api' + url, '_blank')
}

const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref(null)

const form = reactive({
  reportId: null,
  delegationId: null,
  delegationNo: '',
  sampleName: '',
  reportNo: '',
  reviewerId: null,
  finalConclusion: 1,
  reportFileUrl: '',
  issueTime: '',
  issueTimeText: ''
})

const rules = reactive({
  reportNo: [{ required: true, message: '请填写报告编号', trigger: 'blur' }],
  finalConclusion: [{ required: true, message: '请选择综合结论', trigger: 'change' }]
})

const currentReviewerId = computed(() => {
  const raw = localStorage.getItem('userId')
  const parsed = Number(raw)
  return Number.isFinite(parsed) ? parsed : 1
})

const openIssueDialog = (row) => {
  dialogMode.value = 'create'
  const issueTime = nowLocalDateTime()
  Object.assign(form, {
    reportId: null,
    delegationId: row.id,
    delegationNo: row.delegationNo,
    sampleName: row.sampleName,
    reportNo: generateReportNo(),
    reviewerId: currentReviewerId.value,
    finalConclusion: 1,
    reportFileUrl: '',
    issueTime,
    issueTimeText: formatDateTime(issueTime)
  })
  dialogVisible.value = true

  // 请求实测数据
  recordLoading.value = true
  request.get(`/biz-inspection-record/delegation/${row.id}`).then(res => {
    inspectionRecords.value = res || []
    
    // 如果有不合格的项，自动将总结论设为不合格
    const hasFail = inspectionRecords.value.some(r => r.result === 0)
    if (hasFail) {
      form.finalConclusion = 0
    }
  }).finally(() => {
    recordLoading.value = false
  })
}

const openEditDialog = (row) => {
  dialogMode.value = 'edit'
  const issueTime = nowLocalDateTime()
  Object.assign(form, {
    reportId: row.reportId,
    delegationId: row.id,
    delegationNo: row.delegationNo,
    sampleName: row.sampleName,
    reportNo: row.reportNo,
    reviewerId: row.reviewerId || currentReviewerId.value,
    finalConclusion: row.finalConclusion ?? 1,
    reportFileUrl: row.reportFileUrl || '',
    issueTime,
    issueTimeText: formatDateTime(issueTime)
  })
  dialogVisible.value = true

  // 请求实测数据
  recordLoading.value = true
  request.get(`/biz-inspection-record/delegation/${row.id}`).then(res => {
    inspectionRecords.value = res || []
    
    // 如果有不合格的项，自动将总结论设为不合格
    const hasFail = inspectionRecords.value.some(r => r.result === 0)
    if (hasFail) {
      form.finalConclusion = 0
    }
  }).finally(() => {
    recordLoading.value = false
  })
}

const submitIssue = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        id: form.reportId,
        reportNo: form.reportNo,
        delegationId: form.delegationId,
        reviewerId: form.reviewerId,
        finalConclusion: form.finalConclusion,
        reportFileUrl: form.reportFileUrl || null,
        issueTime: form.issueTime
      }

      if (dialogMode.value === 'create') {
        await request.post('/biz-report', payload)
      } else {
        await request.put('/biz-report', payload)
      }

      await request.put('/biz-delegation', { id: form.delegationId, status: 3 })
      ElMessage.success('报告签发成功，PDF已自动生成！')
      dialogVisible.value = false
      await fetchData()
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.review-container {
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
