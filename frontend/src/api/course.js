import request from './request'

// 获取课程列表
export function getCourses(params) {
  return request.get('/courses', { params })
}

// 获取课程详情
export function getCourseDetail(id) {
  return request.get(`/courses/${id}`)
}

// 创建课程
export function createCourse(data) {
  return request.post('/courses', data)
}

// 更新课程
export function updateCourse(id, data) {
  return request.put(`/courses/${id}`, data)
}

// 删除课程
export function deleteCourse(id) {
  return request.delete(`/courses/${id}`)
}

// 更新课程状态
export function updateCourseStatus(id, status) {
  return request.put(`/courses/${id}/status`, null, { params: { status } })
}

// 获取课程附件列表
export function getCourseAttachments(courseId) {
  return request.get(`/courses/${courseId}/attachments`)
}

// 上传附件
export function uploadAttachment(courseId, file, sortOrder = 0) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('sortOrder', sortOrder)
  return request.post(`/courses/${courseId}/attachments`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取附件预览URL
export function getAttachmentPreviewUrl(id) {
  return request.get(`/attachments/${id}/preview`)
}

// 获取附件下载URL
export function getAttachmentDownloadUrl(id) {
  return request.get(`/attachments/${id}/download`)
}

// 删除附件
export function deleteAttachment(id) {
  return request.delete(`/attachments/${id}`)
}

// 搜索附件
export function searchAttachments(params) {
  return request.get('/attachments/search', { params })
}

// 选课
export function enrollCourse(courseId) {
  return request.post('/enrollments', { courseId })
}

// 退课
export function dropCourse(enrollmentId) {
  return request.delete(`/enrollments/${enrollmentId}`)
}

// 获取我的选课列表
export function getMyEnrollments() {
  return request.get('/enrollments/my')
}

// 获取课程日历数据
export function getCalendarEvents(startDate, endDate) {
  return request.get('/enrollments/calendar', {
    params: { startDate, endDate }
  })
}
