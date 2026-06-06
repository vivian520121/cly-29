import { Tag } from 'antd';

const priorityMap = {
  1: { text: '紧急', color: '#f5222d' },
  2: { text: '高', color: '#fa8c16' },
  3: { text: '中', color: '#1890ff' },
  4: { text: '低', color: '#52c41a' },
};

const PriorityTag = ({ priority }) => {
  const priorityInfo = priorityMap[priority] || { text: '未知', color: '#8c8c8c' };

  return (
    <Tag color={priorityInfo.color} style={{ margin: 0 }}>
      {priorityInfo.text}
    </Tag>
  );
};

export default PriorityTag;
