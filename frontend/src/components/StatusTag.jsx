import { Tag } from 'antd';

const statusMap = {
  1: { text: '待办', color: '#f5222d' },
  2: { text: '进行中', color: '#1890ff' },
  3: { text: '审核中', color: '#fa8c16' },
  4: { text: '已完成', color: '#52c41a' },
  5: { text: '已取消', color: '#8c8c8c' },
};

const projectStatusMap = {
  1: { text: '未开始', color: '#8c8c8c' },
  2: { text: '进行中', color: '#1890ff' },
  3: { text: '已暂停', color: '#fa8c16' },
  4: { text: '已完成', color: '#52c41a' },
  5: { text: '已取消', color: '#f5222d' },
};

const milestoneStatusMap = {
  1: { text: '未开始', color: '#8c8c8c' },
  2: { text: '进行中', color: '#1890ff' },
  3: { text: '已完成', color: '#52c41a' },
  4: { text: '已延期', color: '#f5222d' },
};

const StatusTag = ({ status, type = 'task' }) => {
  let statusInfo;

  switch (type) {
    case 'project':
      statusInfo = projectStatusMap[status] || { text: '未知', color: '#8c8c8c' };
      break;
    case 'milestone':
      statusInfo = milestoneStatusMap[status] || { text: '未知', color: '#8c8c8c' };
      break;
    default:
      statusInfo = statusMap[status] || { text: '未知', color: '#8c8c8c' };
  }

  return (
    <Tag color={statusInfo.color} style={{ margin: 0 }}>
      {statusInfo.text}
    </Tag>
  );
};

export default StatusTag;
