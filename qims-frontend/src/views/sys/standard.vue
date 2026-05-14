<template>
  <div class="standard-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>检验标准库</span>
          <div class="header-actions">
            <el-button type="primary" @click="openCreateDialog">新增标准</el-button>
            <el-button type="success" plain :loading="loading" @click="fetchList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" class="query-form">
        <el-form-item label="标准代号">
          <el-input v-model="query.standardCode" placeholder="例如：GB/T 19001-2016" clearable style="width: 240px" />
        </el-form-item>
        <el-form-item label="标准名称">
          <el-input v-model="query.standardName" placeholder="输入标准名称" clearable style="width: 240px" />
        </el-form-item>
        <el-form-item label="食品类型">
          <el-input v-model="query.productCategory" placeholder="例如：肉制品" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="标准类别">
          <el-select v-model="query.standardCategory" placeholder="全部" clearable style="width: 160px">
            <el-option label="国家标准" value="国家标准" />
            <el-option label="行业标准" value="行业标准" />
            <el-option label="地方标准" value="地方标准" />
            <el-option label="团体标准" value="团体标准" />
            <el-option label="企业标准" value="企业标准" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="现行" :value="1" />
            <el-option label="废止" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="filteredList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="standardCode" label="标准代号" min-width="180" />
        <el-table-column prop="standardName" label="标准名称" min-width="240" />
        <el-table-column prop="standardCategory" label="标准类别" min-width="120" />
        <el-table-column prop="productCategory" label="食品类型" min-width="120" />
        <el-table-column prop="status" label="状态" min-width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 1" type="success">现行</el-tag>
            <el-tag v-else type="info">废止</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTimeText" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="openItemsDrawer(scope.row)">
              <el-icon><Setting /></el-icon> 配置检测项
            </el-button>
            <el-dropdown trigger="click" @command="handleCommand($event, scope.row)" style="margin-left: 10px; vertical-align: middle;">
              <el-button type="primary" link size="small">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="delete" style="color: #F56C6C">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增标准' : '编辑标准'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="标准代号" prop="standardCode">
          <el-input v-model="form.standardCode" placeholder="例如：GB/T 19001-2016" />
        </el-form-item>
        <el-form-item label="标准名称" prop="standardName">
          <el-input v-model="form.standardName" placeholder="例如：质量管理体系要求" />
        </el-form-item>
        <el-form-item label="标准类别" prop="standardCategory">
          <el-select v-model="form.standardCategory" style="width: 100%">
            <el-option label="国家标准" value="国家标准" />
            <el-option label="行业标准" value="行业标准" />
            <el-option label="地方标准" value="地方标准" />
            <el-option label="团体标准" value="团体标准" />
            <el-option label="企业标准" value="企业标准" />
          </el-select>
        </el-form-item>
        <el-form-item label="食品类型" prop="productCategory">
          <el-input v-model="form.productCategory" placeholder="例如：肉制品/乳制品/饮料" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">现行</el-radio>
            <el-radio :value="0">废止</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 检测项目配置抽屉 -->
    <el-drawer
      v-model="itemsDrawerVisible"
      :title="`检测项目配置 - ${currentStandard.standardName}`"
      size="800px"
    >
      <div style="margin-bottom: 15px">
        <el-button type="primary" @click="openItemDialog">新增检测项目</el-button>
      </div>
      
      <el-table :data="itemList" border v-loading="itemLoading">
        <el-table-column prop="itemName" label="项目名称" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column label="判定方式与限值" min-width="180">
          <template #default="scope">
            <span v-if="scope.row.judgeType === 1">区间: [{{ scope.row.minValue }}, {{ scope.row.maxValue }}]</span>
            <span v-else-if="scope.row.judgeType === 2">上限: &le; {{ scope.row.maxValue }}</span>
            <span v-else-if="scope.row.judgeType === 3">下限: &ge; {{ scope.row.minValue }}</span>
            <span v-else-if="scope.row.judgeType === 4">定性要求: {{ scope.row.textStandard }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button type="danger" link @click="removeItem(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog v-model="itemDialogVisible" title="新增检测项目" width="500px">
      <el-form :model="itemForm" label-width="120px">
        <el-form-item label="项目名称" required>
          <el-input v-model="itemForm.itemName" placeholder="如：甲醛含量" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="itemForm.unit" placeholder="如：mg/kg" />
        </el-form-item>
        <el-form-item label="判定类型" required>
          <el-select v-model="itemForm.judgeType" style="width: 100%">
            <el-option label="1-数值范围区间" :value="1" />
            <el-option label="2-数值上限 (<=)" :value="2" />
            <el-option label="3-数值下限 (>=)" :value="3" />
            <el-option label="4-文本定性要求" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="最小值" v-if="[1, 3].includes(itemForm.judgeType)">
          <el-input-number v-model="itemForm.minValue" :precision="3" :step="0.1" />
        </el-form-item>
        <el-form-item label="最大值" v-if="[1, 2].includes(itemForm.judgeType)">
          <el-input-number v-model="itemForm.maxValue" :precision="3" :step="0.1" />
        </el-form-item>
        <el-form-item label="文本要求" v-if="itemForm.judgeType === 4">
          <el-input v-model="itemForm.textStandard" placeholder="如：无异味" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitItem" :loading="itemSubmitting">保存项目</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, ArrowDown } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])

const query = reactive({
  standardCode: '',
  standardName: '',
  standardCategory: '',
  productCategory: '',
  status: null,
  applied: {
    standardCode: '',
    standardName: '',
    standardCategory: '',
    productCategory: '',
    status: null
  }
})

const applyFilter = () => {
  query.applied.standardCode = query.standardCode || ''
  query.applied.standardName = query.standardName || ''
  query.applied.standardCategory = query.standardCategory || ''
  query.applied.productCategory = query.productCategory || ''
  query.applied.status = query.status
}

const resetFilter = () => {
  query.standardCode = ''
  query.standardName = ''
  query.standardCategory = ''
  query.productCategory = ''
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

const filteredList = computed(() => {
  const standardCode = (query.applied.standardCode || '').trim()
  const standardName = (query.applied.standardName || '').trim()
  const standardCategory = (query.applied.standardCategory || '').trim()
  const productCategory = (query.applied.productCategory || '').trim()
  const status = query.applied.status

  return (list.value || [])
    .map(item => ({ ...item, createTimeText: formatDateTime(item.createTime) }))
    .filter(item => {
      const okCode = standardCode ? String(item.standardCode || '').includes(standardCode) : true
      const okName = standardName ? String(item.standardName || '').includes(standardName) : true
      const okStandardCategory = standardCategory ? String(item.standardCategory || '').includes(standardCategory) : true
      const okCategory = productCategory ? String(item.productCategory || '').includes(productCategory) : true
      const okStatus = status === null || status === undefined ? true : item.status === status
      return okCode && okName && okStandardCategory && okCategory && okStatus
    })
})

// =========== 标准主表 CRUD ===========
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/std-standard')
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
  standardCode: '',
  standardName: '',
  standardCategory: '国家标准',
  productCategory: '',
  status: 1
})

