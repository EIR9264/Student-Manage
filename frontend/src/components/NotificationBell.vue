<template>
  <el-popover
    placement="bottom"
    :width="360"
    trigger="click"
    @show="onShow"
  >
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
        <el-button :icon="Bell" round>
          通知
        </el-button>
      </el-badge>
    </template>

    <div class="notification-panel">
      <div class="panel-header">
        <span class="title">通知</span>
        <el-button v-if="unreadCount > 0" type="primary" link @click="handleMarkAllRead">
          全部已读
        </el-button>
      </div>

      <el-scrollbar height="300px">
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading"><Loading /></el-icon>
        </div>

        <div v-else-if="notifications.length === 0" class="empty-container">
          <el-empty description="暂无通知" :image-size="80" />
        </div>

        <div v-else class="notification-list">
          <div
            v-for="item in notifications"
            :key="item.id"
            class="notification-item"
            :class="{ unread: !item.isRead }"
            @click="handleClick(item)"
          >
            <div class="item-header">
              <span class="item-title">{{ item.title }}</span>
              <span class="item-time">{{ formatTime(item.createdAt) }}</span>
            </div>
            <div class="item-content">{{ item.content }}</div>
          </div>
        </div>
      </el-scrollbar>
    </div>
  </el-popover>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Loading } from '@element-plus/icons-vue'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const store = useNotificationStore()

const notifications = computed(() => store.notifications)
const unreadCount = computed(() => store.unreadCount)
const loading = computed(() => store.loading)

function onShow() {
  store.fetchNotifications()
}

function handleMarkAllRead() {
  store.markAllRead()
}

function handleClick(item) {
  if (!item.isRead) {
    store.markRead(item.id)
  }
  if (item.relatedId && item.type === 'ENROLL_SUCCESS') {
    router.push(`/courses/${item.relatedId}`)
  }
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${date.getMonth() + 1}/${date.getDate()}`
}
</script>

<style scoped>
.notification-panel {
  margin: -12px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.panel-header .title {
  font-weight: 600;
  font-size: 16px;
}

.loading-container,
.empty-container {
  padding: 40px 0;
  text-align: center;
}

.notification-list {
  padding: 8px 0;
}

.notification-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.item-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.item-title {
  font-weight: 500;
  color: #303133;
}

.item-time {
  font-size: 12px;
  color: #909399;
}

.item-content {
  font-size: 13px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
