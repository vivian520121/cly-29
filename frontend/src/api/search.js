import request from '../utils/request';

export const globalSearch = (keyword) => {
  return request({
    url: '/search/global',
    method: 'get',
    params: { keyword },
  });
};

export const searchProject = (keyword) => {
  return request({
    url: '/search/project',
    method: 'get',
    params: { keyword },
  });
};

export const searchTask = (keyword) => {
  return request({
    url: '/search/task',
    method: 'get',
    params: { keyword },
  });
};
