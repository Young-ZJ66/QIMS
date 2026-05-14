<template>
  <div class="user-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>员工管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="openCreateDialog">新增员工</el-button>
            <el-button type="success" plain :loading="loading" @click="fetchList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" class="query-form">
        <el-form-item label="账号">
          <el-input v-model="query.username" placeholder="输入账号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="query.realName" placeholder="输入姓名" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.roleId" placeholder="全部" clearable style="width: 160px">
            <el-option label="管理员" :value="1" />
            <el-option label="检测员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 160px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="filteredList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="账号" min-width="150" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="roleLabel" label="角色" min-width="100" align="center" />
        <el-table-column prop="phone" label="电话" min-width="140" />
        <el-table-column prop="status" label="状态" min-width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTimeText" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" plain @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              size="small"
              plain
              :disabled="scope.row.username === currentUsername"
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              plain 
              :disabled="scope.row.username === currentUsername"
              @click="remove(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增员工' : '编辑员工'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" placeholder="例如：admin" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item :label="dialogMode === 'create' ? '登录密码' : '重置密码'" :prop="dialogMode === 'create' ? 'password' : undefined">
          <el-input v-model="form.password" type="password" show-password placeholder="编辑时留空表示不修改" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="例如：张三" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-radio-group v-model="form.roleId" :disabled="dialogMode === 'edit' && form.username === currentUsername">
            <el-radio :value="1">管理员</el-radio>
            <el-radio :value="2">检测员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="可选" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status" :disabled="dialogMode === 'edit' && form.username === currentUsername">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const currentUsername = ref(localStorage.getItem('username') || '')

const query = reactive({
  username: '',
  realName: '',
  roleId: null,
  status: null,
  applied: {
    username: '',
    realName: '',
    roleId: null,
    status: null
  }
})

const applyFilter = () => {
  query.applied.username = query.username || ''
  query.applied.realName = query.realName || ''
  query.applied.roleId = query.roleId
  query.applied.status = query.status
}

const resetFilter = () => {
  query.username = ''
  query.realName = ''
  query.roleId = null
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

const roleText = (roleId) => {
  if (roleId === 1) return '管理员'
  if (roleId === 2) return '检测员'
  return '-'
}

const filteredList = computed(() => {
  const username = (query.applied.username || '').trim()
  const realName = (query.applied.realName || '').trim()
  const roleId = query.applied.roleId
  const status = query.applied.status

  return (list.value || [])
    .map(item => ({
      ...item,
      roleLabel: roleText(item.roleId),
      createTimeText: formatDateTime(item.createTime)
    }))
    .filter(item => {
      const okUsername = username ? String(item.username || '').includes(username) : true
      const okRealName = realName ? String(item.realName || '').includes(realName) : true
      const okRole = roleId === null || roleId === undefined ? true : item.roleId === roleId
      const okStatus = status === null || status === undefined ? true : item.status === status
      return okUsername && okRealName && okRole && okStatus
    })
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/sys-user')
    list.value = res || []
    applyFilter()
  } finally {
    loading.value = false
  }
}

const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref(null)

const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  roleId: 2,
  phone: '',
  status: 1
})

const rules = reactive({
  username: [{ required: true, message: '请填写登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请填写登录密码', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

const openCreateDialog = () => {
  dialogMode.value = 'create'
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    realName: '',
    roleId: 2,
    phone: '',
    status: 1
  })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  dialogMode.value = 'edit'
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '',
    realName: row.realName || '',
    roleId: row.roleId ?? 2,
    phone: row.phone || '',
    status: row.status ?? 1
  })
  dialogVisible.value = true
}

const submit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        id: form.id,
        username: form.username,
        password: dialogMode.value === 'create' ? form.password : (form.password ? form.password : null),
        realName: form.realName || null,
        roleId: form.roleId,
        phone: form.phone || null,
        status: form.status
      }

      if (dialogMode.value === 'create') {
        await request.post('/sys-user', payload)
        ElMessage.success('新增成功')
      } else {
        await request.put('/sys-user', payload)
        ElMessage.success('保存成功')
      }

      dialogVisible.value = false
      await fetchList()
    } finally {
      submitting.value = false
    }
  })
}

const toggleStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(`确认${nextStatus === 1 ? '启用' : '禁用'}账号：${row.username}？`, '提示', { type: 'warning' })
  loading.value = true
  try {
    await request.put('/sys-user', { id: row.id, status: nextStatus })
    ElMessage.success('操作成功')
    await fetchList()
  } finally {
    loading.value = false
  }
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除账号：${row.username}？`, '提示', { type: 'warning' })
  loading.value = true
  try {
    await request.delete(`/sys-user/${row.id}`)
    ElMessage.success('删除成功')
    await fetchList()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.user-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.query-form {
  margin-bottom: 16px;
}
</style>

