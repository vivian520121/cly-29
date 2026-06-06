import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Card,
  Button,
  Tag,
  Avatar,
  Dropdown,
  Menu,
  message,
  Spin,
  Empty
} from 'antd'
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragOverlay
} from '@dnd-kit/core'
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
  useSortable
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { PlusOutlined, MoreOutlined, UserOutlined } from '@ant-design/icons'
import { getTaskKanban, updateTaskStatus } from '@/services/task'
import TaskCard from '@/components/TaskCard'
import dayjs from 'dayjs'
import styles from './TaskKanban.module.scss'

const columns = [
  { id: 'TODO', title: '待办', color: '#bfbfbf' },
  { id: 'IN_PROGRESS', title: '进行中', color: '#1890ff' },
  { id: 'REVIEW', title: '评审中', color: '#faad14' },
  { id: 'DONE', title: '已完成', color: '#52c41a' },
  { id: 'CANCELLED', title: '已取消', color: '#ff4d4f' }
]

const SortableTask = ({ task, onClick }) => {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: task.id
  })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1
  }

  return (
    <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
      <TaskCard task={task} onClick={onClick} draggable />
    </div>
  )
}

const TaskKanban = () => {
  const [loading, setLoading] = useState(true)
  const [kanbanData, setKanbanData] = useState({})
  const [activeId, setActiveId] = useState(null)
  const [activeTask, setActiveTask] = useState(null)
  const navigate = useNavigate()

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates
    })
  )

  useEffect(() => {
    loadKanbanData()
  }, [])

  const loadKanbanData = async () => {
    setLoading(true)
    try {
      const data = await getTaskKanban()
      const formatted = {}
      columns.forEach((col) => {
        formatted[col.id] = data?.[col.id] || []
      })
      setKanbanData(formatted)
    } catch (error) {
      console.error('Load kanban error:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleDragStart = (event) => {
    const { active } = event
    setActiveId(active.id)
    let task = null
    Object.values(kanbanData).forEach((tasks) => {
      const found = tasks.find((t) => t.id === active.id)
      if (found) task = found
    })
    setActiveTask(task)
  }

  const handleDragEnd = async (event) => {
    const { active, over } = event
    setActiveId(null)
    setActiveTask(null)

    if (!over) return

    const activeId = active.id
    const overId = over.id

    let sourceColumn = null
    let targetColumn = null
    let activeTaskData = null

    Object.entries(kanbanData).forEach(([columnId, tasks]) => {
      if (tasks.find((t) => t.id === activeId)) {
        sourceColumn = columnId
        activeTaskData = tasks.find((t) => t.id === activeId)
      }
      if (tasks.find((t) => t.id === overId)) {
        targetColumn = columnId
      }
    })

    if (columns.some((col) => col.id === overId)) {
      targetColumn = overId
    }

    if (!sourceColumn || !targetColumn) return

    if (sourceColumn === targetColumn) {
      const tasks = kanbanData[sourceColumn]
      const oldIndex = tasks.findIndex((t) => t.id === activeId)
      const newIndex = tasks.findIndex((t) => t.id === overId)
      if (oldIndex !== -1 && newIndex !== -1 && oldIndex !== newIndex) {
        setKanbanData((prev) => ({
          ...prev,
          [sourceColumn]: arrayMove(tasks, oldIndex, newIndex)
        }))
      }
    } else {
      const sourceTasks = kanbanData[sourceColumn]
      const targetTasks = kanbanData[targetColumn]
      const taskToMove = sourceTasks.find((t) => t.id === activeId)
      if (!taskToMove) return

      const overIndex = targetTasks.findIndex((t) => t.id === overId)
      const insertIndex = overIndex === -1 ? targetTasks.length : overIndex

      const newSourceTasks = sourceTasks.filter((t) => t.id !== activeId)
      const newTargetTasks = [...targetTasks]
      newTargetTasks.splice(insertIndex, 0, { ...taskToMove, status: targetColumn })

      setKanbanData((prev) => ({
        ...prev,
        [sourceColumn]: newSourceTasks,
        [targetColumn]: newTargetTasks
      }))

      try {
        await updateTaskStatus(activeId, { status: targetColumn })
        message.success('状态更新成功')
      } catch (error) {
        console.error('Update status error:', error)
        message.error('状态更新失败')
        loadKanbanData()
      }
    }
  }

  const handleTaskClick = (task) => {
    navigate(`/task/${task.id}`)
  }

  if (loading) {
    return (
      <div className={styles.loadingContainer}>
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div className={styles.taskKanban}>
      <div className="page-header">
        <h1 className="page-title">任务看板</h1>
        <div className={styles.headerActions}>
          <Button type="primary" icon={<PlusOutlined />}>
            新建任务
          </Button>
        </div>
      </div>

      <div className={styles.kanbanContainer}>
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragStart={handleDragStart}
          onDragEnd={handleDragEnd}
        >
          <div className={styles.kanbanBoard}>
            {columns.map((column) => (
              <div key={column.id} className={styles.kanbanColumn}>
                <div className={styles.columnHeader}>
                  <div className={styles.columnTitle}>
                    <span
                      className={styles.columnDot}
                      style={{ backgroundColor: column.color }}
                    />
                    <span>{column.title}</span>
                    <Tag className={styles.columnCount}>
                      {kanbanData[column.id]?.length || 0}
                    </Tag>
                  </div>
                  <Dropdown
                    menu={{
                      items: [
                        { key: 'add', label: '添加任务', icon: <PlusOutlined /> }
                      ]
                    }}
                    trigger={['click']}
                  >
                    <MoreOutlined className={styles.moreIcon} />
                  </Dropdown>
                </div>

                <div className={styles.columnContent}>
                  <SortableContext
                    items={kanbanData[column.id]?.map((t) => t.id) || []}
                    strategy={verticalListSortingStrategy}
                  >
                    {kanbanData[column.id]?.length > 0 ? (
                      kanbanData[column.id].map((task) => (
                        <SortableTask
                          key={task.id}
                          task={task}
                          onClick={handleTaskClick}
                        />
                      ))
                    ) : (
                      <Empty
                        description="暂无任务"
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        className={styles.emptyColumn}
                      />
                    )}
                  </SortableContext>
                </div>
              </div>
            ))}
          </div>

          <DragOverlay>
            {activeId && activeTask ? (
              <div className={styles.dragOverlay}>
                <TaskCard task={activeTask} />
              </div>
            ) : null}
          </DragOverlay>
        </DndContext>
      </div>
    </div>
  )
}

export default TaskKanban
