<template>
  <div class="client-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>客户管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="openCreateDialog">新增客户</el-button>
            <el-button type="success" plain :loading="loading" @click="fetchList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" class="query-form">
        <el-form-item label="企业名称">
          <el-input v-model="query.companyName" placeholder="输入企业名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="query.contactPerson" placeholder="输入联系人" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="filteredList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="companyName" label="企业名称" min-width="180" />
        <el-table-column prop="contactPerson" label="联系人" min-width="120" />
        <el-table-column prop="phone" label="联系电话" min-width="140" />
        <el-table-column prop="address" label="企业地址" min-width="180" />
        <el-table-column prop="loginAccount" label="登录账号" min-width="150" />
        <el-table-column prop="createTimeText" label="注册时间" min-width="160" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" plain @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" plain @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增客户' : '编辑客户'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="企业名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="例如：某某食品股份有限公司" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="例如：张三" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="例如：13800000000" />
        </el-form-item>
        <el-form-item label="企业地址" prop="address">
          <el-input v-model="form.address" placeholder="例如：工业园区1号" />
        </el-form-item>
        <el-form-item label="登录账号" prop="loginAccount">
          <el-input v-model="form.loginAccount" placeholder="例如：client1" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item :label="dialogMode === 'create' ? '登录密码' : '重置密码'" :prop="dialogMode === 'create' ? 'loginPassword' : undefined">
          <el-input v-model="form.loginPassword" type="password" show-password placeholder="编辑时留空表示不修改" />
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

const query = reactive({
  companyName: '',
  contactPerson: '',
  applied: {
    companyName: '',
    contactPerson: ''
  }
})

const applyFilter = () => {
  query.applied.companyName = query.companyName || ''
  query.applied.contactPerson = query.contactPerson || ''
}

const resetFilter = () => {
  query.companyName = ''
  query.contactPerson = ''
  applyFilter()
}

const formatDateTime = (value) => {
  if (!value) return ''
  const str = String(value)
  if (str.includes('T')) return str.replace('T', ' ').slice(0, 19)
  if (str.length >= 19) return str.slice(0, 19)
  return str
}

const filteredList = computed(() => {
  const companyName = (query.applied.companyName || '').trim()
  const contactPerson = (query.applied.contactPerson || '').trim()

  return (list.value || [])
    .map(item => ({
      ...item,
      createTimeText: formatDateTime(item.createTime)
    }))
    .filter(item => {
      const okCompany = companyName ? String(item.companyName || '').includes(companyName) : true
      const okContact = contactPerson ? String(item.contactPerson || '').includes(contactPerson) : true
      return okCompany && okContact
    })
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/sys-client')
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
  companyName: '',
  contactPerson: '',
  phone: '',
  address: '',
  loginAccount: '',
  loginPassword: ''
})

const rules = reactive({
  companyName: [{ required: true, message: '请填写企业名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请填写联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请填写联系电话', trigger: 'blur' }],
  loginAccount: [{ required: true, message: '请填写登录账号', trigger: 'blur' }],
  loginPassword: [{ required: true, message: '请填写登录密码', trigger: 'blur' }]
})

const openCreateDialog = () => {
  dialogMode.value = 'create'
  Object.assign(form, {
    id: null,
    companyName: '',
    contactPerson: '',
    phone: '',
    address: '',
    loginAccount: '',
    loginPassword: ''
  })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  dialogMode.value = 'edit'
  Object.assign(form, {
    id: row.id,
    companyName: row.companyName,
    contactPerson: row.contactPerson || '',
    phone: row.phone || '',
    address: row.address || '',
    loginAccount: row.loginAccount,
    loginPassword: ''
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
        companyName: form.companyName,
        contactPerson: form.contactPerson,
        phone: form.phone,
        address: form.address,
        loginAccount: form.loginAccount,
        loginPassword: dialogMode.value === 'create' ? form.loginPassword : (form.loginPassword ? form.loginPassword : null)
      }

      if (dialogMode.value === 'create') {
        await request.post('/sys-client', payload)
        ElMessage.success('新增成功')
      } else {
        await request.put('/sys-client', payload)
        ElMessage.success('保存成功')
      }

      dialogVisible.value = false
      await fetchList()
    } finally {
      submitting.value = false
    }
  })
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除客户：${row.companyName}？`, '提示', { type: 'warning' })
  loading.value = true
  try {
    await request.delete(`/sys-client/${row.id}`)
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
.client-container {
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
