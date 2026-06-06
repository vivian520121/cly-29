import { Timeline, Avatar, Typography, Card, Empty, Spin, Tag, Button } from 'antd';
import {
  UserOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  MessageOutlined,
  FileOutlined,
} from '@ant-design/icons';
import { useState, useEffect } from 'react';
import dayjs from 'dayjs';
import request from '@/utils/request';
import useRequest from '@/hooks/useRequest';

const { Text, Title } = Typography;

const actionIconMap = {
  CREATE: <PlusOutlined style={{ color: '#52c41a' }} />,
  UPDATE: <EditOutlined style={{ color: '#1890ff' }} />,
  DELETE: <DeleteOutlined style={{ color: '#f5222d' }} />,
  STATUS_CHANGE: <ClockCircleOutlined style={{ color: '#fa8c16' }} />,
  COMMENT: <MessageOutlined style={{ color: '#722ed1' }} />,
  FILE_UPLOAD: <FileOutlined style={{ color: '#13c2c2' }} />,
  COMPLETE: <CheckCircleOutlined style={{ color: '#52c41a' }} />,
};

const actionColorMap = {
  CREATE: '#52c41a',
  UPDATE: '#1890ff',
  DELETE: '#f5222d',
  STATUS_CHANGE: '#fa8c16',
  COMMENT: '#722ed1',
  FILE_UPLOAD: '#13c2c2',
  COMPLETE: '#52c41a',
};

const fieldNameMap = {
  taskName: '任务名称',
  description: '任务描述',
  status: '任务状态',
  priority: '优先级',
  assigneeId: '负责人',
  startDate: '开始日期',
  endDate: '结束日期',
  progress: '进度',
  estimateHours: '预估工时',
  phaseId: '所属阶段',
  taskType: '任务类型',
};

const statusMap = {
  1: '待办',
  2: '进行中',
  3: '审核中',
  4: '已完成',
  5: '已取消',
};

const priorityMap = {
  1: '紧急',
  2: '高',
  3: '中',
  4: '低',
};

const taskTypeMap = {
  1: '需求',
  2: '开发',
  3: '测试',
  4: 'Bug',
  5: '优化',
};

const formatValue = (fieldName, value) => {
  if (value === null || value === undefined) return '-';
  if (fieldName === 'status') return statusMap[value] || value;
  if (fieldName === 'priority') return priorityMap[value] || value;
  if (fieldName === 'taskType') return taskTypeMap[value] || value;
  if (fieldName === 'startDate' || fieldName === 'endDate') {
    return dayjs(value).format('YYYY-MM-DD');
  }
  if (fieldName === 'progress') return `${value}%`;
  if (fieldName === 'estimateHours') return `${value} 小时`;
  return value;
};

