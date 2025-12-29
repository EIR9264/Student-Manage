<template>
  <AppLayout>
    <div class="course-manage-container">
    <el-card class="header-card">
      <div class="header-content">
        <h2>课程管理</h2>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          新建课程
        </el-button>
      </div>
    </el-card>

    <el-card>
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索课程"
          clearable
          style="width: 300px"
          @keyup.enter="fetchCourses"
        />
        <el-button type="primary" @click="fetchCourses">搜索</el-button>
      </div>

      <el-table :data="courses" stripe style="width: 100%">
        <el-table-column prop="courseCode" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column label="选课人数" width="120">
          <template #default="{ row }">
            {{ row.currentStudents }}/{{ row.maxStudents || '不限' }}
          </template>
        </el-table-column>
        <el-table-column prop="courseType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.courseType === 'REQUIRED' ? 'danger' : 'success'" size="small">
              {{ row.courseType === 'REQUIRED' ? '必修' : '选修' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '开放' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="warning" link @click="manageAttachments(row)">附件</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchCourses"
        />
      </div>
    </el-card>

    <!-- 课程表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课程' : '新建课程'" width="600px">
      <el-form ref="formRef" :model="courseForm" :rules="formRules" label-width="100px">
        <el-form-item label="课程编号" prop="courseCode">
          <el-input v-model="courseForm.courseCode" placeholder="如：CS101" />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="courseForm.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherName">
          <el-input v-model="courseForm.teacherName" placeholder="请输入教师姓名" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学分" prop="credit">
              <el-input-number v-model="courseForm.credit" :min="0" :max="10" :precision="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大人数" prop="maxStudents">
              <el-input-number v-model="courseForm.maxStudents" :min="0" placeholder="0表示不限" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="课程类型" prop="courseType">
          <el-radio-group v-model="courseForm.courseType">
            <el-radio value="REQUIRED">必修</el-radio>
            <el-radio value="ELECTIVE">选修</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开课日期">
              <el-date-picker v-model="courseForm.startDate" type="date" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结课日期">
              <el-date-picker v-model="courseForm.endDate" type="date" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="课程描述">
          <el-input v-model="courseForm.description" type="textarea" :rows="3" />
        </el-form-item>

        <el-divider>上课时间安排</el-divider>
        <div v-for="(schedule, index) in courseForm.schedules" :key="index" class="schedule-item">
          <el-row :gutter="10">
            <el-col :span="5">
              <el-select v-model="schedule.dayOfWeek" placeholder="星期">
                <el-option v-for="d in 7" :key="d" :label="getDayName(d)" :value="d" />
              </el-select>
            </el-col>
            <el-col :span="5">
              <el-select v-model="schedule.sectionStart" placeholder="开始节次" @change="onSectionStartChange(schedule)">
                <el-option v-for="s in 12" :key="s" :label="`第${s}节`" :value="s" />
              </el-select>
            </el-col>
            <el-col :span="5">
              <el-select v-model="schedule.sectionEnd" placeholder="结束节次">
                <el-option v-for="s in 12" :key="s" :label="`第${s}节`" :value="s" :disabled="s < (schedule.sectionStart || 1)" />
              </el-select>
            </el-col>
            <el-col :span="5">
              <el-input v-model="schedule.classroom" placeholder="教室" />
            </el-col>
            <el-col :span="4">
              <span class="section-time-hint">{{ getSectionTimeHint(schedule) }}</span>
              <el-button type="danger" circle size="small" @click="removeSchedule(index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-col>
          </el-row>
        </div>
        <el-button type="primary" plain @click="addSchedule" style="width: 100%">
          <el-icon><Plus /></el-icon>
          添加时间安排
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 附件管理对话框 -->
    <el-dialog v-model="attachmentDialogVisible" title="附件管理" width="700px">
      <el-upload
        class="upload-area"
        drag
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        multiple
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持 PDF、Word、PPT、视频、音频等格式</div>
        </template>
      </el-upload>

      <el-table :data="attachments" style="margin-top: 20px">
        <el-table-column prop="fileName" label="文件名" min-width="200" />
        <el-table-column prop="fileType" label="类型" width="80" />
        <el-table-column label="大小" width="100">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDeleteAttachment(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, UploadFilled } from '@element-plus/icons-vue'
import {
  getCourses,
  getCourseDetail,
  createCourse,
  updateCourse,
  deleteCourse,
  getCourseAttachments,
  deleteAttachment
} from '@/api/course'
import AppLayout from '@/components/AppLayout.vue'

const courses = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const courseForm = ref(getEmptyForm())

const attachmentDialogVisible = ref(false)
const currentCourseId = ref(null)
const attachments = ref([])

const uploadUrl = computed(() => `http://localhost:8080/api/courses/${currentCourseId.value}/attachments`)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const formRules = {
  courseCode: [{ required: true, message: '请输入课程编号', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }]
}

function getEmptyForm() {
  return {
    courseCode: '',
    courseName: '',
    description: '',
    teacherName: '',
    credit: 0,
    maxStudents: 0,
    courseType: 'REQUIRED',
    startDate: null,
    endDate: null,
    schedules: []
  }
}

const fetchCourses = async () => {
  try {
    const res = await getCourses({
      keyword: searchKeyword.value,
      status: '',
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

const openCreateDialog = () => {
  isEdit.value = false
  courseForm.value = getEmptyForm()
  dialogVisible.value = true
}

const openEditDialog = async (course) => {
  isEdit.value = true
  try {
    // 获取课程详情（包含schedules）
    const res = await getCourseDetail(course.id)
    if (res.data.success) {
      courseForm.value = { ...res.data.data }
      if (!courseForm.value.schedules) {
        courseForm.value.schedules = []
      }
    } else {
      courseForm.value = { ...course }
      courseForm.value.schedules = []
    }
  } catch (error) {
    console.error('获取课程详情失败', error)
    courseForm.value = { ...course }
    courseForm.value.schedules = []
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      const res = await updateCourse(courseForm.value.id, courseForm.value)
      if (res.data.success) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchCourses()
      }
    } else {
      const res = await createCourse(courseForm.value)
      if (res.data.success) {
        ElMessage.success('创建成功')
        dialogVisible.value = false
        fetchCourses()
      }
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

const handleDelete = async (course) => {
  try {
    await ElMessageBox.confirm(`确定要删除课程「${course.courseName}」吗？`, '确认删除', { type: 'warning' })
    const res = await deleteCourse(course.id)
    if (res.data.success) {
      ElMessage.success('删除成功')
      fetchCourses()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const manageAttachments = async (course) => {
  currentCourseId.value = course.id
  attachmentDialogVisible.value = true
  await fetchAttachments()
}

const fetchAttachments = async () => {
  try {
    const res = await getCourseAttachments(currentCourseId.value)
    if (res.data.success) {
      attachments.value = res.data.data
    }
  } catch (error) {
    console.error('获取附件失败', error)
  }
}

const handleUploadSuccess = () => {
  ElMessage.success('上传成功')
  fetchAttachments()
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const handleDeleteAttachment = async (attachment) => {
  try {
    await ElMessageBox.confirm('确定要删除该附件吗？', '确认删除', { type: 'warning' })
    const res = await deleteAttachment(attachment.id)
    if (res.data.success) {
      ElMessage.success('删除成功')
      fetchAttachments()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getDayName = (day) => {
  const days = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return days[day]
}

// 节次时间映射
const sectionTimeMap = {
  1: { start: '08:00', end: '08:50' },
  2: { start: '08:50', end: '09:40' },
  3: { start: '10:00', end: '10:50' },
  4: { start: '10:50', end: '11:40' },
  5: { start: '14:00', end: '14:50' },
  6: { start: '14:50', end: '15:40' },
  7: { start: '16:00', end: '16:50' },
  8: { start: '16:50', end: '17:40' },
  9: { start: '19:00', end: '19:50' },
  10: { start: '19:50', end: '20:40' },
  11: { start: '21:00', end: '21:50' },
  12: { start: '21:50', end: '22:40' }
}

// 获取节次时间提示
const getSectionTimeHint = (schedule) => {
  if (!schedule.sectionStart || !schedule.sectionEnd) return ''
  const start = sectionTimeMap[schedule.sectionStart]?.start || ''
  const end = sectionTimeMap[schedule.sectionEnd]?.end || ''
  return start && end ? `${start}-${end}` : ''
}

// 当开始节次改变时，自动调整结束节次
const onSectionStartChange = (schedule) => {
  if (!schedule.sectionEnd || schedule.sectionEnd < schedule.sectionStart) {
    schedule.sectionEnd = schedule.sectionStart
  }
}

const addSchedule = () => {
  courseForm.value.schedules.push({
    dayOfWeek: 1,
    sectionStart: 1,
    sectionEnd: 2,
    classroom: '',
    weekStart: 1,
    weekEnd: 18,
    weekType: 'ALL'
  })
}

const removeSchedule = (index) => {
  courseForm.value.schedules.splice(index, 1)
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

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.course-manage-container {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.schedule-item {
  margin-bottom: 10px;
}

.upload-area {
  width: 100%;
}
</style>
