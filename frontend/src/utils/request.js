import axios from 'axios';
import { message } from 'antd';
import { useAuthStore } from '@/store/authStore';

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

instance.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

instance.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code === 200) {
      return res.data;
    }
    message.error(res.message || '请求失败');
    return Promise.reject(new Error(res.message || '请求失败'));
  },
  (error) => {
    if (error.response) {
      const { status } = error.response;
      if (status === 401) {
        useAuthStore.getState().logout();
        window.location.href = '/login';
        message.error('登录已过期，请重新登录');
      } else if (status === 403) {
        message.error('没有权限访问');
      } else if (status === 404) {
        message.error('请求的资源不存在');
      } else if (status >= 500) {
        message.error('服务器错误');
      } else {
        message.error(error.response.data?.message || '请求失败');
      }
    } else if (error.request) {
      message.error('网络错误，请检查网络连接');
    } else {
      message.error(error.message || '请求失败');
    }
    return Promise.reject(error);
  }
);

const request = (config) => {
  return instance(config);
};

export default request;
