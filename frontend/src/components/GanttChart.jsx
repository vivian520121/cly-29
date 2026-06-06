import { Gantt, Task, EventOption, StylingOption, ViewMode } from 'gantt-task-react';
import 'gantt-task-react/dist/index.css';
import { useState, useMemo } from 'react';
import { Typography, Tag, Progress, Tooltip } from 'antd';
import dayjs from 'dayjs';
import StatusTag from './StatusTag';
import PriorityTag from './PriorityTag';

const { Text } = Typography;

const colorMap = {
  phase: '#1890ff',
  milestone: '#722ed1',
  task: '#52c41a',
  bug: '#f5222d',
};

const typeMap = {
  'task': 'task',
  'milestone': 'milestone',
  'project': 'project',
};

const GanttChart = ({
  phases = [],
  milestones = [],
  tasks = [],
  onTaskClick,
  viewMode = ViewMode.Month,
  showProgress = true,
}) => {
  const [selectedTask, setSelectedTask] = useState(null);

  const ganttTasks = useMemo(() => {
    const result = [];
    let idCounter = 1;

    phases.forEach((phase) => {
      result.push({
        id: `phase-${phase.id}`,
        type: 'project',
        name: phase.phaseName,
        start: new Date(phase.startDate),
        end: new Date(phase.endDate),
        progress: phase.progress || 0,
        isDisabled: true,
        styles: {
          progressColor: colorMap.phase,
          backgroundColor: colorMap.phase + '40',
          backgroundSelectedColor: colorMap.phase + '60',
        },
        project: phase,
        hideChildren: false,
      });

      const phaseTasks = tasks.filter((t) => t.phaseId === phase.id);
      phaseTasks.forEach((task) => {
        const taskColor = task.taskType === 4 ? colorMap.bug : colorMap.task;
        result.push({
          id: `task-${task.id}`,
          type: 'task',
          name: task.taskName,
          start: new Date(task.startDate || phase.startDate),
          end: new Date(task.endDate || phase.endDate),
          progress: task.progress || 0,
          dependencies: task.parentId ? [`task-${task.parentId}`] : [],
          styles: {
            progressColor: taskColor,
            backgroundColor: taskColor + '40',
            backgroundSelectedColor: taskColor + '60',
          },
          task,
        });
      });

      idCounter++;
    });

    milestones.forEach((milestone) => {
      result.push({
        id: `milestone-${milestone.id}`,
        type: 'milestone',
        name: milestone.milestoneName,
        start: new Date(milestone.planDate || milestone.actualDate),
        end: new Date(milestone.planDate || milestone.actualDate),
        isDisabled: true,
        styles: {
          backgroundColor: colorMap.milestone,
          backgroundSelectedColor: colorMap.milestone + 'cc',
        },
        milestone,
      });
    });

    return result;
  }, [phases, milestones, tasks]);

  const handleTaskClick = (task) => {
    setSelectedTask(task);
    if (task.task) {
      onTaskClick?.(task.task);
    }
  };

  const formatDate = (date) => {
    return dayjs(date).format('YYYY-MM-DD');
  };

  const getDuration = (start, end) => {
    const startDate = dayjs(start);
    const endDate = dayjs(end);
    return endDate.diff(startDate, 'day') + 1 + '天';
  };

  const renderTooltip = (task) => {
    if (task.task) {
      const t = task.task;
      return (
        <div style={{ padding: 8, minWidth: 200 }}>
          <div style={{ fontWeight: 600, marginBottom: 8 }}>{t.taskName}</div>
          <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
            <StatusTag status={t.status} />
            <PriorityTag priority={t.priority} />
          </div>
          <div style={{ fontSize: 12, color: '#595959', marginBottom: 4 }}>
            <Text strong>工期：</Text>
            {getDuration(t.startDate, t.endDate)} ({formatDate(t.startDate)} ~ {formatDate(t.endDate)})
          </div>
          {showProgress && (
            <div style={{ marginTop: 8 }}>
              <Progress percent={t.progress || 0} size="small" />
            </div>
          )}
          {t.assigneeName && (
            <div style={{ fontSize: 12, color: '#595959', marginTop: 4 }}>
              <Text strong>负责人：</Text>{t.assigneeName}
            </div>
          )}
        </div>
      );
    }

    if (task.project) {
      const p = task.project;
      return (
        <div style={{ padding: 8, minWidth: 200 }}>
          <div style={{ fontWeight: 600, marginBottom: 8 }}>
            <Tag color={colorMap.phase} style={{ margin: 0 }}>阶段</Tag> {p.phaseName}
          </div>
          <div style={{ fontSize: 12, color: '#595959' }}>
            {formatDate(p.startDate)} ~ {formatDate(p.endDate)}
          </div>
          {showProgress && (
            <div style={{ marginTop: 8 }}>
              <Progress percent={p.progress || 0} size="small" />
            </div>
          )}
        </div>
      );
    }

    if (task.milestone) {
      const m = task.milestone;
      return (
        <div style={{ padding: 8, minWidth: 200 }}>
          <div style={{ fontWeight: 600, marginBottom: 8 }}>
            <Tag color={colorMap.milestone} style={{ margin: 0 }}>里程碑</Tag> {m.milestoneName}
          </div>
          <div style={{ fontSize: 12, color: '#595959' }}>
            <Text strong>计划日期：</Text>{formatDate(m.planDate)}
          </div>
          {m.actualDate && (
            <div style={{ fontSize: 12, color: '#595959' }}>
              <Text strong>实际日期：</Text>{formatDate(m.actualDate)}
            </div>
          )}
          <div style={{ marginTop: 8 }}>
            <StatusTag status={m.status} type="milestone" />
          </div>
        </div>
      );
    }

    return null;
  };

  const onViewChange = (mode) => {
  };

  return (
    <div className="gantt-container">
      <Gantt
        tasks={ganttTasks}
        viewMode={viewMode}
        onDateChange={() => {}}
        onProgressChange={() => {}}
        onDoubleClick={handleTaskClick}
        onClick={handleTaskClick}
        columnWidth={60}
        listCellWidth="200px"
        rowHeight={40}
        ganttHeight={500}
        locale="zh-CN"
        barCornerRadius={3}
        viewMode={viewMode}
        onViewChange={onViewChange}
        TaskListHeader={({ headerHeight, rowWidth }) => (
          <div
            style={{
              height: headerHeight,
              width: rowWidth,
              display: 'flex',
              alignItems: 'center',
              padding: '0 8px',
              fontWeight: 600,
              borderBottom: '1px solid #e8e8e8',
              background: '#fafafa',
            }}
          >
            名称
          </div>
        )}
        TaskListTable={({ rowHeight, rowWidth, task, listCellWidth }) => (
          <Tooltip title={renderTooltip(task)} placement="right">
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                padding: '0 8px',
                height: rowHeight,
                width: rowWidth,
                borderBottom: '1px solid #f0f0f0',
                cursor: 'pointer',
                gap: 8,
              }}
            >
              {task.type === 'project' && <Tag color={colorMap.phase}>阶段</Tag>}
              {task.type === 'milestone' && <Tag color={colorMap.milestone}>里程碑</Tag>}
              {task.type === 'task' && task.task?.taskType === 4 && <Tag color={colorMap.bug}>Bug</Tag>}
              <Text ellipsis style={{ flex: 1 }}>{task.name}</Text>
            </div>
          </Tooltip>
        )}
      />
    </div>
  );
};

export default GanttChart;
