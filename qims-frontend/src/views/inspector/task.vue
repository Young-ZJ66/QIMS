<template>
  <div class="inspector-task-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>待检测任务列表 (盲样模式)</span>
          <div class="search-box">
            <el-input 
              v-model="searchCode" 
              placeholder="请用扫码枪扫描盲样条码或手动输入" 
              prefix-icon="Scan" 
              style="width: 300px; margin-right: 10px;"
              clearable
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" @click="handleSearch">搜索接单</el-button>
          </div>
        </div>
      </template>

      <el-table :data="taskList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="blindSampleCode" label="盲样条码 (唯一标识)" width="220">
          <template #default="scope">
            <span style="font-weight:bold; color:#e6a23c;">{{ scope.row.blindSampleCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="receiveTime" label="下发时间" width="180" />
        <el-table-column label="客户信息" align="center">
          <template #default>
            <el-tooltip content="为保证检测公正，盲样模式下客户端信息已脱敏" placement="top">
              <div style="display: inline-flex; align-items: center; justify-content: center; padding: 4px 10px; background-color: #f5f7fa; border: 1px dashed #dcdfe6; border-radius: 4px; color: #909399; font-size: 13px; cursor: not-allowed; user-select: none;">
                <el-icon style="margin-right: 4px;"><Lock /></el-icon>
                <span>盲样测试 (已脱敏)</span>
              </div>
            </el-tooltip>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 0" 
              type="primary" 
              size="small" 
              @click="openInspectDialog(scope.row)"
            >
              录入实测数据
            </el-button>
            <el-tag v-else type="success">已完成检验</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 数据录入弹窗 -->
    <el-dialog
      title="检测数据实测录入"
      v-model="dialogVisible"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-alert 
        :title="`当前检测样品: ${currentTask.blindSampleCode}`" 
        type="warning" 
        show-icon 
        :closable="false"
        style="margin-bottom: 20px"
      />
      
      <!-- 批量录入表单 -->
      <el-form label-position="top">
        <el-table :data="currentItems" border style="width: 100%" v-loading="itemsLoading">
          <el-table-column prop="itemName" label="检验项目" width="150" />
          <el-table-column label="标准要求" width="180">
            <template #default="scope">
              <span v-if="scope.row.judgeType === 1">区间: [{{ scope.row.minValue }}, {{ scope.row.maxValue }}] {{ scope.row.unit }}</span>
              <span v-else-if="scope.row.judgeType === 2">上限: &le; {{ scope.row.maxValue }} {{ scope.row.unit }}</span>
              <span v-else-if="scope.row.judgeType === 3">下限: &ge; {{ scope.row.minValue }} {{ scope.row.unit }}</span>
              <span v-else-if="scope.row.judgeType === 4">定性: {{ scope.row.textStandard }}</span>
            </template>
          </el-table-column>
          <el-table-column label="实测录入">
            <template #default="scope">
              <el-form-item :validate-status="scope.row.isError ? 'error' : ''" :error="scope.row.isError ? '请填入实测数据' : ''" style="margin-bottom: 0;">
                <!-- 数值型录入 -->
                <div v-if="scope.row.judgeType !== 4">
                  <el-input-number v-model="scope.row.measuredValue" :precision="3" :step="0.1" style="width: 150px" @change="scope.row.isError = false" />
                  <span style="margin-left: 5px; color:#888">{{ scope.row.unit }}</span>
                </div>
                <!-- 定性文本型录入 -->
                <div v-else>
                  <el-radio-group v-model="scope.row.measuredText" @change="scope.row.isError = false">
                    <el-radio :value="scope.row.textStandard">{{ scope.row.textStandard }}</el-radio>
                    <el-radio value="不符合要求">不符合要求</el-radio>
                  </el-radio-group>
                </div>
              </el-form-item>
            </template>
          </el-table-column>
          <el-table-column label="现场照片(可选)" width="100" align="center">
            <template #default="scope">
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="(res) => handleUploadSuccess(res, scope.row)"
                :on-error="handleUploadError"
                accept="image/*"
              >
                <el-image v-if="scope.row.photoUrl" :src="'/api' + scope.row.photoUrl" style="width: 40px; height: 40px" />
                <el-button v-else type="primary" link><el-icon><Upload /></el-icon>上传</el-button>
              </el-upload>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitData" :loading="submitting">
            批量提交系统判定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import request from '@/utils/request'

const taskList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const currentTask = ref({})
const searchCode = ref('')
const currentItems = ref([])
const itemsLoading = ref(false)

const uploadUrl = '/api/file/upload'
const uploadHeaders = {
  Authorization: 'Bearer ' + localStorage.getItem('token')
}

const handleUploadSuccess = (res, row) => {
  if (res.code === 200) {
    row.photoUrl = res.data
    ElMessage.success('上传照片成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传异常')
}

// 获取待检任务列表
const fetchTaskList = () => {
  loading.value = true
  request.get('/biz-sample-task').then(res => {
    let list = res || []
    if (searchCode.value) {
      list = list.filter(item => item.blindSampleCode.includes(searchCode.value))
    }
    taskList.value = list
  }).finally(() => {
    loading.value = false
  })
}

// 扫码/搜索
const handleSearch = () => {
  fetchTaskList()
}

// 打开录入弹窗
const openInspectDialog = (row) => {
  currentTask.value = row
  currentItems.value = []
  dialogVisible.value = true

  // 动态获取检测项目
  itemsLoading.value = true
  request.get(`/biz-sample-task/${row.id}/items`).then(res => {
    // 为每个项目初始化实测值字段
    currentItems.value = (res || []).map(item => ({
      ...item,
      measuredValue: undefined,
      measuredText: '',
      photoUrl: '',
      isError: false
    }))
  }).finally(() => {
    itemsLoading.value = false
  })
}

// 提交检测数据
const handleSubmitData = () => {
  // 校验是否漏填
  let hasEmpty = false;
  currentItems.value.forEach(item => {
    if (item.judgeType === 4) {
      if (!item.measuredText) {
        item.isError = true;
        hasEmpty = true;
      } else {
        item.isError = false;
      }
    } else {
      if (item.measuredValue === undefined || item.measuredValue === null) {
        item.isError = true;
        hasEmpty = true;
      } else {
        item.isError = false;
      }
    }
  });

  if (hasEmpty) {
    ElMessage.warning('请将所有的检验项目数据补充完整后再提交');
    return;
  }

  // 组装要提交的数据
  const submitData = currentItems.value.map(item => ({
    taskId: currentTask.value.id,
    itemId: item.id,
    measuredValue: item.measuredValue,
    measuredText: item.measuredText ? item.measuredText : null,
    photoUrl: item.photoUrl
  }))

  submitting.value = true
  request.post('/biz-inspection-record/submit-batch-data', submitData).then(res => {
    ElNotification({
      title: '批量录入成功',
      message: '系统已根据【国家标准限值】完成所有项目的自动判定！',
      type: 'success',
      duration: 4000
    })
    dialogVisible.value = false
    fetchTaskList()
  }).finally(() => {
    submitting.value = false
  })
}

onMounted(() => {
  fetchTaskList()
})
</script>

<style scoped>
.inspector-task-container {
  padding: 10px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.search-box {
  display: flex;
  align-items: center;
}
</style>
