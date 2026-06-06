import React, { useState } from 'react';
import { Layout, Menu, Avatar, Dropdown, Input, Badge } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  SearchOutlined,
  BellOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { MENU_ITEMS } from '../utils/constants';
import { useAuthStore } from '../store/authStore';
import styles from './MainLayout.module.scss';

const { Header, Sider, Content } = Layout;

const MainLayout = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { userInfo, logout } = useAuthStore();

  const handleMenuClick = ({ key }) => {
    navigate(key);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人中心',
      onClick: () => navigate('/profile'),
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '账户设置',
      onClick: () => navigate('/settings'),
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ];

  const renderMenuItems = (items) => {
    return items.map((item) => {
      if (item.children) {
        return {
          key: item.key,
          icon: item.icon ? React.createElement(item.icon) : null,
          label: item.label,
          children: renderMenuItems(item.children),
        };
      }
      return {
        key: item.key,
        icon: item.icon ? React.createElement(item.icon) : null,
        label: item.label,
      };
    });
  };

  return (
    <Layout className={styles.mainLayout}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        className={styles.sider}
      >
        <div className={styles.logo}>
          {collapsed ? 'PC' : '项目协作平台'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={renderMenuItems(MENU_ITEMS)}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header className={styles.header}>
          <div className={styles.headerLeft}>
            {React.createElement(
              collapsed ? MenuUnfoldOutlined : MenuFoldOutlined,
              {
                className: styles.trigger,
                onClick: () => setCollapsed(!collapsed),
              }
            )}
          </div>
          <div className={styles.headerCenter}>
            <Input
              className={styles.searchInput}
              placeholder="全局搜索..."
              prefix={<SearchOutlined />}
              allowClear
            />
          </div>
          <div className={styles.headerRight}>
            <Badge count={5} size="small">
              <BellOutlined className={styles.icon} />
            </Badge>
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <div className={styles.userInfo}>
                <Avatar
                  size="small"
                  src={userInfo?.avatar}
                  icon={<UserOutlined />}
                />
                <span className={styles.userName}>
                  {collapsed ? '' : userInfo?.realName || userInfo?.username}
                </span>
              </div>
            </Dropdown>
          </div>
        </Header>
        <Content className={styles.content}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
