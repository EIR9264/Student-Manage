<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Reading, Collection, Calendar, Setting, TrendCharts } from '@element-plus/icons-vue'
import AppLayout from '@/components/AppLayout.vue'

const router = useRouter()

const userInfo = computed(() => {
  const stored = localStorage.getItem('userInfo')
  return stored ? JSON.parse(stored) : {}
})

const isAdmin = computed(() => userInfo.value.role === 'ADMIN')

const quickActions = computed(() => {
  const actions = [
    { key: 'courses', label: '选课中心', desc: '浏览可选课程', icon: Reading, color: '#007aff', path: '/courses' },
    { key: 'my-courses', label: '我的课程', desc: '查看已选课程', icon: Collection, color: '#34c759', path: '/my-courses' },
    { key: 'calendar', label: '课程日历', desc: '查看课表安排', icon: Calendar, color: '#ff9500', path: '/calendar' },
  ]
  if (isAdmin.value) {
    actions.unshift({ key: 'student', label: '学生管理', desc: '管理学生信息', icon: User, color: '#af52de', path: '/student' })
    actions.push({ key: 'course-manage', label: '课程管理', desc: '管理课程信息', icon: Setting, color: '#ff3b30', path: '/course-manage' })
  }
  return actions
})

const goTo = (path) => {
  router.push(path)
}
</script>

<template>
  <AppLayout>
    <div class="home-container">
      <!-- 欢迎区 -->
      <div class="welcome-section animate-fade-up">
        <h2 class="welcome-title">欢迎回来，{{ userInfo.username || '用户' }}</h2>
        <p class="welcome-desc">今天也要加油学习哦</p>
      </div>

      <!-- 快捷入口 -->
      <div class="section">
        <h3 class="section-title">快捷入口</h3>
        <div class="action-grid">
          <div
            v-for="action in quickActions"
            :key="action.key"
            class="action-card ios-pressable"
            @click="goTo(action.path)"
          >
            <div class="action-icon" :style="{ background: action.color }">
              <el-icon><component :is="action.icon" /></el-icon>
            </div>
            <div class="action-info">
              <div class="action-label">{{ action.label }}</div>
              <div class="action-desc">{{ action.desc }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<style scoped>
.home-container {
  max-width: 1000px;
  margin: 0 auto;
}

.welcome-section {
  margin-bottom: 32px;
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--ios-text);
  margin: 0 0 8px;
}

.welcome-desc {
  font-size: 15px;
  color: var(--ios-text-secondary);
  margin: 0;
}

.section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--ios-text);
  margin: 0 0 16px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--ios-card-solid);
  border-radius: var(--ios-radius-lg);
  box-shadow: var(--ios-shadow-sm);
  cursor: pointer;
  transition: all var(--ios-transition);
}

.action-card:hover {
  box-shadow: var(--ios-shadow);
  transform: translateY(-2px);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--ios-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.action-info {
  flex: 1;
}

.action-label {
  font-size: 16px;
  font-weight: 600;
  color: var(--ios-text);
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: var(--ios-text-secondary);
}
</style>
