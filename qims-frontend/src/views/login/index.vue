<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon :size="44" color="#1890ff" class="logo"><Cpu /></el-icon>
        <h2>食品质量检测系统</h2>
        <p class="subtitle">标准检测平台</p>
      </div>
      
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules" 
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入账号 (内部或客户账号均可)" 
            :prefix-icon="'User'"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            :prefix-icon="'Lock'"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            class="login-btn" 
            size="large" 
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
        <div style="text-align: right">
          <el-button type="primary" link @click="showRegisterDialog = true">
            送检客户注册账号
          </el-button>
        </div>
      </el-form>
    </div>

    <!-- 客户注册弹窗 -->
    <el-dialog
      title="送检客户注册"
      v-model="showRegisterDialog"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="regFormRef" :model="regForm" :rules="regRules" label-width="100px">
        <el-form-item label="企业名称" prop="companyName">
          <el-input v-model="regForm.companyName" placeholder="请输入企业全称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="regForm.contactPerson" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="regForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="登录账号" prop="loginAccount">
          <el-input v-model="regForm.loginAccount" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="登录密码" prop="loginPassword">
          <el-input v-model="regForm.loginPassword" type="password" show-password placeholder="请输入登录密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="loginPasswordConfirm">
          <el-input v-model="regForm.loginPasswordConfirm" type="password" show-password placeholder="请再次输入登录密码" />
        </el-form-item>
        <el-form-item label="企业地址" prop="address">
          <el-input v-model="regForm.address" placeholder="请输入企业地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRegisterDialog = false">取消</el-button>
          <el-button type="primary" @click="handleRegister" :loading="regLoading">
            确认注册
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = reactive({
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const handleLogin = () => {
  loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      // 调用登录接口
      request.post('/auth/login', {
        username: loginForm.username,
        password: loginForm.password
      }).then(res => {
        // 保存 JWT Token 和用户信息
        localStorage.setItem('token', res.token)
        localStorage.setItem('userId', res.userId)
        localStorage.setItem('username', res.username)
        localStorage.setItem('roleId', res.roleId)
        
        ElMessage.success(`欢迎回来, ${res.realName || res.username}`)
        router.push('/')
      }).catch(() => {
      }).finally(() => {
        loading.value = false
      })
    } else {
      return false
    }
  })
}

// 注册功能
const showRegisterDialog = ref(false)
const regFormRef = ref(null)
const regLoading = ref(false)
const regForm = reactive({
  companyName: '',
  contactPerson: '',
  phone: '',
  address: '',
  loginAccount: '',
  loginPassword: '',
  loginPasswordConfirm: ''
})

const regRules = reactive({
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  loginAccount: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  loginPassword: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
  loginPasswordConfirm: [
    { required: true, message: '请再次输入登录密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== regForm.loginPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
})

const handleRegister = () => {
  regFormRef.value.validate((valid) => {
    if (valid) {
      regLoading.value = true
      const payload = {
        companyName: regForm.companyName,
        contactPerson: regForm.contactPerson,
        phone: regForm.phone,
        address: regForm.address,
        loginAccount: regForm.loginAccount,
        loginPassword: regForm.loginPassword
      }
      request.post('/auth/register', payload).then(() => {
        ElMessage.success('注册成功，请使用新账号登录')
        showRegisterDialog.value = false
        regFormRef.value.resetFields()
        // 自动填入刚刚注册的账号
        loginForm.username = regForm.loginAccount
      }).finally(() => {
        regLoading.value = false
      })
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
  background-image: url('https://gw.alipayobjects.com/zos/rmsportal/TVYTbAXWheQpRcWDaQGD.svg');
  background-repeat: no-repeat;
  background-position: center 110px;
  background-size: 100%;
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  margin-bottom: 16px;
}

.login-header h2 {
  margin: 0;
  font-size: 28px;
  color: #333;
  font-weight: 600;
}

.subtitle {
  margin-top: 12px;
  font-size: 14px;
  color: #888;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
}

.login-footer {
  margin-top: 24px;
  text-align: center;
  color: #999;
  font-size: 13px;
}

.login-footer p {
  margin: 4px 0;
}
</style>
