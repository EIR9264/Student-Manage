import request from './request'

// 获取通知列表
export function getNotifications(page = 0, size = 10) {
  return request.get('/notifications', { params: { page, size } })
}

// 获取未读通知数量
export function getUnreadCount() {
  return request.get('/notifications/unread-count')
}

// 标记单条通知已读
export function markAsRead(id) {
  return request.put(`/notifications/${id}/read`)
}

// 标记全部已读
export function markAllAsRead() {
  return request.put('/notifications/read-all')
}
