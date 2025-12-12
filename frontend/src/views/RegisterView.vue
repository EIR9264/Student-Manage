<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

const registerForm = ref({
  username: '',      // 学号（必填）
  password: '',      // 密码（必填）
  confirmPassword: '',
  realName: '',      // 姓名（必填）
  gender: '男',      // 性别（必填）
  email: '',         // 邮箱（可选）
  phone: '',         // 手机（可选）
  captchaCode: '',
  captchaKey: ''
})

const captchaImage = ref('')
const usernameCheckStatus = ref('') // 'checking', 'available', 'taken'
const loading = ref(false)

// 获取验证码
const fetchCaptcha = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/captcha')
    captchaImage.value = res.data.image
    registerForm.value.captchaKey = res.data.captchaKey
  } catch (e) {
    ElMessage.error('获取验证码失败')
  }
}

// 检查用户名是否存在
const checkUsername = async () => {
  if (!registerForm.value.username || registerForm.value.username.length < 3) {
    return
  }

  usernameCheckStatus.value = 'checking'
  try {
    const res = await axios.get('http://localhost:8080/api/check-username', {
      params: { username: registerForm.value.username }
    })
    usernameCheckStatus.value = res.data.exists ? 'taken' : 'available'
  } catch (e) {
    usernameCheckStatus.value = ''
  }
}

// 注册
const handleRegister = async () => {
  // 表单验证
  if (!registerForm.value.username || registerForm.value.username.length < 3) {
    ElMessage.warning('学号至少3个字符')
    return
  }

  if (usernameCheckStatus.value === 'taken') {
    ElMessage.warning('学号已存在，请更换')
    return
  }

  if (!registerForm.value.realName || registerForm.value.realName.trim() === '') {
    ElMessage.warning('请输入真实姓名')
    return
  }

  if (!registerForm.value.gender) {
    ElMessage.warning('请选择性别')
    return
  }

  if (!registerForm.value.password || registerForm.value.password.length < 6) {
    ElMessage.warning('密码至少6个字符')
    return
  }

  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  if (!registerForm.value.captchaCode) {
    ElMessage.warning('请输入验证码')
    return
  }

  loading.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/register', registerForm.value)
    if (res.data.success) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.data.message)
      fetchCaptcha() // 刷新验证码
    }
  } catch (e) {
    ElMessage.error('注册失败')
    fetchCaptcha()
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  fetchCaptcha()
})
</script>

<template>
  <div class="register-container">
    <div class="register-box">
      <h2 class="title">用户注册</h2>

      <el-form :model="registerForm" label-width="80px">
        <el-form-item label="学号" required>
          <el-input
            v-model="registerForm.username"
            placeholder="请输入学号（至少3个字符）"
            @blur="checkUsername"
          >
            <template #suffix>
              <span v-if="usernameCheckStatus === 'checking'" style="color: #909399;">检查中...</span>
              <span v-if="usernameCheckStatus === 'available'" style="color: #67c23a;">✓ 可用</span>
              <span v-if="usernameCheckStatus === 'taken'" style="color: #f56c6c;">✗ 已存在</span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="姓名" required>
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="性别" required>
          <el-radio-group v-model="registerForm.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="密码" required>
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="至少6个字符"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" required>
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="再次输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="registerForm.email" placeholder="选填" />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="registerForm.phone" placeholder="选填" />
        </el-form-item>

        <el-form-item label="验证码">
          <div style="display: flex; gap: 10px; align-items: center;">
            <el-input
              v-model="registerForm.captchaCode"
              placeholder="请输入验证码"
              style="flex: 1;"
            />
            <img
              :src="captchaImage"
              @click="fetchCaptcha"
              style="height: 40px; cursor: pointer; border: 1px solid #dcdfe6; border-radius: 4px;"
              title="点击刷新"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleRegister"
            :loading="loading"
            style="width: 100%;"
          >
            注册
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button
            text
            @click="goToLogin"
            style="width: 100%;"
          >
            已有账号？去登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  width: 450px;
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}
</style>
