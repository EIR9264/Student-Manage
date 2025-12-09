<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const form = ref({ username: '', password: '' })
const loading = ref(false)

const handleLogin = async () => {
  if(!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/login', form.value)
    if (res.data.success) {
      ElMessage.success('欢迎回来！')
      router.push('/todo') // 跳转到 Todo 页
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('连接服务器失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>Task Manager</h2>
        </div>
      </template>
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="admin" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="123456" prefix-icon="Lock" @keyup.enter="handleLogin"/>
        </el-form-item>
        <el-button type="primary" class="w-100" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>
      <div class="tips">测试账号: admin / 123456</div>
    </el-card>
  </div>
</template>

<style scoped>
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.login-card { width: 400px; border-radius: 15px; }
.card-header h2 { text-align: center; color: #333; margin: 0; }
.w-100 { width: 100%; margin-top: 10px; font-size: 16px; padding: 20px 0; }
.tips { margin-top: 15px; text-align: center; color: #999; font-size: 12px; }
</style>
