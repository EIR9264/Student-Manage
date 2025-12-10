<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)

// 表单数据，新增 captchaCode 和 captchaKey
const form = reactive({
  username: '',
  password: '',
  captchaCode: '',
  captchaKey: ''
})

// 验证码图片 URL
const captchaUrl = ref('')

// 获取验证码
const fetchCaptcha = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/captcha')
    form.captchaKey = res.data.captchaKey
    captchaUrl.value = res.data.image
    form.captchaCode = '' // 刷新时清空输入框
  } catch (e) {
    ElMessage.error('验证码加载失败'+e)
  }
}

const handleLogin = async () => {
  if(!form.username || !form.password || !form.captchaCode) {
    ElMessage.warning('请填写完整信息')
    return
  }

  loading.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/login', form)
    if (res.data.success) {
      ElMessage.success('登录成功！')
      router.push('/student')
    } else {
      ElMessage.error(res.data.message)
      // 登录失败通常刷新验证码，提升体验
      fetchCaptcha()
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('连接服务器失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时获取验证码
onMounted(() => {
  fetchCaptcha()
})
</script>

<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>学生管理系统</h2>
        </div>
      </template>
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="admin" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="123" prefix-icon="Lock" />
        </el-form-item>

        <el-form-item label="验证码">
          <div class="captcha-box">
            <el-input
              v-model="form.captchaCode"
              placeholder="请输入验证码"
              prefix-icon="Key"
              @keyup.enter="handleLogin"
              style="width: 200px; margin-right: 10px;"
            />
            <img
              :src="captchaUrl"
              alt="captcha"
              @click="fetchCaptcha"
              class="captcha-img"
              title="点击刷新"
            />
          </div>
        </el-form-item>

        <el-button type="primary" class="w-100" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.login-card { width: 400px; border-radius: 15px; }
.card-header h2 { text-align: center; color: #333; margin: 0; }
.w-100 { width: 100%; margin-top: 10px; font-size: 16px; padding: 20px 0; }
.captcha-box { display: flex; align-items: center; }
.captcha-img { height: 32px; cursor: pointer; border: 1px solid #dcdfe6; border-radius: 4px; }
</style>