const DynamicTimeline = ({ taskId, projectId, pageSize = 20 }) => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [pageNum, setPageNum] = useState(1);

  const fetchLogs = async (page = 1, reset = false) => {
    if (loading) return;
    setLoading(true);
    try {
      const params = {
        pageNum: page,
        pageSize,
      };
      if (taskId) params.taskId = taskId;
      if (projectId) params.projectId = projectId;

      const data = await request({
        url: '/task/log/list',
        method: 'GET',
        params,
      });

      const records = data?.records || [];
      if (reset) {
        setLogs(records);
        setPageNum(1);
      } else {
        setLogs((prev) => [...prev, ...records]);
        setPageNum(page);
      }
      setHasMore(records.length >= pageSize);
    } catch (error) {
      console.error('获取动态列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setLogs([]);
    setPageNum(1);
    setHasMore(true);
    fetchLogs(1, true);
  }, [taskId, projectId]);

  const handleLoadMore = () => {
    if (hasMore && !loading) {
      fetchLogs(pageNum + 1);
    }
  };

  const renderActionContent = (log) => {
    const actionType = log.actionType;
    const actionTypeName = log.actionTypeName || actionType;

    if (actionType === 'CREATE') {
      return (
        <div>
          <Text strong>{log.realName || log.username}</Text> 创建了任务
          {log.newValue && (
            <Text type="secondary">：{log.newValue}</Text>
          )}
        </div>
      );
    }

    if (actionType === 'UPDATE' && log.fieldName) {
      const fieldLabel = fieldNameMap[log.fieldName] || log.fieldName;
      return (
        <div>
          <Text strong>{log.realName || log.username}</Text> 更新了
          <Tag color="blue" style={{ margin: '0 4px' }}>{fieldLabel}</Tag>
          {log.oldValue !== undefined && log.oldValue !== null && (
            <span>
              从 <Text delete type="secondary">{formatValue(log.fieldName, log.oldValue)}</Text>{' '}
            </span>
          )}
          {log.newValue !== undefined && log.newValue !== null && (
            <span>
              改为 <Text strong>{formatValue(log.fieldName, log.newValue)}</Text>
            </span>
          )}
        </div>
      );
    }

    if (actionType === 'STATUS_CHANGE') {
      return (
        <div>
          <Text strong>{log.realName || log.username}</Text> 变更了状态
          {log.oldValue && (
            <span>
              从 <Tag color="default">{statusMap[log.oldValue] || log.oldValue}</Tag>{' '}
            </span>
          )}
          {log.newValue && (
            <span>
              改为 <Tag color="processing">{statusMap[log.newValue] || log.newValue}</Tag>
            </span>
          )}
        </div>
      );
    }

    if (actionType === 'COMMENT') {
      return (
        <div>
          <Text strong>{log.realName || log.username}</Text> 发表了评论
          {log.remark && (
            <div style={{
              background: '#f5f5f5',
              padding: '8px 12px',
              borderRadius: 4,
              marginTop: 8,
              whiteSpace: 'pre-wrap',
            }}>
              {log.remark}
            </div>
          )}
        </div>
      );
    }

    if (actionType === 'FILE_UPLOAD') {
      return (
        <div>
          <Text strong>{log.realName || log.username}</Text> 上传了文件
          {log.newValue && (
            <Text type="secondary">：{log.newValue}</Text>
          )}
        </div>
      );
    }

    if (actionType === 'DELETE') {
      return (
        <div>
          <Text strong>{log.realName || log.username}</Text> 删除了任务
          {log.oldValue && (
            <Text type="secondary">：{log.oldValue}</Text>
          )}
        </div>
      );
    }

    return (
      <div>
        <Text strong>{log.realName || log.username}</Text>{' '}
        {actionTypeName}
        {log.remark && (
          <div style={{ marginTop: 8, color: '#595959' }}>
            {log.remark}
          </div>
        )}
      </div>
    );
  };

  const groupLogsByDate = () => {
    const groups = {};
    logs.forEach((log) => {
      const date = dayjs(log.createTime).format('YYYY-MM-DD');
      if (!groups[date]) {
        groups[date] = [];
      }
      groups[date].push(log);
    });
    return Object.entries(groups).sort((a, b) => b[0].localeCompare(a[0]));
  };

  const groupedLogs = groupLogsByDate();

  if (logs.length === 0 && !loading) {
    return (
      <Card>
        <Empty description="暂无动态" image={Empty.PRESENTED_IMAGE_SIMPLE} />
      </Card>
    );
  }

  return (
    <Card title="动态时间线" extra={
      <Button size="small" onClick={() => fetchLogs(1, true)} loading={loading}>
        刷新
      </Button>
    }>
      <Spin spinning={loading && logs.length === 0}>
        {groupedLogs.map(([date, dateLogs]) => (
          <div key={date} style={{ marginBottom: 24 }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 8,
                marginBottom: 16,
              }}
            >
              <div
                style={{
                  padding: '4px 12px',
                  background: '#f0f0f0',
                  borderRadius: 12,
                  fontSize: 12,
                  color: '#595959',
                }}
              >
                {date === dayjs().format('YYYY-MM-DD')
                  ? '今天'
                  : date === dayjs().subtract(1, 'day').format('YYYY-MM-DD')
                  ? '昨天'
                  : date}
              </div>
            </div>

            <Timeline
              mode="left"
              items={dateLogs.map((log) => {
                const color = actionColorMap[log.actionType] || '#1890ff';
                const icon = actionIconMap[log.actionType] || <ClockCircleOutlined />;

                return {
                  color,
                  dot: icon,
                  children: (
                    <div style={{ marginBottom: 16 }}>
                      <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                        <Avatar
                          size={32}
                          src={log.avatar}
                          icon={<UserOutlined />}
                        />
                        <div style={{ flex: 1 }}>
                          <div style={{ marginBottom: 4 }}>
                            {renderActionContent(log)}
                          </div>
                          <div style={{ fontSize: 12, color: '#8c8c8c' }}>
                            {dayjs(log.createTime).format('HH:mm:ss')}
                          </div>
                        </div>
                      </div>
                    </div>
                  ),
                };
              })}
            />
          </div>
        ))}

        {hasMore && (
          <div style={{ textAlign: 'center', marginTop: 16 }}>
            <Button onClick={handleLoadMore} loading={loading}>
              加载更多
            </Button>
          </div>
        )}
      </Spin>
    </Card>
  );
};

export default DynamicTimeline;
