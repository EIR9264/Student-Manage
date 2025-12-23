<template>
  <AppLayout>
    <div class="timetable-container">
      <el-card class="header-card">
        <div class="header-content">
          <h2>我的课表</h2>
          <div class="week-nav">
            <el-button @click="prevWeek" :icon="ArrowLeft" />
            <span class="week-label">{{ weekLabel }}</span>
            <el-button @click="nextWeek" :icon="ArrowRight" />
            <el-button type="primary" plain @click="goToday">本周</el-button>
          </div>
        </div>
      </el-card>

      <el-card class="timetable-card" v-loading="loading">
        <div class="timetable">
          <!-- 表头：星期 -->
          <div class="timetable-header">
            <div class="time-column header-cell">节次</div>
            <div v-for="day in weekDays" :key="day.value" class="day-column header-cell"
                 :class="{ 'is-today': isToday(day.date) }">
              <div class="day-name">{{ day.name }}</div>
              <div class="day-date">{{ formatDate(day.date) }}</div>
            </div>
          </div>

          <!-- 课表内容 -->
          <div class="timetable-body">
            <div v-for="section in sections" :key="section.id" class="timetable-row">
              <!-- 节次列 -->
              <div class="time-column section-cell">
                <div class="section-num">第{{ section.id }}节</div>
                <div class="section-time">{{ section.time }}</div>
              </div>
              <!-- 每天的课程 -->
              <div v-for="day in weekDays" :key="day.value" class="day-column course-cell"
                   :class="{ 'is-today': isToday(day.date) }">
                <div v-for="course in getCoursesAt(day.value, section.id)" :key="course.id"
                     class="course-item" :style="{ backgroundColor: course.color }"
                     @click="showCourseDetail(course)">
                  <div class="course-name">{{ course.courseName }}</div>
                  <div class="course-info">{{ course.classroom }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 课程详情弹窗 -->
      <el-dialog v-model="dialogVisible" :title="selectedCourse?.courseName" width="400px">
        <div v-if="selectedCourse" class="course-detail">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="节次">
              第{{ selectedCourse.sectionStart }}-{{ selectedCourse.sectionEnd }}节
            </el-descriptions-item>
            <el-descriptions-item label="时间">
              {{ getSectionTimeRange(selectedCourse.sectionStart, selectedCourse.sectionEnd) }}
            </el-descriptions-item>
            <el-descriptions-item label="教室">
              {{ selectedCourse.classroom || '未指定' }}
            </el-descriptions-item>
            <el-descriptions-item label="教师">
              {{ selectedCourse.teacherName || '未指定' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <template #footer>
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="goToCourseDetail">查看详情</el-button>
        </template>
      </el-dialog>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getCalendarEvents } from '@/api/course'
import AppLayout from '@/components/AppLayout.vue'

const router = useRouter()
const loading = ref(false)
const dialogVisible = ref(false)
const selectedCourse = ref(null)
const currentWeekStart = ref(getMonday(new Date()))
const courseData = ref([])

// 节次定义（6大节，每大节2小节）
const sections = [
  { id: 1, time: '08:00-08:50' },
  { id: 2, time: '08:50-09:40' },
  { id: 3, time: '10:00-10:50' },
  { id: 4, time: '10:50-11:40' },
  { id: 5, time: '14:00-14:50' },
  { id: 6, time: '14:50-15:40' },
  { id: 7, time: '16:00-16:50' },
  { id: 8, time: '16:50-17:40' },
  { id: 9, time: '19:00-19:50' },
  { id: 10, time: '19:50-20:40' },
  { id: 11, time: '21:00-21:50' },
  { id: 12, time: '21:50-22:40' }
]

// 星期几
const weekDays = computed(() => {
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return days.map((name, index) => {
    const date = new Date(currentWeekStart.value)
    date.setDate(date.getDate() + index)
    return { name, value: index + 1, date }
  })
})

// 周标签
const weekLabel = computed(() => {
  const start = currentWeekStart.value
  const end = new Date(start)
  end.setDate(end.getDate() + 6)
  return `${formatDate(start)} - ${formatDate(end)}`
})

// 获取周一日期
function getMonday(date) {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  d.setDate(diff)
  d.setHours(0, 0, 0, 0)
  return d
}

// 格式化日期
function formatDate(date) {
  const m = date.getMonth() + 1
  const d = date.getDate()
  return `${m}/${d}`
}

// 是否是今天
function isToday(date) {
  const today = new Date()
  return date.toDateString() === today.toDateString()
}

// 上一周
function prevWeek() {
  const newDate = new Date(currentWeekStart.value)
  newDate.setDate(newDate.getDate() - 7)
  currentWeekStart.value = newDate
  fetchCourses()
}

// 下一周
function nextWeek() {
  const newDate = new Date(currentWeekStart.value)
  newDate.setDate(newDate.getDate() + 7)
  currentWeekStart.value = newDate
  fetchCourses()
}

// 回到本周
function goToday() {
  currentWeekStart.value = getMonday(new Date())
  fetchCourses()
}

// 获取某天某节的课程
function getCoursesAt(dayOfWeek, sectionId) {
  return courseData.value.filter(c =>
    c.dayOfWeek === dayOfWeek &&
    c.sectionStart <= sectionId &&
    c.sectionEnd >= sectionId
  )
}

// 获取节次时间范围
function getSectionTimeRange(start, end) {
  const startSection = sections.find(s => s.id === start)
  const endSection = sections.find(s => s.id === end)
  if (startSection && endSection) {
    return `${startSection.time.split('-')[0]} - ${endSection.time.split('-')[1]}`
  }
  return ''
}

// 显示课程详情
function showCourseDetail(course) {
  selectedCourse.value = course
  dialogVisible.value = true
}

// 跳转课程详情页
function goToCourseDetail() {
  if (selectedCourse.value?.courseId) {
    router.push(`/courses/${selectedCourse.value.courseId}`)
  }
}

// 获取课程数据
async function fetchCourses() {
  loading.value = true
  try {
    const start = currentWeekStart.value.toISOString().split('T')[0]
    const end = new Date(currentWeekStart.value)
    end.setDate(end.getDate() + 6)
    const endStr = end.toISOString().split('T')[0]

    const res = await getCalendarEvents(start, endStr)
    if (res.data.success) {
      // 转换数据格式
      const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9B59B6']
      const courseColorMap = {}
      let colorIndex = 0

      courseData.value = res.data.data.map(event => {
        const courseId = event.extendedProps?.courseId
        if (!courseColorMap[courseId]) {
          courseColorMap[courseId] = colors[colorIndex % colors.length]
          colorIndex++
        }
        const eventDate = new Date(event.start)
        return {
          id: event.id,
          courseId: courseId,
          courseName: event.title,
          dayOfWeek: eventDate.getDay() === 0 ? 7 : eventDate.getDay(),
          sectionStart: event.extendedProps?.sectionStart || 1,
          sectionEnd: event.extendedProps?.sectionEnd || 2,
          classroom: event.extendedProps?.classroom,
          teacherName: event.extendedProps?.teacherName,
          color: courseColorMap[courseId]
        }
      })
    }
  } catch (error) {
    ElMessage.error('获取课表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.timetable-container {
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

.week-nav {
  display: flex;
  align-items: center;
  gap: 10px;
}

.week-label {
  font-size: 16px;
  font-weight: 500;
  min-width: 150px;
  text-align: center;
}

.timetable-card {
  overflow-x: auto;
}

.timetable {
  min-width: 900px;
}

.timetable-header {
  display: flex;
  border-bottom: 2px solid #409EFF;
}

.header-cell {
  padding: 12px 8px;
  text-align: center;
  font-weight: 600;
  background: #f5f7fa;
}

.header-cell.is-today {
  background: #ecf5ff;
  color: #409EFF;
}

.day-name {
  font-size: 15px;
}

.day-date {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.time-column {
  width: 80px;
  flex-shrink: 0;
}

.day-column {
  flex: 1;
  min-width: 100px;
  border-left: 1px solid #ebeef5;
}

.timetable-body {
  display: flex;
  flex-direction: column;
}

.timetable-row {
  display: flex;
  min-height: 50px;
  border-bottom: 1px solid #ebeef5;
}

.section-cell {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #fafafa;
  padding: 4px;
}

.section-num {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

.section-time {
  font-size: 10px;
  color: #909399;
  margin-top: 2px;
}

.course-cell {
  position: relative;
  padding: 2px;
}

.course-cell.is-today {
  background: #fafcff;
}

.course-item {
  padding: 6px 8px;
  border-radius: 4px;
  color: white;
  cursor: pointer;
  height: 100%;
  min-height: 44px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.course-item:hover {
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.course-name {
  font-size: 13px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-info {
  font-size: 11px;
  opacity: 0.9;
  margin-top: 2px;
}

.course-detail {
  padding: 10px 0;
}
</style>
