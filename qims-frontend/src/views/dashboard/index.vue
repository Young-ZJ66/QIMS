<template>
  <div class="dashboard-container" v-loading="loading">
    <!-- 顶部数据概览卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span>今日新增委托</span>
            <el-tag type="primary" size="small">实时</el-tag>
          </div>
          <div class="card-value">{{ stats.todayDelegations || 0 }} <span class="unit">单</span></div>
          <div class="card-footer">
            较昨日
            <template v-if="stats.delegationGrowth === 'new'">
              <span class="up">全新增</span>
            </template>
            <template v-else>
              <span :class="Number(stats.delegationGrowth) >= 0 ? 'up' : 'down'">
                {{ Number(stats.delegationGrowth) > 0 ? '+' : '' }}{{ stats.delegationGrowth || '0.0' }}%
              </span>
            </template>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span>待检盲样任务</span>
            <el-tag type="warning" size="small">急</el-tag>
          </div>
          <div class="card-value">{{ stats.pendingTasks || 0 }} <span class="unit">个</span></div>
          <div class="card-footer">检测员正在处理中...</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span v-if="stats.isInspector">当月已完成任务</span>
            <span v-else>当月已签发报告</span>
            <el-tag type="success" size="small">月度</el-tag>
          </div>
          <div class="card-value">
            {{ stats.monthReports || 0 }} 
            <span class="unit">{{ stats.isInspector ? '个' : '份' }}</span>
          </div>
          <div class="card-footer">完成率 <span class="up">{{ stats.completionRate || '0.0' }}%</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span>总体合格率</span>
            <el-tag type="danger" size="small">实时</el-tag>
          </div>
          <div class="card-value">{{ stats.passRate || '0.0' }} <span class="unit">%</span></div>
          <div class="card-footer">较上月 <span class="up">+{{ stats.passRateGrowth || '0.0' }}%</span></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表展示区 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-title">近7日检测委托趋势</div>
          </template>
          <!-- ECharts 挂载点 -->
          <div class="mock-chart-bar" ref="barChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-title">不良品缺陷分类分析</div>
          </template>
          <div class="mock-chart-pie" ref="pieChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部待办列表 -->
    <el-row :gutter="20" class="task-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-title">
              <span>系统实时动态</span>
              <el-button type="primary" link @click="fetchStats">刷新动态</el-button>
            </div>
          </template>
          <el-table :data="stats.dynamicLogs" style="width: 100%" size="large" empty-text="暂无动态">
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="action" label="动态类型" width="150">
              <template #default="scope">
                <el-tag :type="scope.row.type">{{ scope.row.action }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="desc" label="详细内容" />
            <el-table-column prop="operator" label="操作人" width="120" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

const loading = ref(false)
const barChartRef = ref(null)
const pieChartRef = ref(null)

const stats = ref({
  todayDelegations: 0,
  pendingTasks: 0,
  monthReports: 0,
  passRate: '0.0',
  trendDates: [],
  trendCounts: [],
  defectData: [],
  dynamicLogs: []
})

const initCharts = () => {
  if (barChartRef.value) {
    const barChart = echarts.init(barChartRef.value)
    barChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: stats.value.trendDates },
      yAxis: { type: 'value' },
      series: [
        {
          data: stats.value.trendCounts,
          type: 'line',
          smooth: true,
          areaStyle: {},
          itemStyle: { color: '#409EFF' }
        }
      ]
    })
  }

  if (pieChartRef.value) {
    const pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      series: [
        {
          name: '缺陷类型',
          type: 'pie',
          radius: '60%',
          data: stats.value.defectData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  }
}

const fetchStats = () => {
  loading.value = true
  request.get('/dashboard/stats').then(res => {
    stats.value = res || {}
    nextTick(() => {
      initCharts()
    })
  }).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard-container {
  padding: 10px;
}

.data-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #666;
  font-size: 14px;
}

.card-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin: 15px 0;
}

.unit {
  font-size: 14px;
  font-weight: normal;
  color: #909399;
}

.card-footer {
  font-size: 13px;
  color: #909399;
  border-top: 1px solid #ebeef5;
  padding-top: 10px;
}

.up {
  color: #f56c6c;
}
.down {
  color: #67c23a;
}

.chart-row {
  margin-top: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mock-chart-bar, .mock-chart-pie {
  height: 300px;
  background-color: #f8f9fb;
  border-radius: 4px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.mock-placeholder {
  text-align: center;
  color: #909399;
}

.mock-placeholder p {
  margin-top: 10px;
  font-size: 14px;
}

.task-row {
  margin-top: 20px;
}
</style>