const rules = reactive({
  standardCode: [{ required: true, message: '请填写标准代号', trigger: 'blur' }],
  standardName: [{ required: true, message: '请填写标准名称', trigger: 'blur' }],
  standardCategory: [{ required: true, message: '请选择标准类别', trigger: 'change' }],
  productCategory: [{ required: true, message: '请填写食品类型', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

const openCreateDialog = () => {
  dialogMode.value = 'create'
  Object.assign(form, {
    id: null,
    standardCode: '',
    standardName: '',
    standardCategory: '国家标准',
    productCategory: '',
    status: 1
  })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  dialogMode.value = 'edit'
  Object.assign(form, {
    id: row.id,
    standardCode: row.standardCode,
    standardName: row.standardName,
    standardCategory: row.standardCategory || '国家标准',
    productCategory: row.productCategory,
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
        standardCode: form.standardCode,
        standardName: form.standardName,
        standardCategory: form.standardCategory,
        productCategory: form.productCategory,
        status: form.status
      }
      if (dialogMode.value === 'create') {
        await request.post('/std-standard', payload)
        ElMessage.success('新增成功')
      } else {
        await request.put('/std-standard', payload)
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
  await ElMessageBox.confirm(`确认删除标准：${row.standardCode}？`, '提示', { type: 'warning' })
  loading.value = true
  try {
    await request.delete(`/std-standard/${row.id}`)
    ElMessage.success('删除成功')
    await fetchList()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
})

// =========== 检测项目配置 ===========
const itemsDrawerVisible = ref(false)
const itemDialogVisible = ref(false)
const itemLoading = ref(false)
const itemSubmitting = ref(false)
const itemList = ref([])
const currentStandard = ref({})
const handleCommand = (command, row) => {
  if (command === 'edit') {
    openEditDialog(row)
  } else if (command === 'delete') {
    remove(row)
  }
}

const itemForm = ref({
  standardId: '',
  itemName: '',
  unit: '',
  judgeType: 1,
  minValue: 0,
  maxValue: 0,
  textStandard: ''
})

const openItemsDrawer = (row) => {
  currentStandard.value = row
  itemsDrawerVisible.value = true
  fetchItemList(row.id)
}

const fetchItemList = (standardId) => {
  itemLoading.value = true
  request.get(`/std-inspection-item?standardId=${standardId}`).then(res => {
    itemList.value = res || []
  }).finally(() => {
    itemLoading.value = false
  })
}

const openItemDialog = () => {
  itemForm.value = {
    standardId: currentStandard.value.id,
    itemName: '',
    unit: '',
    judgeType: 1,
    minValue: 0,
    maxValue: 0,
    textStandard: ''
  }
  itemDialogVisible.value = true
}

const submitItem = () => {
  if (!itemForm.value.itemName) {
    ElMessage.warning('请输入项目名称')
    return
  }
  itemSubmitting.value = true
  request.post('/std-inspection-item', itemForm.value).then(() => {
    ElMessage.success('添加成功')
    itemDialogVisible.value = false
    fetchItemList(currentStandard.value.id)
  }).finally(() => {
    itemSubmitting.value = false
  })
}

const removeItem = (id) => {
  ElMessageBox.confirm('确认删除该检测项目吗?', '提示', { type: 'warning' }).then(() => {
    request.delete(`/std-inspection-item/${id}`).then(() => {
      ElMessage.success('删除成功')
      fetchItemList(currentStandard.value.id)
    })
  })
}
</script>

<style scoped>
.standard-container {
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
