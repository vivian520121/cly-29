import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Card,
  Row,
  Col,
  Statistic,
  List,
  Avatar,
  Tag,
  Timeline,
  Progress,
  Button,
  Empty,
  Spin
} from 'antd'
import {
  ProjectOutlined,
  UnorderedListOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ArrowRightOutlined,
  UserOutlined
} from '@ant-design/icons'
import ReactECharts from 'echarts-for-react'
import {
  getDashboardData,
  getTodoTasks,
  getRecentActivities,
  getTaskStatistics
} from '@/services/dashboard'
import dayjs from 'dayjs'
import styles from './Dashboard.module.scss'

const Dashboard = () => {
  const [loading, setLoading] = useState(true)
  const [dashboardData, setDashboardData] = useState(null)
  const [todoTasks, setTodoTasks] = useState([])
  const [activities, setActivities] = useState([])
  const [taskStats, setTaskStats] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const [data, tasks, acts, stats] = await Promise.all([
        getDashboardData(),
        getTodoTasks({ pageNum: 1, pageSize: 5 }),
        getRecentActivities({ pageNum: 1, pageSize: 10 }),
        getTaskStatistics()
      ])
      setDashboardData(data)
      setTodoTasks(tasks?.records || [])
      setActivities(acts?.records || [])
      setTaskStats(stats)
    } catch (error) {
      console.error('Load dashboard error:', error)
    } finally {
      setLoading(false)
    }
  }

  const getChartOption = () => {
    if (!taskStats) return {}
    return {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '任务状态',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 20,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { value: taskStats.todo || 0, name: '待办', itemStyle: { color: '#bfbfbf' } },
            { value: taskStats.inProgress || 0, name: '进行中', itemStyle: { color: '#1890ff' } },
            { value: taskStats.review || 0, name: '评审中', itemStyle: { color: '#faad14' } },
            { value: taskStats.done || 0, name: '已完成', itemStyle: { color: '#52c41a' } },
            { value: taskStats.cancelled || 0, name: '已取消', itemStyle: { color: '#ff4d4f' } }
          ]
        }
      ]
    }
  }

  const getBarChartOption = () => {
    if (!taskStats) return {}
    const priorityData = taskStats.priorityStats || []
    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['高优先级', '中优先级', '低优先级']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '任务数',
          type: 'bar',
          data: [
            priorityData.HIGH || 0,
            priorityData.MEDIUM || 0,
            priorityData.LOW || 0
          ],
          itemStyle: {
            color: function (params) {
              const colorList = ['#ff4d4f', '#faad14', '#1890ff']
              return colorList[params.dataIndex]
            },
            borderRadius: [4, 4, 0, 0]
          },
          barWidth: '40%'
        }
      ]
    }
  }

  const statusColors = {
    TODO: 'default',
    IN_PROGRESS: 'processing',
    REVIEW: 'warning',
    DONE: 'success',
    CANCELLED: 'error'
  }

  const statusText = {
    TODO: '待办',
    IN_PROGRESS: '进行中',
    REVIEW: '评审中',
    DONE: '已完成',
    CANCELLED: '已取消'
  }

  if (loading) {
    return (
      <div className={styles.loadingContainer}>
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div className={styles.dashboard}>
      <div className="page-header">
        <h1 className="page-title">工作台</h1>
        <Button type="primary" onClick={() => navigate('/task/kanban')}>
          查看全部任务 <ArrowRightOutlined />
        </Button>
      </div>

      <Row gutter={[16, 16]} className={styles.statsRow}>
        <Col xs={24} sm={12} lg={6}>
          <Card className={`${styles.statCard} card-hover`}>
            <Statistic
              title={
                <span className={styles.statTitle}>
                  <ProjectOutlined className={styles.statIcon} />
                  项目总数
                </span>
              }
              value={dashboardData?.projectCount || 0}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className={`${styles.statCard} card-hover`}>
            <Statistic
              title={
                <span className={styles.statTitle}>
                  <UnorderedListOutlined className={styles.statIcon} />
                  任务总数
                </span>
              }
              value={dashboardData?.taskCount || 0}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className={`${styles.statCard} card-hover`}>
            <Statistic
              title={
                <span className={styles.statTitle}>
                  <CheckCircleOutlined className={styles.statIcon} />
                  完成率
                </span>
              }
              value={dashboardData?.completionRate || 0}
              precision={1}
              suffix="%"
              valueStyle={{ color: '#52c41a' }}
            />
            <Progress
              percent={dashboardData?.completionRate || 0}
              showInfo={false}
              strokeColor="#52c41a"
              className={styles.statProgress}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className={`${styles.statCard} card-hover`}>
            <Statistic
              title={
                <span className={styles.statTitle}>
                  <ClockCircleOutlined className={styles.statIcon} />
                  进行中任务
                </span>
              }
              value={dashboardData?.inProgressTaskCount || 0}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} className={styles.contentRow}>
        <Col xs={24} lg={12}>
          <Card
            title={
              <div className={styles.cardHeader}>
                <span>待办任务</span>
                <Button
                  type="link"
                  size="small"
                  onClick={() => navigate('/task/list')}
                >
                  查看全部
                </Button>
              </div>
            }
            className={styles.contentCard}
          >
            {todoTasks.length > 0 ? (
              <List
                dataSource={todoTasks}
                renderItem={(task) => (
                  <List.Item
                    key={task.id}
                    className={styles.taskItem}
                    onClick={() => navigate(`/task/${task.id}`)}
                  >
                    <List.Item.Meta
                      avatar={
                        <Avatar src={task.assignee?.avatar}>
                          {task.assignee?.username?.charAt(0)?.toUpperCase() || <UserOutlined />}
                        </Avatar>
                      }
                      title={
                        <div className={styles.taskTitle}>
                          <span className={styles.taskName}>{task.name}</span>
                          <Tag color={statusColors[task.status]}>
                            {statusText[task.status]}
                          </Tag>
                        </div>
                      }
                      description={
                        <div className={styles.taskDesc}>
                          <span className={styles.projectName}>
                            {task.project?.name || '未关联项目'}
                          </span>
                          {task.dueDate && (
                            <span className={styles.dueDate}>
                              <ClockCircleOutlined />
                              {dayjs(task.dueDate).format('YYYY-MM-DD')}
                            </span>
                          )}
                        </div>
                      }
                    />
                  </List.Item>
                )}
              />
            ) : (
              <Empty description="暂无待办任务" />
            )}
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card
            title={
              <div className={styles.cardHeader}>
                <span>项目动态</span>
                <Button
                  type="link"
                  size="small"
                  onClick={() => navigate('/project')}
                >
                  查看全部
                </Button>
              </div>
            }
            className={styles.contentCard}
          >
            {activities.length > 0 ? (
              <Timeline
                mode="left"
                className={styles.timeline}
                items={activities.map((activity, index) => ({
                  color: index === 0 ? 'blue' : 'gray',
                  label: dayjs(activity.createTime).format('MM-DD HH:mm'),
                  children: (
                    <div className={styles.timelineItem}>
                      <span className={styles.activityUser}>
                        {activity.operator?.username || '系统'}
                      </span>
                      <span className={styles.activityAction}>
                        {activity.action}
                      </span>
                      <span
                        className={styles.activityTarget}
                        onClick={() => {
                          if (activity.projectId) {
                            navigate(`/project/${activity.projectId}`)
                          } else if (activity.taskId) {
                            navigate(`/task/${activity.taskId}`)
                          }
                        }}
                      >
                        {activity.targetName}
                      </span>
                    </div>
                  )
                }))}
              />
            ) : (
              <Empty description="暂无动态" />
            )}
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} className={styles.chartRow}>
        <Col xs={24} lg={12}>
          <Card title="任务状态统计" className={styles.chartCard}>
            <ReactECharts
              option={getChartOption()}
              style={{ height: 300 }}
              notMerge
              lazyUpdate
            />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="优先级分布" className={styles.chartCard}>
            <ReactECharts
              option={getBarChartOption()}
              style={{ height: 300 }}
              notMerge
              lazyUpdate
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Dashboard
