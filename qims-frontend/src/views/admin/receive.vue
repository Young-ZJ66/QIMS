<template>
  <div class="receive-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>待收样委托单列表 (管理员视角)</span>
          <el-button type="primary" plain @click="fetchPendingList">刷新列表</el-button>
        </div>
      </template>

      <el-table :data="pendingList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="delegationNo" label="委托单号" width="180" />
        <el-table-column prop="clientName" label="委托方" min-width="160" />
        <el-table-column prop="sampleName" label="送检样品名称" min-width="140" />
        <el-table-column prop="sampleQuantity" label="数量" width="80" align="center" />
        <el-table-column label="提交时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.submitTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="220" align="center">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 0" 
              type="success" 
              size="small" 
              @click="openReceiveDialog(scope.row)"
            >
              确认收样并派发
            </el-button>
            <el-tag v-else type="info">已处理</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 收样弹窗 -->
    <el-dialog
      title="盲样生成与任务派发"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form :model="assignForm" label-width="120px">
        <el-form-item label="委托单号">
          <el-input v-model="currentDelegation.delegationNo" disabled />
        </el-form-item>
        <el-form-item label="样品名称">
          <el-input v-model="currentDelegation.sampleName" disabled />
        </el-form-item>
        <el-form-item label="指派检测员">
          <el-select v-model="assignForm.inspectorId" placeholder="请选择检测员">
            <el-option 
              v-for="user in inspectorList" 
              :key="user.id" 
              :label="`检测员: ${user.realName} (工号: ${user.username})`" 
              :value="user.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleReceive" :loading="assigning">
            确认收样并生成盲样
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const pendingList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const assigning = ref(false)
const currentDelegation = ref({})
const inspectorList = ref([])

const assignForm = ref({
  inspectorId: ''
})

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 19)
}

// 获取质检员列表
const fetchInspectorList = () => {
  request.get('/sys-user').then(res => {
    // 过滤出 roleId === 2 (质检员) 的用户
    inspectorList.value = (res || []).filter(user => user.roleId === 2)
  })
}

// 获取待收样列表
const fetchPendingList = () => {
  loading.value = true
  request.get('/biz-delegation').then(res => {
    // 过滤待收样数据
    pendingList.value = res.filter(item => item.status === 0)
  }).finally(() => {
    loading.value = false
  })
}

// 打开收样弹窗
const openReceiveDialog = (row) => {
  currentDelegation.value = row
  assignForm.value.inspectorId = ''
  dialogVisible.value = true
}

// 确认收样
const handleReceive = () => {
  if (!assignForm.value.inspectorId) {
    ElMessage.warning('请选择要指派的检测员')
    return
  }

  assigning.value = true
  // 调用盲样分发接口
  request.post('/biz-delegation/receive', null, {
    params: {
      delegationId: currentDelegation.value.id,
      inspectorId: assignForm.value.inspectorId,
      receiverId: Number(localStorage.getItem('userId')) || 1
    }
  }).then(blindCode => {
    ElMessageBox.alert(
      `<strong>收样成功！</strong><br/><br/>已成功抹除客户信息，系统生成的内部盲样条码为：<h3 style="color:#f56c6c">${blindCode}</h3>`,
      '系统提示',
      { dangerouslyUseHTMLString: true }
    )
    dialogVisible.value = false
    fetchPendingList()
  }).finally(() => {
    assigning.value = false
  })
}

onMounted(() => {
  fetchPendingList()
  fetchInspectorList()
})
</script>

<style scoped>
.receive-container {
  padding: 10px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
</style>
