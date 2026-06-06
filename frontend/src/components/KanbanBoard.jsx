import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragOverlay,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Card, Badge, Avatar, Typography, Progress, Space, Empty } from 'antd';
import { useState, useMemo } from 'react';
import { UserOutlined } from '@ant-design/icons';
import StatusTag from './StatusTag';
import PriorityTag from './PriorityTag';
import dayjs from 'dayjs';

const { Text, Title } = Typography;

const statusConfig = [
  { status: 1, name: '待办', color: '#f5222d' },
  { status: 2, name: '进行中', color: '#1890ff' },
  { status: 3, name: '审核中', color: '#fa8c16' },
  { status: 4, name: '已完成', color: '#52c41a' },
  { status: 5, name: '已取消', color: '#8c8c8c' },
];

const SortableTaskCard = ({ task, onClick }) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: task.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
    marginBottom: 12,
  };

  return (
    <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
      <Card
        size="small"
        className="card-hover"
        onClick={() => onClick?.(task)}
        styles={{ body: { padding: 12 } }}
      >
        <div style={{ marginBottom: 8 }}>
          <Text strong className="text-ellipsis-2" style={{ display: '-webkit-box' }}>
            {task.taskName}
          </Text>
        </div>

        <Space size={4} wrap style={{ marginBottom: 8 }}>
          <StatusTag status={task.status} />
          <PriorityTag priority={task.priority} />
          {task.taskNo && (
            <Text type="secondary" style={{ fontSize: 12 }}>
              #{task.taskNo}
            </Text>
          )}
        </Space>

        {task.progress !== undefined && task.progress !== null && (
          <Progress
            percent={task.progress}
            size="small"
            showInfo={false}
            strokeColor={task.progress === 100 ? '#52c41a' : '#1890ff'}
            style={{ marginBottom: 8 }}
          />
        )}

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Space size={4}>
            {task.assigneeAvatar || task.assigneeName ? (
              <Avatar size={20} src={task.assigneeAvatar} icon={<UserOutlined />} />
            ) : null}
            {task.assigneeName && (
              <Text type="secondary" style={{ fontSize: 12 }}>
                {task.assigneeName}
              </Text>
            )}
          </Space>
          {task.endDate && (
            <Text
              type={dayjs(task.endDate).isBefore(dayjs(), 'day') ? 'danger' : 'secondary'}
              style={{ fontSize: 12 }}
            >
              {dayjs(task.endDate).format('MM-DD')}
            </Text>
          )}
        </div>

        {task.subtaskCount > 0 && (
          <div style={{ marginTop: 8, fontSize: 12, color: '#8c8c8c' }}>
            子任务：{task.completedSubtaskCount || 0}/{task.subtaskCount}
          </div>
        )}
      </Card>
    </div>
  );
};

const KanbanColumn = ({ status, name, color, tasks = [], onTaskClick, onTaskDrop }) => {
  const { setNodeRef } = useSortable({ id: `column-${status}` });

  return (
    <div
      ref={setNodeRef}
      style={{
        flex: 1,
        minWidth: 280,
        background: '#f5f5f5',
        borderRadius: 8,
        padding: 12,
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: 12 }}>
        <div
          style={{
            width: 4,
            height: 16,
            background: color,
            borderRadius: 2,
            marginRight: 8,
          }}
        />
        <Title level={5} style={{ margin: 0, flex: 1 }}>{name}</Title>
        <Badge count={tasks.length} color={color} />
      </div>

      <div style={{ flex: 1, overflowY: 'auto' }}>
        <SortableContext
          items={tasks.map((t) => t.id)}
          strategy={verticalListSortingStrategy}
        >
          {tasks.length === 0 ? (
            <Empty description="暂无任务" image={Empty.PRESENTED_IMAGE_SIMPLE} />
          ) : (
            tasks.map((task) => (
              <SortableTaskCard key={task.id} task={task} onClick={onTaskClick} />
            ))
          )}
        </SortableContext>
      </div>
    </div>
  );
};

const KanbanBoard = ({ columns = [], onTaskClick, onTaskStatusChange, onTaskReorder }) => {
  const [activeId, setActiveId] = useState(null);
  const [activeTask, setActiveTask] = useState(null);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const kanbanColumns = useMemo(() => {
    if (columns && columns.length > 0) {
      return columns.map((col) => ({
        status: col.status,
        name: col.statusName,
        color: col.statusColor,
        tasks: col.tasks || [],
      }));
    }

    return statusConfig.map((config) => ({
      ...config,
      tasks: [],
    }));
  }, [columns]);

  const findTask = (id) => {
    for (const col of kanbanColumns) {
      const task = col.tasks.find((t) => t.id === id);
      if (task) return { task, column: col };
    }
    return null;
  };

  const findColumn = (id) => {
    if (typeof id === 'string' && id.startsWith('column-')) {
      const status = parseInt(id.replace('column-', ''));
      return kanbanColumns.find((col) => col.status === status);
    }
    const result = findTask(id);
    return result?.column;
  };

  const handleDragStart = (event) => {
    const { active } = event;
    setActiveId(active.id);
    const result = findTask(active.id);
    setActiveTask(result?.task);
  };

  const handleDragOver = (event) => {
    const { active, over } = event;
    if (!over) return;

    const activeColumn = findColumn(active.id);
    const overColumn = findColumn(over.id);

    if (!activeColumn || !overColumn) return;

    if (activeColumn.status !== overColumn.status) {
      const activeIndex = activeColumn.tasks.findIndex((t) => t.id === active.id);
      if (activeIndex !== -1) {
        const [movedTask] = activeColumn.tasks.splice(activeIndex, 1);
        movedTask.status = overColumn.status;
        overColumn.tasks.push(movedTask);
      }
    }
  };

  const handleDragEnd = (event) => {
    const { active, over } = event;
    setActiveId(null);
    setActiveTask(null);

    if (!over) return;

    const activeResult = findTask(active.id);
    const overResult = findTask(over.id);
    const overColumn = findColumn(over.id);

    if (!activeResult) return;

    if (activeResult.task.status !== overColumn?.status) {
      onTaskStatusChange?.(activeResult.task.id, overColumn.status);
    } else if (overResult && active.id !== over.id) {
      const oldIndex = activeResult.column.tasks.findIndex((t) => t.id === active.id);
      const newIndex = activeResult.column.tasks.findIndex((t) => t.id === over.id);
      if (oldIndex !== newIndex) {
        const newTasks = arrayMove(activeResult.column.tasks, oldIndex, newIndex);
        onTaskReorder?.(
          activeResult.column.status,
          newTasks.map((t, i) => ({ id: t.id, sortOrder: i }))
        );
      }
    }
  };

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragStart={handleDragStart}
      onDragOver={handleDragOver}
      onDragEnd={handleDragEnd}
    >
      <div style={{ display: 'flex', gap: 16, height: 'calc(100vh - 200px)', overflowX: 'auto' }}>
        <SortableContext items={kanbanColumns.map((c) => `column-${c.status}`)}>
          {kanbanColumns.map((col) => (
            <KanbanColumn
              key={col.status}
              status={col.status}
              name={col.name}
              color={col.color}
              tasks={col.tasks}
              onTaskClick={onTaskClick}
            />
          ))}
        </SortableContext>
      </div>

      <DragOverlay>
        {activeTask && (
          <Card size="small" style={{ width: 280, boxShadow: '0 8px 24px rgba(0,0,0,0.15)' }}>
            <Text strong>{activeTask.taskName}</Text>
          </Card>
        )}
      </DragOverlay>
    </DndContext>
  );
};

export default KanbanBoard;
