<template>
  <AppLayout>
    <div class="course-list-container">
    <el-card class="header-card">
      <div class="header-content">
        <h2>课程中心</h2>
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程名称/编号/教师"
            clearable
            @keyup.enter="handleSearch"
            style="width: 300px"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-card>

    <div class="course-grid">
      <el-card
        v-for="course in courses"
        :key="course.id"
        class="course-card"
        shadow="hover"
        @click="goToDetail(course.id)"
      >
        <div class="course-cover">
          <el-image
            v-if="course.coverImage"
            :src="course.coverImage"
            fit="cover"
            style="width: 100%; height: 150px"
          />
          <div v-else class="default-cover">
            <el-icon size="48"><Reading /></el-icon>
          </div>
        </div>
        <div class="course-info">
          <h3 class="course-name">{{ course.courseName }}</h3>
          <p class="course-code">{{ course.courseCode }}</p>
          <p class="course-teacher">
            <el-icon><User /></el-icon>
            {{ course.teacherName || '未指定' }}
          </p>
          <div class="course-meta">
            <el-tag size="small" :type="course.courseType === 'REQUIRED' ? 'danger' : 'success'">
              {{ course.courseType === 'REQUIRED' ? '必修' : '选修' }}
            </el-tag>
            <span class="credit">{{ course.credit }} 学分</span>
          </div>
          <div class="course-capacity">
            <el-progress
              :percentage="getCapacityPercent(course)"
              :status="getCapacityStatus(course)"
              :stroke-width="6"
            />
            <span class="capacity-text">
              {{ course.currentStudents }}/{{ course.maxStudents || '不限' }}
            </span>
          </div>
          <div class="course-actions" @click.stop>
            <el-button
              v-if="!course.enrolled"
              type="primary"
              size="small"
              :disabled="isCourseFull(course)"
              @click="handleEnroll(course)"
            >
              {{ isCourseFull(course) ? '已满' : '选课' }}
            </el-button>
            <el-tag v-else type="success" size="small">已选</el-tag>
          </div>
        </div>
      </el-card>
    </div>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[8, 12, 16, 24]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchCourses"
        @current-change="fetchCourses"
      />
    </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Reading, User } from '@element-plus/icons-vue'
import { getCourses, enrollCourse } from '@/api/course'
import AppLayout from '@/components/AppLayout.vue'

const router = useRouter()

const courses = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const fetchCourses = async () => {
  try {
    const res = await getCourses({
      keyword: searchKeyword.value,
      page: currentPage.value - 1,
      size: pageSize.value
    })
    if (res.data.success) {
      courses.value = res.data.data.content
      total.value = res.data.data.totalElements
    }
  } catch (error) {
    ElMessage.error('获取课程列表失败')
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchCourses()
}

const goToDetail = (id) => {
  router.push(`/courses/${id}`)
}

const getCapacityPercent = (course) => {
  if (!course.maxStudents || course.maxStudents === 0) return 0
  return Math.round((course.currentStudents / course.maxStudents) * 100)
}

const getCapacityStatus = (course) => {
  const percent = getCapacityPercent(course)
  if (percent >= 100) return 'exception'
  if (percent >= 80) return 'warning'
  return 'success'
}

const isCourseFull = (course) => {
  return course.maxStudents > 0 && course.currentStudents >= course.maxStudents
}

const handleEnroll = async (course) => {
  try {
    await ElMessageBox.confirm(
      `确定要选择课程「${course.courseName}」吗？`,
      '确认选课',
      { type: 'info' }
    )
    const res = await enrollCourse(course.id)
    if (res.data.success) {
      ElMessage.success('选课成功')
      fetchCourses()
    } else {
      ElMessage.error(res.data.message || '选课失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '选课失败')
    }
  }
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.course-list-container {
  padding: 20px;
  max-width: 1400px;
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

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.course-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.course-card:hover {
  transform: translateY(-4px);
}

.course-cover {
  margin: -20px -20px 15px -20px;
  border-radius: 4px 4px 0 0;
  overflow: hidden;
}

.default-cover {
  height: 150px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.course-info {
  padding: 0 5px;
}

.course-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-code {
  margin: 0 0 8px 0;
  color: #909399;
  font-size: 13px;
}

.course-teacher {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.course-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.credit {
  color: #909399;
  font-size: 13px;
}

.course-capacity {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.course-capacity .el-progress {
  flex: 1;
}

.capacity-text {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.course-actions {
  text-align: right;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>
