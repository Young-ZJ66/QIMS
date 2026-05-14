<template>
  <div class="delegate-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>提交产品检验委托单</span>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            提交委托
          </el-button>
        </div>
      </template>

      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="120px" 
        class="delegate-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="样品名称" prop="sampleName">
              <el-input v-model="form.sampleName" placeholder="例如：婴幼儿罐装辅助食品" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格型号" prop="sampleSpecs">
              <el-input v-model="form.sampleSpecs" placeholder="例如：120/60" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="送样数量" prop="sampleQuantity">
              <el-input-number v-model="form.sampleQuantity" :min="1" :max="1000" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检测依据标准" prop="standardId">
              <el-select v-model="form.standardId" placeholder="请选择要求依据的检验标准" style="width: 100%">
                <el-option 
                  v-for="std in standardList" 
                  :key="std.id" 
                  :label="`${std.standardCode} (${std.standardName})`" 
                  :value="std.id" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注说明" prop="remark">
          <el-input 
            v-model="form.remark" 
            type="textarea" 
            :rows="3" 
            placeholder="如有特殊要求请在此说明" 
          />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 历史委托列表 -->
    <el-card shadow="never" class="mt-20">
      <template #header>
        <div class="card-header">
          <span>我的历史委托记录</span>
          <el-button type="success" plain @click="fetchHistory">刷新列表</el-button>
        </div>
      </template>

      <el-table :data="historyList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="delegationNo" label="委托单号" width="180" />
        <el-table-column prop="sampleName" label="样品名称" />
        <el-table-column prop="sampleQuantity" label="数量" width="80" align="center" />
        <el-table-column label="提交时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="当前状态" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">待收样</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="warning">检测中</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="primary">审核中</el-tag>
            <el-tag v-else-if="scope.row.status === 3" type="success">已出报告</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const formRef = ref(null)
const submitting = ref(false)
const loading = ref(false)
const historyList = ref([])
const standardList = ref([])

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 19)
}

const form = reactive({
  sampleName: '',
  sampleSpecs: '',
  sampleQuantity: 1,
  standardId: '',
  remark: ''
})

const rules = reactive({
  sampleName: [{ required: true, message: '请输入样品名称', trigger: 'blur' }],
  sampleQuantity: [{ required: true, message: '请填写送样数量', trigger: 'change' }],
  standardId: [{ required: true, message: '请选择检验依据标准', trigger: 'change' }]
})

// 提交委托单
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true
      request.post('/biz-delegation/submit', form).then(res => {
        ElMessage.success(`提交成功！自动生成单号: ${res}`)
        formRef.value.resetFields()
        fetchHistory()
      }).catch(() => {}).finally(() => {
        submitting.value = false
      })
    }
  })
}

// 获取国家标准列表
const fetchStandardList = () => {
  request.get('/std-standard').then(res => {
    // 只展示状态为现行(status=1)的标准
    standardList.value = (res || []).filter(item => item.status === 1)
  })
}

// 获取历史记录
const fetchHistory = () => {
  loading.value = true
  request.get('/biz-delegation').then(res => {
    historyList.value = res || []
  }).catch(() => {}).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  fetchStandardList()
  fetchHistory()
})
</script>

<style scoped>
.delegate-container {
  padding: 10px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.delegate-form {
  max-width: 900px;
  margin: 0 auto;
}
.mt-20 {
  margin-top: 20px;
}
</style>
