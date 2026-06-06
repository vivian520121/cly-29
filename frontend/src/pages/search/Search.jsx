import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { Input, Tabs, List, Card, Tag, Space, Typography, Empty, Avatar } from 'antd'
import { SearchOutlined, ProjectOutlined, CheckCircleOutlined, UserOutlined, ClockCircleOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { globalSearch, searchProjects, searchTasks } from '@/services/search'
import styles from './Search.module.scss'

const { Title, Text } = Typography
const { Search } = Input

const priorityColors = {
  HIGH: 'red',
  MEDIUM: 'orange',
  LOW: 'blue'
}

const priorityText = {
  HIGH: '高',
  MEDIUM: '中',
  LOW: '低'
}

const statusColors = {
  PENDING: 'default',
  IN_PROGRESS: 'processing',
  REVIEWING: 'warning',
  COMPLETED: 'success',
  CANCELLED: 'error'
}

const statusText = {
  PENDING: '待办',
  IN_PROGRESS: '进行中',
  REVIEWING: '评审中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

const projectStatusColors = {
  PLANNING: 'default',
  IN_PROGRESS: 'processing',
  COMPLETED: 'success',
  ON_HOLD: 'warning',
  CANCELLED: 'error'
}

const projectStatusText = {
  PLANNING: '规划中',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  ON_HOLD: '暂停',
  CANCELLED: '已取消'
}

const SearchPage = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '')
  const [activeTab, setActiveTab] = useState(searchParams.get('type') || 'all')
  const [loading, setLoading] = useState(false)
  const [results, setResults] = useState({
    all: [],
    projects: [],
    tasks: []
  })
  const [pagination, setPagination] = useState({
    all: { current: 1, pageSize: 10, total: 0 },
    projects: { current: 1, pageSize: 10, total: 0 },
    tasks: { current: 1, pageSize: 10, total: 0 }
  })

  const fetchSearchResults = async (tab = activeTab, page = 1, pageSize = 10) => {
    if (!keyword.trim()) {
      setResults({ all: [], projects: [], tasks: [] })
      return
    }

    setLoading(true)
    try {
      const params = {
        keyword: keyword.trim(),
        page,
        pageSize
      }

      let response
      if (tab === 'all') {
        response = await globalSearch(params)
      } else if (tab === 'projects') {
        response = await searchProjects(params)
      } else if (tab === 'tasks') {
        response = await searchTasks(params)
      }

      if (response?.data) {
        const data = response.data
        setResults(prev => ({
          ...prev,
          [tab]: data.records || data.list || []
        }))
        setPagination(prev => ({
          ...prev,
          [tab]: {
            current: page,
            pageSize,
            total: data.total || 0
          }
        }))
      }
    } catch (error) {
      console.error('搜索失败:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    const urlKeyword = searchParams.get('keyword')
    const urlType = searchParams.get('type')
    if (urlKeyword) {
      setKeyword(urlKeyword)
      if (urlType) {
        setActiveTab(urlType)
      }
      fetchSearchResults(urlType || 'all', 1, 10)
    }
  }, [searchParams])

  const handleSearch = (value) => {
    const trimmedValue = value.trim()
    if (!trimmedValue) return
    
    setKeyword(trimmedValue)
    setSearchParams({
      keyword: trimmedValue,
      type: activeTab
    })
    fetchSearchResults(activeTab, 1, 10)
  }

  const handleTabChange = (key) => {
    setActiveTab(key)
    setSearchParams({
      keyword,
      type: key
    })
    if (results[key].length === 0 || pagination[key].current === 1) {
      fetchSearchResults(key, 1, pagination[key].pageSize)
    }
  }

  const handlePageChange = (page, pageSize) => {
    fetchSearchResults(activeTab, page, pageSize)
  }

  const renderProjectItem = (project) => (
    <List.Item
      key={`project-${project.id}`}
      className={styles.resultItem}
      onClick={() => navigate(`/project/${project.id}`)}
    >
      <List.Item.Meta
        avatar={<Avatar icon={<ProjectOutlined />} style={{ backgroundColor: '#1677ff' }} />}
        title={
          <Space className={styles.itemTitle}>
            <span className={styles.titleText}>{project.name}</span>
            <Tag color={projectStatusColors[project.status]}>
              {projectStatusText[project.status]}
            </Tag>
          </Space>
        }
        description={
          <Space direction="vertical" size="small" className={styles.itemDesc}>
            <Text type="secondary" className={styles.descText}>
              {project.description || '暂无描述'}
            </Text>
            <Space size="large">
              <Text type="secondary">
                <ClockCircleOutlined className={styles.icon} />
                创建于 {dayjs(project.createdAt).format('YYYY-MM-DD')}
              </Text>
              {project.deadline && (
                <Text type="secondary">
                  <CheckCircleOutlined className={styles.icon} />
                  截止 {dayjs(project.deadline).format('YYYY-MM-DD')}
                </Text>
              )}
            </Space>
          </Space>
        }
      />
      <Tag color="blue" className={styles.typeTag}>项目</Tag>
    </List.Item>
  )

  const renderTaskItem = (task) => (
    <List.Item
      key={`task-${task.id}`}
      className={styles.resultItem}
      onClick={() => navigate(`/task/${task.id}`)}
    >
      <List.Item.Meta
        avatar={<Avatar icon={<CheckCircleOutlined />} style={{ backgroundColor: '#52c41a' }} />}
        title={
          <Space className={styles.itemTitle}>
            <span className={styles.titleText}>{task.title}</span>
            <Tag color={priorityColors[task.priority]}>
              {priorityText[task.priority]}优先级
            </Tag>
            <Tag color={statusColors[task.status]}>
              {statusText[task.status]}
            </Tag>
          </Space>
        }
        description={
          <Space direction="vertical" size="small" className={styles.itemDesc}>
            <Text type="secondary" className={styles.descText}>
              {task.description || '暂无描述'}
            </Text>
            <Space size="large">
              <Text type="secondary">
                <UserOutlined className={styles.icon} />
                {task.assignee?.realName || '未分配'}
              </Text>
              {task.deadline && (
                <Text type={dayjs(task.deadline).isBefore(dayjs()) ? 'danger' : 'secondary'}>
                  <ClockCircleOutlined className={styles.icon} />
                  截止 {dayjs(task.deadline).format('YYYY-MM-DD')}
                </Text>
              )}
              {task.project?.name && (
                <Text type="secondary">
                  <ProjectOutlined className={styles.icon} />
                  {task.project.name}
                </Text>
              )}
            </Space>
          </Space>
        }
      />
      <Tag color="green" className={styles.typeTag}>任务</Tag>
    </List.Item>
  )

  const tabItems = [
    {
      key: 'all',
      label: `全部 (${pagination.all.total})`,
      children: (
        <List
          loading={loading && activeTab === 'all'}
          dataSource={results.all}
          renderItem={(item) => item.type === 'project' ? renderProjectItem(item) : renderTaskItem(item)}
          locale={{ emptyText: <Empty description="暂无搜索结果" /> }}
          pagination={{
            ...pagination.all,
            onChange: handlePageChange,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条结果`
          }}
        />
      )
    },
    {
      key: 'projects',
      label: `项目 (${pagination.projects.total})`,
      children: (
        <List
          loading={loading && activeTab === 'projects'}
          dataSource={results.projects}
          renderItem={renderProjectItem}
          locale={{ emptyText: <Empty description="暂无相关项目" /> }}
          pagination={{
            ...pagination.projects,
            onChange: handlePageChange,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 个项目`
          }}
        />
      )
    },
    {
      key: 'tasks',
      label: `任务 (${pagination.tasks.total})`,
      children: (
        <List
          loading={loading && activeTab === 'tasks'}
          dataSource={results.tasks}
          renderItem={renderTaskItem}
          locale={{ emptyText: <Empty description="暂无相关任务" /> }}
          pagination={{
            ...pagination.tasks,
            onChange: handlePageChange,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 个任务`
          }}
        />
      )
    }
  ]

  return (
    <div className={styles.searchPage}>
      <Card className={styles.searchHeader}>
        <div className={styles.searchContent}>
          <Title level={3} className={styles.pageTitle}>
            <SearchOutlined className={styles.titleIcon} />
            全局搜索
          </Title>
          <div className={styles.searchBox}>
            <Search
              placeholder="请输入关键词搜索项目、任务..."
              allowClear
              enterButton
              size="large"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onSearch={handleSearch}
              className={styles.searchInput}
            />
          </div>
          {keyword && (
            <Text type="secondary" className={styles.searchHint}>
              搜索关键词: <Text strong>{keyword}</Text>
            </Text>
          )}
        </div>
      </Card>

      <Card className={styles.searchResults}>
        {keyword ? (
          <Tabs
            activeKey={activeTab}
            onChange={handleTabChange}
            items={tabItems}
            className={styles.searchTabs}
          />
        ) : (
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description="请输入关键词开始搜索"
            className={styles.emptyState}
          />
        )}
      </Card>
    </div>
  )
}

export default SearchPage
