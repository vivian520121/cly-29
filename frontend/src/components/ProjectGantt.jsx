import { Spin, Card, Button, Space, Modal, Descriptions, Tag, Progress, Empty } from 'antd';
import { useState, useEffect } from 'react';
import {
  EyeOutlined,
  CalendarOutlined,
  UserOutlined,
  FlagOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import request from '@/utils/request';
import GanttChart from './GanttChart';
import StatusTag from './StatusTag';
import PriorityTag from './PriorityTag';
import useRequest from '@/hooks/useRequest';

const ProjectGantt = ({ projectId }) => {
  const [detailModal, setDetailModal] = useState({ open: false, data: null, type: null });

  const { data: ganttData, loading, refresh } = useRequest(
    projectId ? `/project/${projectId}/gantt` : null,
    {
      manual: !projectId,
    }
  );

  const handleTaskClick = (task) => {
    setDetailModal({
      open: true,
      data: task,
      type: 'task',
    });
  };

  const renderDetailContent = () => {
    const { data, type } = detailModal;
    if (!data) return null;

    if (type === 'task') {
      return (
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="任务名称" span={2}>
            {data.taskName}
          </Descriptions.Item>
          {data.taskNo && (
            <Descriptions.Item label="任务编号">
              #{data.taskNo}
            </Descriptions.Item>
          )}
          <Descriptions.Item label="任务类型">
            {['', '需求', '开发', '测试', 'Bug', '优化'][data.taskType] || '未知'}
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            <StatusTag status={data.status} />
          </Descriptions.Item>
          <Descriptions.Item label="优先级">
            <PriorityTag priority={data.priority} />
          </Descriptions.Item>
          <Descriptions.Item label="进度">
            <Progress percent={data.progress || 0} size="small" />
          </Descriptions.Item>
          <Descriptions.Item label="开始日期">
            <Space>
              <CalendarOutlined />
              {data.startDate ? dayjs(data.startDate).format('YYYY-MM-DD') : '-'}
            </Space>
          </Descriptions.Item>
          <Descriptions.Item label="结束日期">
            <Space>
              <CalendarOutlined />
              {data.endDate ? dayjs(data.endDate).format('YYYY-MM-DD') : '-'}
            </Space>
          </Descriptions.Item>
          <Descriptions.Item label="预估工时">
            {data.estimateHours ? `${data.estimateHours} 小时` : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="实际工时">
            {data.actualHours ? `${data.actualHours} 小时` : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="负责人" span={2}>
            <Space>
              <UserOutlined />
              {data.assigneeName || '未分配'}
            </Space>
          </Descriptions.Item>
          {data.description && (
            <Descriptions.Item label="描述" span={2}>
              <div dangerouslySetInnerHTML={{ __html: data.description }} />
            </Descriptions.Item>
          )}
        </Descriptions>
      );
    }

    if (type === 'phase') {
      return (
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="阶段名称" span={2}>
            <Tag color="#1890ff">阶段</Tag> {data.phaseName}
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            <StatusTag status={data.status} type="project" />
          </Descriptions.Item>
          <Descriptions.Item label="进度">
            <Progress percent={data.progress || 0} size="small" />
          </Descriptions.Item>
          <Descriptions.Item label="开始日期">
            {data.startDate ? dayjs(data.startDate).format('YYYY-MM-DD') : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="结束日期">
            {data.endDate ? dayjs(data.endDate).format('YYYY-MM-DD') : '-'}
          </Descriptions.Item>
          {data.description && (
            <Descriptions.Item label="描述" span={2}>
              {data.description}
            </Descriptions.Item>
          )}
        </Descriptions>
      );
    }

    if (type === 'milestone') {
      return (
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="里程碑名称" span={2}>
            <Tag color="#722ed1">里程碑</Tag> {data.milestoneName}
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            <StatusTag status={data.status} type="milestone" />
          </Descriptions.Item>
          <Descriptions.Item label="排序">
            {data.sortOrder}
          </Descriptions.Item>
          <Descriptions.Item label="计划日期">
            <FlagOutlined /> {data.planDate ? dayjs(data.planDate).format('YYYY-MM-DD') : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="实际日期">
            <FlagOutlined /> {data.actualDate ? dayjs(data.actualDate).format('YYYY-MM-DD') : '-'}
          </Descriptions.Item>
          {data.description && (
            <Descriptions.Item label="描述" span={2}>
              {data.description}
            </Descriptions.Item>
          )}
        </Descriptions>
      );
    }

    return null;
  };

  const getModalTitle = () => {
    const { type, data } = detailModal;
    switch (type) {
      case 'task':
        return `任务详情 - ${data?.taskName}`;
      case 'phase':
        return `阶段详情 - ${data?.phaseName}`;
      case 'milestone':
        return `里程碑详情 - ${data?.milestoneName}`;
      default:
        return '详情';
    }
  };

  if (!projectId) {
    return <Empty description="请选择项目" />;
  }

  return (
    <div>
      <Card
        extra={
          <Button icon={<TeamOutlined />} onClick={refresh} loading={loading}>
            刷新
          </Button>
        }
      >
        <Spin spinning={loading}>
          {ganttData ? (
            <GanttChart
              phases={ganttData.phases || []}
              milestones={ganttData.milestones || []}
              tasks={ganttData.tasks || []}
              onTaskClick={handleTaskClick}
            />
          ) : (
            <Empty description="暂无甘特图数据" />
          )}
        </Spin>
      </Card>

      <Modal
        title={getModalTitle()}
        open={detailModal.open}
        onCancel={() => setDetailModal({ ...detailModal, open: false })}
        footer={[
          <Button key="close" onClick={() => setDetailModal({ ...detailModal, open: false })}>
            关闭
          </Button>,
        ]}
        width={700}
        destroyOnHidden
      >
        {renderDetailContent()}
      </Modal>
    </div>
  );
};

export default ProjectGantt;
