import React from 'react'
import { useNavigate } from 'react-router-dom'
import { Result, Button, Typography, Space } from 'antd'
import { HomeOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import styles from './NotFound.module.scss'

const { Text, Title } = Typography

const NotFound = () => {
  const navigate = useNavigate()

  const handleGoHome = () => {
    navigate('/dashboard')
  }

  const handleGoBack = () => {
    navigate(-1)
  }

  return (
    <div className={styles.notFoundPage}>
      <div className={styles.content}>
        <div className={styles.errorCode}>
          <span className={styles.digit}>4</span>
          <span className={styles.zero}>
            <div className={styles.eye}></div>
          </span>
          <span className={styles.digit}>4</span>
        </div>
        
        <Result
          status="404"
          title={
            <div className={styles.title}>
              <Title level={2} className={styles.titleText}>
                页面未找到
              </Title>
              <Text type="secondary" className={styles.subtitle}>
                抱歉，您访问的页面不存在或已被移除3242433
              </Text>
            </div>
          }
          description={
            <div className={styles.description}>
              <Space direction="vertical" size="middle" className={styles.tips}>
                <Text type="secondary">
                  可能的原因：
                </Text>
                <ul className={styles.reasons}>
                  <li>• 您输入的网址有误</li>
                  <li>• 页面已被删除或移动</li>
                  <li>• 您没有访问该页面的权限</li>
                </ul>
              </Space>
              <div className={styles.illustration}>
                  <svg viewBox="0 0 200 100" className={styles.searchSvg}>
                    <path
                      d="M20,50 Q50,20 80,50 T140,50 T200,50"
                      fill="none"
                      stroke="#d9d9d9"
                      strokeWidth="2"
                      strokeDasharray="5,5"
                      className={styles.dottedLine}
                    />
                    <circle cx="170" cy="50" r="8" fill="#1677ff" className={styles.searchCircle}>
                      <animate attributeName="cx" values="170" to="30" dur="3s" repeatCount="indefinite" />
                    </circle>
                    <path d="M176,56 L184,64" stroke="#1677ff" strokeWidth="2" className={styles.searchHandle}>
                      <animate attributeName="d" values="M176,56 L184,64" dur="3s" repeatCount="indefinite" />
                    </path>
                  </svg>
                </div>
            </div>
          }
          extra={
            <Space size="middle" className={styles.actions}>
              <Button
                type="primary"
                icon={<ArrowLeftOutlined />}
                onClick={handleGoBack}
                size="large"
                className={styles.button}
              >
                返回上一页
              </Button>
              <Button
                icon={<HomeOutlined />}
                onClick={handleGoHome}
                size="large"
                className={styles.button}
              >
                返回首页
              </Button>
            </Space>
          }
          className={styles.result}
        />
      </div>
    </div>
  )
}

export default NotFound
