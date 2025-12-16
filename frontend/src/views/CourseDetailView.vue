<template>
  <AppLayout>
    <div class="course-detail-container">
    <el-page-header @back="goBack" style="margin-bottom: 20px">
      <template #content>
        <span class="page-title">{{ course.courseName }}</span>
      </template>
    </el-page-header>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>课程信息</span>
              <el-tag :type="course.courseType === 'REQUIRED' ? 'danger' : 'success'">
                {{ course.courseType === 'REQUIRED' ? '必修' : '选修' }}
              </el-tag>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="课程编号">{{ course.courseCode }}</el-descriptions-item>
            <el-descriptions-item label="授课教师">{{ course.teacherName || '未指定' }}</el-descriptions-item>
            <el-descriptions-item label="学分">{{ course.credit }}</el-descriptions-item>
            <el-descriptions-item label="选课人数">
              {{ course.currentStudents }}/{{ course.maxStudents || '不限' }}
            </el-descriptions-item>
            <el-descriptions-item label="开课日期">{{ course.startDate || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="结课日期">{{ course.endDate || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="课程描述" :span="2">
              {{ course.description || '暂无描述' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="schedule-card" style="margin-top: 20px">
          <template #header>
            <span>上课时间</span>
          </template>
          <el-table :data="course.schedules || []" stripe>
            <el-table-column prop="dayOfWeek" label="星期" width="100">
              <template #default="{ row }">
                {{ getDayName(row.dayOfWeek) }}
              </template>
            </el-table-column>
            <el-table-column label="时间" width="180">
              <template #default="{ row }">
                {{ row.startTime }} - {{ row.endTime }}
              </template>
            </el-table-column>
            <el-table-column prop="classroom" label="教室" />
            <el-table-column label="周次">
              <template #default="{ row }">
                第{{ row.weekStart }}-{{ row.weekEnd }}周
                <span v-if="row.weekType !== 'ALL'">
                  ({{ row.weekType === 'ODD' ? '单周' : '双周' }})
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="attachment-card" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>课程资料</span>
              <el-input
                v-model="attachmentSearch"
                placeholder="搜索资料内容"
                clearable
                style="width: 200px"
                @keyup.enter="searchAttachmentContent"
              >
                <template #append>
                  <el-button @click="searchAttachmentContent">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </div>
          </template>

          <el-table :data="attachments" stripe>
            <el-table-column prop="fileName" label="文件名" min-width="200">
              <template #default="{ row }">
                <el-icon :size="18" style="margin-right: 8px; vertical-align: middle">
                  <component :is="getFileIcon(row.fileType)" />
                </el-icon>
                {{ row.fileName }}
              </template>
            </el-table-column>
            <el-table-column prop="fileType" label="类型" width="80" />
            <el-table-column label="大小" width="100">
              <template #default="{ row }">
                {{ formatFileSize(row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link @click="previewAttachment(row)">预览</el-button>
                <el-button type="success" link @click="downloadAttachment(row)">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="action-card">
          <div class="enroll-section">
            <div class="capacity-info">
              <el-progress
                type="circle"
                :percentage="getCapacityPercent()"
                :status="getCapacityStatus()"
                :width="120"
              />
              <p class="capacity-text">
                已选 {{ course.currentStudents }} / {{ course.maxStudents || '不限' }}
              </p>
            </div>

            <el-button
              v-if="!course.enrolled"
              type="primary"
              size="large"
              :disabled="isCourseFull()"
              @click="handleEnroll"
              style="width: 100%"
            >
              {{ isCourseFull() ? '选课人数已满' : '立即选课' }}
            </el-button>
            <el-button
              v-else
              type="danger"
              size="large"
              @click="handleDrop"
              style="width: 100%"
            >
              退选课程
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" :title="previewTitle" width="80%" top="5vh">
      <div class="preview-container">
        <iframe
          v-if="previewUrl && isPreviewable"
          :src="previewUrl"
          style="width: 100%; height: 70vh; border: none"
        />
        <div v-else class="no-preview">
          <el-icon size="64"><Document /></el-icon>
          <p>该文件类型暂不支持在线预览</p>
          <el-button type="primary" @click="downloadCurrent">下载文件</el-button>
        </div>
      </div>
    </el-dialog>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Document, VideoPlay, Headset, Picture, Folder } from '@element-plus/icons-vue'
import {
  getCourseDetail,
  getCourseAttachments,
  getAttachmentPreviewUrl,
  getAttachmentDownloadUrl,
  enrollCourse,
  dropCourse,
  getMyEnrollments
} from '@/api/course'
import AppLayout from '@/components/AppLayout.vue'

const route = useRoute()
const router = useRouter()

const course = ref({})
const attachments = ref([])
const attachmentSearch = ref('')
const previewVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('')
const currentAttachment = ref(null)
const enrollmentId = ref(null)

const isPreviewable = computed(() => {
  if (!currentAttachment.value) return false
  const type = currentAttachment.value.fileType?.toUpperCase()
  return ['PDF', 'IMAGE', 'VIDEO', 'AUDIO'].includes(type)
})

const fetchCourseDetail = async () => {
  try {
    const res = await getCourseDetail(route.params.id)
    if (res.data.success) {
      course.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取课程详情失败')
  }
}

const fetchAttachments = async () => {
  try {
    const res = await getCourseAttachments(route.params.id)
    if (res.data.success) {
      attachments.value = res.data.data
    }
  } catch (error) {
    console.error('获取附件列表失败', error)
  }
}

const fetchEnrollmentId = async () => {
  try {
    const res = await getMyEnrollments()
    if (res.data.success) {
      const enrollment = res.data.data.find(e => e.courseId === parseInt(route.params.id))
      if (enrollment) {
        enrollmentId.value = enrollment.id
      }
    }
  } catch (error) {
    console.error('获取选课信息失败', error)
  }
}

const goBack = () => {
  router.back()
}

const getDayName = (day) => {
  const days = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return days[day] || ''
}

const getFileIcon = (type) => {
  const icons = {
    PDF: Document,
    DOC: Document,
    DOCX: Document,
    PPT: Document,
    PPTX: Document,
    VIDEO: VideoPlay,
    AUDIO: Headset,
    IMAGE: Picture
  }
  return icons[type?.toUpperCase()] || Folder
}

const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let unitIndex = 0
  let s = size
  while (s >= 1024 && unitIndex < units.length - 1) {
    s /= 1024
    unitIndex++
  }
  return `${s.toFixed(2)} ${units[unitIndex]}`
}

const getCapacityPercent = () => {
  if (!course.value.maxStudents || course.value.maxStudents === 0) return 0
  return Math.round((course.value.currentStudents / course.value.maxStudents) * 100)
}

const getCapacityStatus = () => {
  const percent = getCapacityPercent()
  if (percent >= 100) return 'exception'
  if (percent >= 80) return 'warning'
  return 'success'
}

const isCourseFull = () => {
  return course.value.maxStudents > 0 && course.value.currentStudents >= course.value.maxStudents
}

const handleEnroll = async () => {
  try {
    await ElMessageBox.confirm('确定要选择该课程吗？', '确认选课', { type: 'info' })
    const res = await enrollCourse(course.value.id)
    if (res.data.success) {
      ElMessage.success('选课成功')
      fetchCourseDetail()
      fetchEnrollmentId()
    } else {
      ElMessage.error(res.data.message || '选课失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '选课失败')
    }
  }
}

const handleDrop = async () => {
  try {
    await ElMessageBox.confirm('确定要退选该课程吗？', '确认退课', { type: 'warning' })
    const res = await dropCourse(enrollmentId.value)
    if (res.data.success) {
      ElMessage.success('退课成功')
      course.value.enrolled = false
      enrollmentId.value = null
      fetchCourseDetail()
    } else {
      ElMessage.error(res.data.message || '退课失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '退课失败')
    }
  }
}

const previewAttachment = async (attachment) => {
  try {
    currentAttachment.value = attachment
    previewTitle.value = attachment.fileName
    const res = await getAttachmentPreviewUrl(attachment.id)
    if (res.data.success) {
      previewUrl.value = res.data.data
      previewVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取预览链接失败')
  }
}

const downloadAttachment = async (attachment) => {
  try {
    const res = await getAttachmentDownloadUrl(attachment.id)
    if (res.data.success) {
      window.open(res.data.data, '_blank')
    }
  } catch (error) {
    ElMessage.error('获取下载链接失败')
  }
}

const downloadCurrent = () => {
  if (currentAttachment.value) {
    downloadAttachment(currentAttachment.value)
  }
}

const searchAttachmentContent = () => {
  if (attachmentSearch.value) {
    router.push(`/courses?search=${attachmentSearch.value}`)
  }
}

onMounted(() => {
  fetchCourseDetail()
  fetchAttachments()
  fetchEnrollmentId()
})
</script>

<style scoped>
.course-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.enroll-section {
  text-align: center;
}

.capacity-info {
  margin-bottom: 20px;
}

.capacity-text {
  margin-top: 10px;
  color: #606266;
}

.preview-container {
  min-height: 400px;
}

.no-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #909399;
}

.no-preview p {
  margin: 20px 0;
}
</style>
