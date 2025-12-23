<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { HomeFilled, User, Reading, Collection, Calendar, Setting, SwitchButton, UserFilled } from '@element-plus/icons-vue'
import NotificationBell from './NotificationBell.vue'
import { useNotificationStore } from '@/stores/notification'
import request from '@/api/request'

const router = useRouter()
const route = useRoute()
const notificationStore = useNotificationStore()

const userInfo = ref({})
const avatarUrl = ref('')
const isAdmin = computed(() => userInfo.value.role === 'ADMIN')

const menuItems = computed(() => {
  const items = [
    { key: 'home', label: '主页', icon: HomeFilled, path: '/' },
    { key: 'courses', label: '选课中心', icon: Reading, path: '/courses' },
    { key: 'my-courses', label: '我的课程', icon: Collection, path: '/my-courses' },
    { key: 'calendar', label: '课程日历', icon: Calendar, path: '/calendar' },
  ]
  if (isAdmin.value) {
    items.splice(1, 0, { key: 'student', label: '学生管理', icon: User, path: '/student' })
    items.push({ key: 'course-manage', label: '课程管理', icon: Setting, path: '/course-manage' })
  } else {
    // 普通用户添加班级成员菜单
    items.splice(1, 0, { key: 'classmates', label: '班级成员', icon: UserFilled, path: '/classmates' })
  }
  return items
})

const activeMenu = computed(() => {
  const path = route.path
  if (path === '/' || path === '/home') return 'home'
  if (path === '/student') return 'student'
  if (path === '/classmates') return 'classmates'
  if (path.startsWith('/courses') && !path.includes('manage')) return 'courses'
  if (path === '/my-courses') return 'my-courses'
  if (path === '/calendar') return 'calendar'
  if (path === '/course-manage') return 'course-manage'
  return 'home'
})

const handleMenuClick = (item) => {
  router.push(item.path)
}

const goToProfile = () => {
  router.push('/profile')
}

const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    notificationStore.cleanup()
    router.push('/login')
    ElMessage.info('已退出登录')
  }).catch(() => {})
}

const fetchUserInfo = async () => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
    // 获取用户头像
    try {
      const res = await request.get('/avatar/url')
      if (res.data.data?.avatarUrl) {
        avatarUrl.value = res.data.data.avatarUrl
      }
    } catch (e) {
      // 静默失败，使用默认头像
    }
  }
}

onMounted(() => {
  fetchUserInfo()
  notificationStore.init()
})

onUnmounted(() => {
  notificationStore.cleanup()
})
</script>

<template>
  <div class="app-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <span class="logo-icon">📚</span>
          <span class="logo-text">学生管理</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <div
          v-for="item in menuItems"
          :key="item.key"
          class="nav-item ios-pressable"
          :class="{ active: activeMenu === item.key }"
          @click="handleMenuClick(item)"
        >
          <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          <span class="nav-label">{{ item.label }}</span>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info" @click="goToProfile">
          <el-avatar :size="36" :src="avatarUrl" class="avatar">
            {{ userInfo.username?.charAt(0)?.toUpperCase() || 'U' }}
          </el-avatar>
          <div class="user-detail">
            <div class="username">{{ userInfo.username || '用户' }}</div>
            <div class="role">{{ isAdmin ? '管理员' : '普通用户' }}</div>
          </div>
        </div>
        <button class="logout-btn ios-pressable" @click="logout">
          <el-icon><SwitchButton /></el-icon>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-area">
      <header class="topbar">
        <h1 class="page-title">{{ menuItems.find(m => m.key === activeMenu)?.label || '主页' }}</h1>
        <div class="topbar-actions">
          <NotificationBell />
        </div>
      </header>

      <div class="content">
        <slot></slot>
      </div>
    </main>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
  background: var(--ios-bg);
}

/* 侧边栏 */
.sidebar {
  width: var(--sidebar-width);
  background: var(--ios-sidebar);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid var(--ios-separator);
  display: flex;
  flex-direction: column;
  padding: 20px 12px;
}

.sidebar-header {
  padding: 8px 12px 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: var(--ios-text);
}

/* 导航菜单 */
.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--ios-radius);
  cursor: pointer;
  color: var(--ios-text-secondary);
  transition: all var(--ios-transition);
}

.nav-item:hover {
  background: var(--ios-blue-light);
  color: var(--ios-blue);
}

.nav-item.active {
  background: var(--ios-blue);
  color: white;
}

.nav-icon {
  font-size: 20px;
}

.nav-label {
  font-size: 15px;
  font-weight: 500;
}

/* 侧边栏底部 */
.sidebar-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 8px;
  border-top: 1px solid var(--ios-separator);
  margin-top: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px;
  border-radius: var(--ios-radius);
  transition: background var(--ios-transition);
}

.user-info:hover {
  background: var(--ios-blue-light);
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--ios-blue), var(--ios-purple));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--ios-text);
}

.role {
  font-size: 12px;
  color: var(--ios-text-secondary);
}

.logout-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: var(--ios-radius);
  background: transparent;
  color: var(--ios-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all var(--ios-transition);
}

.logout-btn:hover {
  background: rgba(255, 59, 48, 0.1);
  color: var(--ios-red);
}

/* 主内容区 */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.topbar {
  height: var(--topbar-height);
  padding: 0 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--ios-bg);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--ios-text);
  margin: 0;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.content {
  flex: 1;
  padding: 0 32px 32px;
  overflow-y: auto;
}
</style>
