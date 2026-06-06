import { useMemo } from 'react';
import { useAuthStore } from '@/store/authStore';

const useAuth = () => {
  const { token, userInfo, login, logout, setUserInfo } = useAuthStore();

  const isAuthenticated = useMemo(() => {
    return !!token && !!userInfo;
  }, [token, userInfo]);

  const hasPermission = (permission) => {
    if (!userInfo || !userInfo.permissions) return false;
    if (userInfo.userType === 1) return true;
    return userInfo.permissions.includes(permission);
  };

  const hasRole = (role) => {
    if (!userInfo || !userInfo.roles) return false;
    if (userInfo.userType === 1) return true;
    return userInfo.roles.includes(role);
  };

  const isAdmin = useMemo(() => {
    return userInfo?.userType === 1;
  }, [userInfo]);

  return {
    token,
    userInfo,
    isAuthenticated,
    isAdmin,
    login,
    logout,
    setUserInfo,
    hasPermission,
    hasRole,
  };
};

export default useAuth;
