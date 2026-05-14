<template>
  <el-container class="layout-container">
    <!-- 左侧边栏 -->
    <el-aside width="260px" class="sidebar">
      <div class="logo-box">
        <el-icon class="logo-icon" color="#1890ff"><Cpu /></el-icon>
        <span class="logo-text">食品质量检测系统</span>
      </div>
      <el-menu
        :default-active="$route.path"
        class="el-menu-vertical"
        background-color="#001529"
        text-color="#a6adb4"
        active-text-color="#fff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <template #title>首页概览</template>
        </el-menu-item>
        
        <el-sub-menu index="/client" v-if="isClient">
          <template #title>
            <el-icon><User /></el-icon>
            <span>客户业务</span>
          </template>
          <el-menu-item index="/client/delegate">提交委托单</el-menu-item>
          <el-menu-item index="/client/report">报告查询</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/admin" v-if="isAdmin">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>管理员业务</span>
          </template>
          <el-menu-item index="/admin/receive">收样与盲样派发</el-menu-item>
          <el-menu-item index="/admin/review">报告审核签发</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/inspector" v-if="isInspector">
          <template #title>
            <el-icon><Odometer /></el-icon>
            <span>检测员业务</span>
          </template>
          <el-menu-item index="/inspector/task">我的检测任务</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/sys" v-if="isAdmin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/sys/standard">检验标准库</el-menu-item>
          <el-menu-item index="/sys/client">客户管理</el-menu-item>
          <el-menu-item index="/sys/user">员工管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 右侧内容区 -->
    <el-container class="main-container">
      <!-- 顶部 Header -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-icon"><Fold /></el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title || '当前页面' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
              <span class="username">{{ currentUsername }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="!isAdmin" command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item :divided="!isAdmin" command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主视图区 -->
      <el-main class="main-view">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- 个人中心弹窗 -->
    <el-dialog
      title="个人中心"
      v-model="profileVisible"
      width="680px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <el-form :model="profileForm" label-width="100px" style="margin-top: 15px">
            <el-form-item label="账号">
              <el-input v-model="profileForm.username" disabled placeholder="登录账号不可修改" />
            </el-form-item>

            <el-form-item v-if="isInspector" label="姓名">
              <el-input v-model="profileForm.realName" placeholder="请输入姓名" />
            </el-form-item>

            <template v-if="isClient">
              <el-form-item label="企业名称">
                <el-input v-model="profileForm.companyName" placeholder="请输入企业名称" />
              </el-form-item>
              <el-form-item label="联系人">
                <el-input v-model="profileForm.contactPerson" placeholder="请输入联系人" />
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input v-model="profileForm.phone" placeholder="请输入联系电话" />
              </el-form-item>
              <el-form-item label="企业地址">
                <el-input v-model="profileForm.address" placeholder="请输入企业地址" />
              </el-form-item>
            </template>
          </el-form>
          <div style="text-align: right; margin-top: 20px;">
            <el-button type="primary" :loading="saving" @click="handleSaveProfile">保存信息</el-button>
          </div>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="pwd">
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="margin-top: 15px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
          </el-form>
          <div style="text-align: right; margin-top: 20px;">
            <el-button type="primary" :loading="pwdSaving" @click="handleChangePassword">确认修改</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const currentUsername = ref(localStorage.getItem('username') || '用户')
const roleId = ref(localStorage.getItem('roleId'))

const isAdmin = computed(() => roleId.value === '1')
const isInspector = computed(() => roleId.value === '2')
const isClient = computed(() => roleId.value === '3')

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(() => {
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('roleId')
      localStorage.removeItem('userId')
      localStorage.removeItem('clientId')
      
      ElMessage.success('已退出登录')
      router.push('/login')
    })
  } else if (command === 'profile') {
    openProfileDialog()
  }
}

// ---------------- 个人中心相关逻辑 ----------------
const profileVisible = ref(false)
const activeTab = ref('info')
const saving = ref(false)
const pwdSaving = ref(false)
const profileForm = reactive({
  username: '',
  realName: '',
  companyName: '',
  contactPerson: '',
  phone: '',
  address: '',
  loginAccount: ''
})

const pwdFormRef = ref(null)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = reactive({
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次输入的新密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
})

const fetchProfile = async () => {
  try {
    const res = await request.get('/profile')
    profileForm.username = res.username || res.loginAccount || ''
    profileForm.realName = res.realName || ''
    profileForm.companyName = res.companyName || ''
    profileForm.contactPerson = res.contactPerson || ''
    profileForm.phone = res.phone || ''
    profileForm.address = res.address || ''
    profileForm.loginAccount = res.loginAccount || ''
  } catch (error) {
    console.error('Failed to fetch profile', error)
  }
}

const openProfileDialog = () => {
  activeTab.value = 'info'
  profileVisible.value = true
  fetchProfile()
}

const handleSaveProfile = async () => {
  saving.value = true
  try {
    if (isInspector.value) {
      await request.put('/profile', { realName: profileForm.realName })
      localStorage.setItem('username', profileForm.username)
      ElMessage.success('保存成功')
    } else if (isClient.value) {
      await request.put('/profile', {
        companyName: profileForm.companyName,
        contactPerson: profileForm.contactPerson,
        phone: profileForm.phone,
        address: profileForm.address
        // 客户登录账号不允许修改
      })
      ElMessage.success('保存成功')
      await fetchProfile()
    }
  } finally {
    saving.value = false
  }
}

const handleChangePassword = () => {
  if (!pwdFormRef.value) return
  pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    pwdSaving.value = true
    try {
      await request.post('/profile/change-password', {
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('密码修改成功，请使用新密码重新登录')
      profileVisible.value = false
      
      // 密码修改成功后，强制退出登录
      setTimeout(() => {
        localStorage.clear()
        router.push('/login')
      }, 1500)
    } finally {
      pwdSaving.value = false
    }
  })
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
}

.sidebar {
  background-color: #001529;
  transition: width 0.3s;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  z-index: 10;
}

.logo-box {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: #002140;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  overflow: hidden;
}

.logo-icon {
  width: 32px;
  height: 32px;
  margin-right: 12px;
}

.el-menu-vertical {
  border-right: none;
}

/* 覆盖 Element Plus 菜单激活状态的背景色 */
.el-menu-item.is-active {
  background-color: #1890ff !important;
}

.header {
  height: 60px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  margin-right: 20px;
  color: #666;
}

.collapse-icon:hover {
  color: #1890ff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
}

.username {
  margin-left: 8px;
  font-size: 14px;
}

.main-view {
  background-color: #f0f2f5;
  padding: 20px;
}

/* 页面切换动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
