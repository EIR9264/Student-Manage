<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Management, User, Reading, Collection, Calendar, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 获取用户信息
const userInfo = ref({})
const isAdmin = computed(() => userInfo.value.role === 'ADMIN')

// 当前激活的菜单
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/student') return 'student'
  if (path.startsWith('/courses') && !path.includes('manage')) return 'courses'
  if (path === '/my-courses') return 'my-courses'
  if (path === '/calendar') return 'calendar'
  if (path === '/course-manage') return 'course-manage'
  return 'student'
})

// 导航菜单选择
const handleMenuSelect = (index) => {
  switch (index) {
    case 'student':
      router.push('/student')
      break
    case 'courses':
      router.push('/courses')
      break
    case 'my-courses':
      router.push('/my-courses')
      break
    case 'calendar':
      router.push('/calendar')
      break
    case 'course-manage':
      router.push('/course-manage')
      break
  }
}

// 去个人中心
const goToProfile = () => {
  router.push('/profile')
}

// 登出
const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
  ElMessage.info('已退出登录')
}

// 获取用户信息
const fetchUserInfo = () => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo">
        <el-icon style="margin-right: 8px; vertical-align: middle;"><Management /></el-icon>
        <span>学生管理系统</span>
      </div>
      <div class="nav-menu">
        <el-menu mode="horizontal" :default-active="activeMenu" @select="handleMenuSelect">
          <el-menu-item index="student">
            <el-icon><User /></el-icon>
            <span>学生管理</span>
          </el-menu-item>
          <el-menu-item index="courses">
            <el-icon><Reading /></el-icon>
            <span>课程中心</span>
          </el-menu-item>
          <el-menu-item index="my-courses">
            <el-icon><Collection /></el-icon>
            <span>我的课程</span>
          </el-menu-item>
          <el-menu-item index="calendar">
            <el-icon><Calendar /></el-icon>
            <span>课程日历</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="course-manage">
            <el-icon><Setting /></el-icon>
            <span>课程管理</span>
          </el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <el-tag :type="isAdmin ? 'danger' : 'info'" style="margin-right: 10px;">
          {{ isAdmin ? '管理员' : '普通用户' }}
        </el-tag>
        <el-button type="primary" plain size="small" @click="goToProfile" style="margin-right: 10px;">
          个人中心
        </el-button>
        <el-button type="danger" plain size="small" @click="logout">退出登录</el-button>
      </div>
    </el-header>

    <el-main class="main-content">
      <slot></slot>
    </el-main>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.logo {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.nav-menu {
  flex: 1;
  margin: 0 20px;
}

.nav-menu .el-menu {
  border-bottom: none;
  background: transparent;
}

.nav-menu .el-menu-item {
  height: 60px;
  line-height: 60px;
}

.header-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.main-content {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>
