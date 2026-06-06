import request from '../utils/request';

export const getDashboard = () => {
  return request({
    url: '/dashboard',
    method: 'get',
  });
};

export const getTodoTasks = () => {
  return request({
    url: '/dashboard/todoTasks',
    method: 'get',
  });
};

export const getProjectDynamics = () => {
  return request({
    url: '/dashboard/projectDynamics',
    method: 'get',
  });
};

export const getStatistics = () => {
  return request({
    url: '/dashboard/statistics',
    method: 'get',
  });
};
