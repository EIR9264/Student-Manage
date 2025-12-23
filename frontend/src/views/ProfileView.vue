<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../api/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const userInfo = ref({})
const loading = ref(false)
const passwordDialogVisible = ref(false)
const avatarUrl = ref('')
const uploadLoading = ref(false)

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 获取用户信息
const fetchUserInfo = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/profile')
    if (res.data.success) {
      userInfo.value = res.data.data
    }
    // 获取头像
    const avatarRes = await request.get('/avatar/url')
    if (avatarRes.data.success && avatarRes.data.data.avatarUrl) {
      avatarUrl.value = avatarRes.data.data.avatarUrl
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 头像上传前校验
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 自定义头像上传
const handleAvatarUpload = async (options) => {
  uploadLoading.value = true
  const formData = new FormData()
  formData.append('file', options.file)

  try {
    const res = await request.post('/avatar/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.data.success) {
      avatarUrl.value = res.data.data.avatarUrl
      ElMessage.success('头像上传成功')
    }
  } catch (e) {
    ElMessage.error('头像上传失败')
  } finally {
    uploadLoading.value = false
  }
}

// 更新个人信息
const handleUpdateProfile = async () => {
  try {
    const res = await request.put('/user/profile', {
      realName: userInfo.value.realName,
      email: userInfo.value.email,
      phone: userInfo.value.phone
    })
    if (res.data.success) {
      ElMessage.success('更新成功')
      // 更新本地存储
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  } catch (e) {
    console.error(e)
  }
}

// 修改密码
const handleUpdatePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword || !passwordForm.value.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('密码长度至少为6位')
    return
  }

  try {
    const res = await request.put('/user/password', passwordForm.value)
    if (res.data.success) {
      ElMessage.success('密码修改成功，请重新登录')
      passwordDialogVisible.value = false
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }
  } catch (e) {
    console.error(e)
  }
}

const goBack = () => {
  router.push('/student')
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo">
        <el-icon style="margin-right: 8px"><User /></el-icon>
        <span>个人中心</span>
      </div>
      <el-button type="primary" plain size="small" @click="goBack">返回</el-button>
    </el-header>

    <el-main class="main-content">
      <el-card class="profile-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <span>个人信息</span>
            <el-button type="primary" size="small" @click="handleUpdateProfile">保存修改</el-button>
          </div>
        </template>

        <el-form :model="userInfo" label-width="100px">
          <el-form-item label="头像">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :http-request="handleAvatarUpload"
              :before-upload="beforeAvatarUpload"
            >
              <img v-if="avatarUrl" :src="avatarUrl" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon" :class="{ 'is-loading': uploadLoading }">
                <Plus />
              </el-icon>
            </el-upload>
            <span class="tip">点击上传头像，支持 jpg/png 格式，大小不超过 2MB</span>
          </el-form-item>

          <el-form-item label="用户名">
            <el-input v-model="userInfo.username" disabled />
            <span class="tip">用户名不可修改</span>
          </el-form-item>

          <el-form-item label="角色">
            <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : ''">
              {{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </el-form-item>

          <el-form-item label="真实姓名">
            <el-input
              v-model="userInfo.realName"
              placeholder="请输入真实姓名"
              :disabled="userInfo.studentId != null"
            />
            <span class="tip" v-if="userInfo.studentId != null">学生用户不可修改基本信息</span>
          </el-form-item>

          <el-form-item label="邮箱">
            <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item label="手机号">
            <el-input v-model="userInfo.phone" placeholder="请输入手机号" />
          </el-form-item>

          <el-form-item>
            <el-button type="warning" @click="passwordDialogVisible = true">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-main>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdatePassword">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  background: #f0f2f5;
}

.header {
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.main-content {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.avatar-uploader-icon:hover {
  border-color: #409eff;
  color: #409eff;
}

.avatar-uploader-icon.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
