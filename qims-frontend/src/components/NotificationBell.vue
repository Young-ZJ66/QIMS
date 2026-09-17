<template>
  <el-popover placement="bottom" :width="360" trigger="click">
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-badge">
        <el-icon :size="20" class="notification-icon"><Bell /></el-icon>
      </el-badge>
    </template>

    <div class="notification-panel">
      <div class="notification-header">
        <span class="notification-title">消息通知</span>
        <el-button v-if="unreadCount > 0" type="primary" link size="small" @click="handleReadAll">
          全部已读
        </el-button>
      </div>

      <div class="notification-list" v-loading="loading">
        <template v-if="notifications.length > 0">
          <div
            v-for="item in notifications"
            :key="item.id"
            class="notification-item"
            :class="{ unread: item.isRead === 0 }"
            @click="handleRead(item)"
          >
            <div class="notification-item-header">
              <el-tag :type="getTagType(item.type)" size="small">{{ item.title }}</el-tag>
              <span class="notification-time">{{ formatTime(item.createTime) }}</span>
            </div>
            <div class="notification-item-content">{{ item.content }}</div>
          </div>
        </template>
        <div v-else class="notification-empty">
          <el-icon :size="40" color="#c0c4cc"><Bell /></el-icon>
          <p>暂无通知</p>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { formatDateTime } from '@/utils/format'

const notifications = ref([])
const unreadCount = ref(0)
const loading = ref(false)

const formatTime = (time) => {
  if (!time) return ''
  const formatted = formatDateTime(time)
  // 只显示时间部分
  return formatted.length > 10 ? formatted.slice(5) : formatted
}

const getTagType = (type) => {
  const map = { info: 'info', warning: 'warning', success: 'success', primary: 'primary' }
  return map[type] || 'info'
}

const fetchNotifications = async () => {
  loading.value = true
  try {
    const [notifs, countRes] = await Promise.all([
      request.get('/notification', { params: { limit: 20 } }),
      request.get('/notification/unread-count')
    ])
    notifications.value = notifs || []
    unreadCount.value = countRes?.count || 0
  } catch (e) {
    // 静默失败
  } finally {
    loading.value = false
  }
}

const handleRead = async (item) => {
  if (item.isRead === 0) {
    try {
      await request.put(`/notification/${item.id}/read`)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) {
      // 静默失败
    }
  }
}

const handleReadAll = async () => {
  try {
    await request.put('/notification/read-all')
    notifications.value.forEach(n => { n.isRead = 1 })
    unreadCount.value = 0
  } catch (e) {
    // 静默失败
  }
}

onMounted(() => {
  fetchNotifications()
  // 每60秒刷新一次
  setInterval(fetchNotifications, 60000)
})

// 暴露刷新方法供父组件调用
defineExpose({ refresh: fetchNotifications })
</script>

<style scoped>
.notification-badge {
  cursor: pointer;
}

.notification-icon {
  color: #606266;
  cursor: pointer;
  transition: color 0.2s;
}

.notification-icon:hover {
  color: #1890ff;
}

.notification-panel {
  margin: -12px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.notification-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.notification-item.unread:hover {
  background-color: #d9ecff;
}

.notification-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-item-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.notification-empty {
  text-align: center;
  padding: 40px 0;
  color: #c0c4cc;
}

.notification-empty p {
  margin-top: 8px;
  font-size: 14px;
}
</style>