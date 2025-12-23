<script setup>
import { ref, onMounted, computed } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import request from '../api/request'

const classmates = ref([])
const loading = ref(false)
const userInfo = ref({})
const myClassName = ref('')

const fetchUserInfo = () => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    userInfo.value = JSON.parse(stored)
  }
}

const fetchMyInfo = async () => {
  if (!userInfo.value.studentId) return
  try {
    const res = await request.get(`/students/${userInfo.value.studentId}`)
    myClassName.value = res.data.className || ''
    if (myClassName.value) {
      fetchClassmates()
    }
  } catch (e) {
    // 静默处理
  }
}

const fetchClassmates = async () => {
  if (!myClassName.value) return
  loading.value = true
  try {
    const res = await request.get('/students', {
      params: { className: myClassName.value }
    })
    classmates.value = res.data
  } catch (e) {
    // 静默处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
  fetchMyInfo()
})
</script>

<template>
  <AppLayout>
    <div class="classmates-container">
      <div class="class-header">
        <h3 class="class-name">{{ myClassName || '未分配班级' }}</h3>
        <span class="class-count">共 {{ classmates.length }} 人</span>
      </div>

      <div class="classmates-grid" v-loading="loading">
        <div
          v-for="mate in classmates"
          :key="mate.id"
          class="mate-card"
          :class="{ 'is-me': mate.id === userInfo.studentId }"
        >
          <el-avatar :size="56" :src="mate.avatarUrl" class="mate-avatar">
            {{ mate.name?.charAt(0) || '?' }}
          </el-avatar>
          <div class="mate-info">
            <div class="mate-name">
              {{ mate.name }}
              <el-tag v-if="mate.id === userInfo.studentId" size="small" type="success">我</el-tag>
            </div>
            <div class="mate-number">{{ mate.studentNumber }}</div>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && classmates.length === 0" description="暂无班级成员" />
    </div>
  </AppLayout>
</template>

<style scoped>
.classmates-container {
  max-width: 1000px;
  margin: 0 auto;
}

.class-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.class-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--ios-text);
  margin: 0;
}

.class-count {
  font-size: 14px;
  color: var(--ios-text-secondary);
}

.classmates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.mate-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--ios-card-solid);
  border-radius: var(--ios-radius-lg);
  box-shadow: var(--ios-shadow-sm);
  transition: all var(--ios-transition);
}

.mate-card:hover {
  box-shadow: var(--ios-shadow);
  transform: translateY(-2px);
}

.mate-card.is-me {
  border: 2px solid var(--ios-blue);
}

.mate-avatar {
  background: linear-gradient(135deg, var(--ios-blue), var(--ios-purple));
  color: white;
  font-weight: 600;
}

.mate-info {
  flex: 1;
  min-width: 0;
}

.mate-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--ios-text);
  display: flex;
  align-items: center;
  gap: 6px;
}

.mate-number {
  font-size: 13px;
  color: var(--ios-text-secondary);
  margin-top: 2px;
}
</style>
