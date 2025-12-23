import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'
import { connect, disconnect } from '@/utils/websocket'
import { ElNotification } from 'element-plus'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([])
  const unreadCount = ref(0)
  const connected = ref(false)
  const loading = ref(false)

  // 初始化：获取未读数量 + 建立 WebSocket
  async function init() {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (!userInfo.id) return

    await fetchUnreadCount()
    await fetchNotifications()

    // 建立 WebSocket 连接
    connect(userInfo.id, handleNewNotification)
    connected.value = true
  }

  // 获取未读数量
  async function fetchUnreadCount() {
    try {
      const res = await getUnreadCount()
      if (res.data.success) {
        unreadCount.value = res.data.data
      }
    } catch (error) {
      console.error('获取未读数量失败', error)
    }
  }

  // 获取通知列表
  async function fetchNotifications() {
    loading.value = true
    try {
      const res = await getNotifications(0, 10)
      if (res.data.success) {
        notifications.value = res.data.data.content || res.data.data
      }
    } catch (error) {
      console.error('获取通知列表失败', error)
    } finally {
      loading.value = false
    }
  }

  // 处理新通知
  function handleNewNotification(notification) {
    notifications.value.unshift(notification)
    if (notifications.value.length > 10) {
      notifications.value.pop()
    }
    unreadCount.value++

    // 显示桌面通知
    ElNotification({
      title: notification.title,
      message: notification.content,
      type: 'info',
      duration: 5000
    })
  }

  // 标记单条已读
  async function markRead(id) {
    try {
      const res = await markAsRead(id)
      if (res.data.success) {
        const item = notifications.value.find(n => n.id === id)
        if (item && !item.isRead) {
          item.isRead = true
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        }
      }
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }

  // 全部标记已读
  async function markAllRead() {
    try {
      const res = await markAllAsRead()
      if (res.data.success) {
        notifications.value.forEach(n => n.isRead = true)
        unreadCount.value = 0
      }
    } catch (error) {
      console.error('标记全部已读失败', error)
    }
  }

  // 清理
  function cleanup() {
    disconnect()
    connected.value = false
  }

  return {
    notifications,
    unreadCount,
    connected,
    loading,
    init,
    fetchNotifications,
    fetchUnreadCount,
    markRead,
    markAllRead,
    cleanup
  }
})
