<template>
  <AppLayout>
    <div class="calendar-container">
    <el-card class="header-card">
      <div class="header-content">
        <h2>课程日历</h2>
      </div>
    </el-card>

    <el-card class="calendar-card">
      <FullCalendar ref="calendarRef" :options="calendarOptions" />
    </el-card>

    <!-- 课程详情弹窗 -->
    <el-dialog v-model="eventDialogVisible" :title="selectedEvent?.title" width="400px">
      <div v-if="selectedEvent" class="event-detail">
        <p><strong>时间：</strong>{{ formatEventTime(selectedEvent) }}</p>
        <p><strong>教室：</strong>{{ selectedEvent.extendedProps?.classroom || '未指定' }}</p>
        <p><strong>教师：</strong>{{ selectedEvent.extendedProps?.teacherName || '未指定' }}</p>
      </div>
      <template #footer>
        <el-button @click="eventDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToCourseDetail">查看课程</el-button>
      </template>
    </el-dialog>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import { getCalendarEvents } from '@/api/course'
import AppLayout from '@/components/AppLayout.vue'

const router = useRouter()
const calendarRef = ref(null)
const eventDialogVisible = ref(false)
const selectedEvent = ref(null)

const calendarOptions = ref({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'timeGridWeek',
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek,timeGridDay'
  },
  locale: 'zh-cn',
  slotMinTime: '07:00:00',
  slotMaxTime: '22:00:00',
  allDaySlot: false,
  weekends: true,
  nowIndicator: true,
  slotDuration: '00:30:00',
  height: 'auto',
  events: [],
  eventClick: handleEventClick,
  datesSet: handleDatesSet,
  buttonText: {
    today: '今天',
    month: '月',
    week: '周',
    day: '日'
  }
})

async function fetchEvents(start, end) {
  try {
    const startDate = start.toISOString().split('T')[0]
    const endDate = end.toISOString().split('T')[0]
    console.log('请求日历数据:', startDate, '至', endDate)
    const res = await getCalendarEvents(startDate, endDate)
    if (res.data.success) {
      console.log('返回事件数量:', res.data.data.length)
      console.log('事件详情:', JSON.stringify(res.data.data, null, 2))
      calendarOptions.value.events = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取课程日历失败')
  }
}

function handleDatesSet(dateInfo) {
  fetchEvents(dateInfo.start, dateInfo.end)
}

function handleEventClick(info) {
  selectedEvent.value = info.event
  eventDialogVisible.value = true
}

function formatEventTime(event) {
  if (!event) return ''
  const start = event.start
  const end = event.end
  const formatTime = (date) => {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return `${formatTime(start)} - ${formatTime(end)}`
}

function goToCourseDetail() {
  if (selectedEvent.value?.extendedProps?.courseId) {
    router.push(`/courses/${selectedEvent.value.extendedProps.courseId}`)
  }
}

// datesSet 事件会在日历初始化和视图切换时自动触发，无需在 onMounted 中手动调用
</script>

<style scoped>
.calendar-container {
  padding: 20px;
  max-width: 1200px;
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

.calendar-card {
  padding: 10px;
}

.event-detail p {
  margin: 10px 0;
  font-size: 14px;
}

:deep(.fc) {
  font-family: inherit;
}

:deep(.fc-toolbar-title) {
  font-size: 1.2em !important;
}

:deep(.fc-event) {
  cursor: pointer;
  border-radius: 4px;
}

:deep(.fc-timegrid-slot) {
  height: 40px;
}
</style>
