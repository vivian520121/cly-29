import React from 'react';
import { Layout } from 'antd';
import { Outlet } from 'react-router-dom';
import styles from './AuthLayout.module.scss';

const { Content } = Layout;

const AuthLayout = () => {
  return (
    <Layout className={styles.authLayout}>
      <Content className={styles.authContent}>
        <div className={styles.authCard}>
          <Outlet />
        </div>
      </Content>
    </Layout>
  );
};

export default AuthLayout;
