import { Card, Tag, Avatar, Tooltip } from 'antd'
import {
  ClockCircleOutlined,
  UserOutlined,
  FlagOutlined
} from '@ant-design/icons'
import dayjs from 'dayjs'
import styles from './TaskCard.module.scss'

const priorityColors = {
  HIGH: 'red',
  MEDIUM: 'orange',
  LOW: 'blue'
}

const priorityText = {
  HIGH: '高',
  MEDIUM: '中',
  LOW: '低'
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

const TaskCard = ({ task, onClick, draggable, dragHandleProps }) => {
  const isOverdue = task.dueDate && dayjs(task.dueDate).isBefore(dayjs(), 'day')

  return (
    <Card
      className={`${styles.taskCard} card-hover`}
      onClick={() => onClick?.(task)}
      {...dragHandleProps}
    >
      <div className={styles.cardHeader}>
        <div className={styles.taskTitle}>{task.name}</div>
        <Tag color={statusColors[task.status]}>
          {statusText[task.status]}
        </Tag>
      </div>

      <div className={styles.taskMeta}>
        <div className={styles.metaItem}>
          <FlagOutlined
            style={{ color: priorityColors[task.priority] === 'red' ? '#ff4d4f' : priorityColors[task.priority] === 'orange' ? '#fa8c16' : '#1890ff' }}
          />
          <span className={styles.metaLabel}>优先级</span>
          <Tag
            color={priorityColors[task.priority]}
            className={styles.metaTag}
          >
            {priorityText[task.priority]}
          </Tag>
        </div>

        <div className={styles.metaItem}>
          <UserOutlined />
          <span className={styles.metaLabel}>负责人</span>
          <Tooltip title={task.assignee?.username || '未分配'}>
            <Avatar
              size="small"
              src={task.assignee?.avatar}
              className={styles.assigneeAvatar}
            >
              {task.assignee?.username?.charAt(0)?.toUpperCase() || '?'}
            </Avatar>
          </Tooltip>
        </div>

        <div className={styles.metaItem}>
          <ClockCircleOutlined className={isOverdue ? styles.overdue : ''} />
          <span className={styles.metaLabel}>截止日期</span>
          <span
            className={`${styles.dueDate} ${isOverdue ? styles.overdue : ''}`}
          >
            {task.dueDate ? dayjs(task.dueDate).format('YYYY-MM-DD') : '无'}
          </span>
        </div>
      </div>

      {task.description && (
        <div className={styles.taskDesc}>
          {task.description}
        </div>
      )}

      {task.tags && task.tags.length > 0 && (
        <div className={styles.taskTags}>
          {task.tags.map((tag) => (
            <Tag key={tag.id} color="blue">
              {tag.name}
            </Tag>
          ))}
        </div>
      )}
    </Card>
  )
}

export default TaskCard
