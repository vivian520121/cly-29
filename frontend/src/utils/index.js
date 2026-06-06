import dayjs from 'dayjs';

export const formatDate = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date) return '-';
  return dayjs(date).format(format);
};

export const formatFileSize = (size) => {
  if (!size || size === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(size) / Math.log(k));
  return parseFloat((size / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

export const getStatusColor = (status) => {
  const colorMap = {
    TODO: '#91d5ff',
    IN_PROGRESS: '#ffd666',
    REVIEW: '#b37feb',
    DONE: '#95de64',
    CLOSED: '#8c8c8c',
  };
  return colorMap[status] || '#d9d9d9';
};

export const getPriorityColor = (priority) => {
  const colorMap = {
    LOW: '#52c41a',
    MEDIUM: '#faad14',
    HIGH: '#f5222d',
    URGENT: '#cf1322',
  };
  return colorMap[priority] || '#d9d9d9';
};

export const getTaskTypeColor = (type) => {
  const colorMap = {
    TASK: '#1890ff',
    BUG: '#f5222d',
    FEATURE: '#722ed1',
    IMPROVEMENT: '#13c2c2',
    DOC: '#fa8c16',
  };
  return colorMap[type] || '#d9d9d9';
};

export const generateUUID = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
};

export const debounce = (fn, delay) => {
  let timer = null;
  return function (...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
};
