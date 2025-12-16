<template>
  <AppLayout>
    <div class="my-courses-container">
    <el-card class="header-card">
      <div class="header-content">
        <h2>我的课程</h2>
        <div class="header-actions">
          <el-button type="primary" @click="goToCourseList">
            <el-icon><Plus /></el-icon>
            选课
          </el-button>
          <el-button @click="goToCalendar">
            <el-icon><Calendar /></el-icon>
              我的课程表
          </el-button>
        </div>
      </div>
    </el-card>

    <el-empty v-if="enrollments.length === 0" description="暂无选课记录">
      <el-button type="primary" @click="goToCourseList">去选课</el-button>
    </el-empty>

    <div v-else class="course-list">
      <el-card
        v-for="enrollment in enrollments"
        :key="enrollment.id"
        class="course-item"
        shadow="hover"
      >
        <div class="course-content">
          <div class="course-info" @click="goToDetail(enrollment.courseId)">
            <h3 class="course-name">{{ enrollment.courseName }}</h3>
            <p class="course-code">{{ enrollment.courseCode }}</p>
            <div class="course-meta">
              <span class="teacher">
                <el-icon><User /></el-icon>
                {{ enrollment.teacherName || '未指定' }}
              </span>
              <span class="credit">
                <el-icon><Medal /></el-icon>
                {{ enrollment.credit }} 学分
              </span>
            </div>
            <p class="enroll-time">
              选课时间：{{ formatDate(enrollment.enrolledAt) }}
            </p>
          </div>
          <div class="course-actions">
            <el-button type="primary" @click="goToDetail(enrollment.courseId)">
              查看详情
            </el-button>
            <el-button type="danger" plain @click="handleDrop(enrollment)">
              退选
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Calendar, User, Medal } from '@element-plus/icons-vue'
import { getMyEnrollments, dropCourse } from '@/api/course'
import AppLayout from '@/components/AppLayout.vue'

const router = useRouter()
const enrollments = ref([])

const fetchEnrollments = async () => {
  try {
    const res = await getMyEnrollments()
    if (res.data.success) {
      enrollments.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取选课列表失败')
  }
}

const goToCourseList = () => {
  router.push('/courses')
}

const goToCalendar = () => {
  router.push('/calendar')
}

const goToDetail = (courseId) => {
  router.push(`/courses/${courseId}`)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const handleDrop = async (enrollment) => {
  try {
    await ElMessageBox.confirm(
      `确定要退选课程「${enrollment.courseName}」吗？`,
      '确认退课',
      { type: 'warning' }
    )
    const res = await dropCourse(enrollment.id)
    if (res.data.success) {
      ElMessage.success('退课成功')
      fetchEnrollments()
    } else {
      ElMessage.error(res.data.message || '退课失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '退课失败')
    }
  }
}

onMounted(() => {
  fetchEnrollments()
})
</script>

<style scoped>
.my-courses-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.course-item {
  cursor: pointer;
}

.course-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-info {
  flex: 1;
}

.course-name {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.course-code {
  margin: 0 0 10px 0;
  color: #909399;
  font-size: 14px;
}

.course-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}

.course-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
}

.enroll-time {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.course-actions {
  display: flex;
  gap: 10px;
}
</style>
