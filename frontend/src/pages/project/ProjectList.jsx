import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Card,
  Row,
  Col,
  Input,
  Select,
  Button,
  Modal,
  Form,
  DatePicker,
  Avatar,
  Tag,
  Progress,
  Popconfirm,
  message,
  Empty,
  Spin,
  Space
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  TeamOutlined,
  CalendarOutlined,
  SearchOutlined,
  FilterOutlined
} from '@ant-design/icons'
import {
  getProjectList,
  createProject,
  updateProject,
  deleteProject
} from '@/services/project'
import dayjs from 'dayjs'
import styles from './ProjectList.module.scss'

const { Search } = Input
const { RangePicker } = DatePicker

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '未开始', value: 'NOT_STARTED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已暂停', value: 'PAUSED' },
  { label: '已取消', value: 'CANCELLED' }
]

const priorityOptions = [
  { label: '全部优先级', value: '' },
  { label: '高', value: 'HIGH' },
  { label: '中', value: 'MEDIUM' },
  { label: '低', value: 'LOW' }
]

const statusColors = {
  NOT_STARTED: 'default',
  IN_PROGRESS: 'processing',
  COMPLETED: 'success',
  PAUSED: 'warning',
  CANCELLED: 'error'
}

const statusText = {
  NOT_STARTED: '未开始',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  PAUSED: '已暂停',
  CANCELLED: '已取消'
}

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

const ProjectList = () => {
  const [loading, setLoading] = useState(false)
  const [projects, setProjects] = useState([])
  const [pagination, setPagination] = useState({ current: 1, pageSize: 12, total: 0 })
  const [filters, setFilters] = useState({
    keyword: '',
    status: '',
    priority: ''
  })
  const [modalVisible, setModalVisible] = useState(false)
  const [editingProject, setEditingProject] = useState(null)
  const [form] = Form.useForm()
  const navigate = useNavigate()

  useEffect(() => {
    loadProjects()
  }, [pagination.current, pagination.pageSize, filters])

  const loadProjects = async () => {
    setLoading(true)
    try {
      const data = await getProjectList({
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
        ...filters
      })
      setProjects(data?.records || [])
      setPagination((prev) => ({ ...prev, total: data?.total || 0 }))
    } catch (error) {
      console.error('Load projects error:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = (value) => {
    setFilters((prev) => ({ ...prev, keyword: value }))
    setPagination((prev) => ({ ...prev, current: 1 }))
  }

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }))
    setPagination((prev) => ({ ...prev, current: 1 }))
  }

  const handleCreate = () => {
    setEditingProject(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (project) => {
    setEditingProject(project)
    form.setFieldsValue({
      ...project,
      startDate: project.startDate ? dayjs(project.startDate) : null,
      endDate: project.endDate ? dayjs(project.endDate) : null
    })
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    try {
      await deleteProject(id)
      message.success('删除成功')
      loadProjects()
    } catch (error) {
      console.error('Delete project error:', error)
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const submitData = {
        ...values,
        startDate: values.startDate?.format('YYYY-MM-DD'),
        endDate: values.endDate?.format('YYYY-MM-DD')
      }

      if (editingProject) {
        await updateProject(editingProject.id, submitData)
        message.success('更新成功')
      } else {
        await createProject(submitData)
        message.success('创建成功')
      }
      setModalVisible(false)
      loadProjects()
    } catch (error) {
      console.error('Submit project error:', error)
    }
  }

  const handleCardClick = (project) => {
    navigate(`/project/${project.id}`)
  }

  return (
    <div className={styles.projectList}>
      <div className="page-header">
        <h1 className="page-title">项目管理</h1>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          新建项目
        </Button>
      </div>

      <Card className={styles.filterCard}>
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} sm={12} md={8} lg={6}>
            <Search
              placeholder="搜索项目名称"
              allowClear
              onSearch={handleSearch}
              prefix={<SearchOutlined />}
            />
          </Col>
          <Col xs={24} sm={12} md={8} lg={6}>
            <Select
              placeholder="项目状态"
              allowClear
              style={{ width: '100%' }}
              options={statusOptions}
              value={filters.status || undefined}
              onChange={(value) => handleFilterChange('status', value || '')}
              prefix={<FilterOutlined />}
            />
          </Col>
          <Col xs={24} sm={12} md={8} lg={6}>
            <Select
              placeholder="优先级"
              allowClear
              style={{ width: '100%' }}
              options={priorityOptions}
              value={filters.priority || undefined}
              onChange={(value) => handleFilterChange('priority', value || '')}
            />
          </Col>
        </Row>
      </Card>

      {loading ? (
        <div className={styles.loadingContainer}>
          <Spin size="large" />
        </div>
      ) : projects.length > 0 ? (
        <Row gutter={[16, 16]} className={styles.projectGrid}>
          {projects.map((project) => (
            <Col xs={24} sm={12} lg={8} xl={6} key={project.id}>
              <Card
                className={`${styles.projectCard} card-hover`}
                onClick={() => handleCardClick(project)}
                actions={[
                  <EditOutlined
                    key="edit"
                    onClick={(e) => {
                      e.stopPropagation()
                      handleEdit(project)
                    }}
                  />,
                  <Popconfirm
                    key="delete"
                    title="确定要删除这个项目吗?"
                    onConfirm={(e) => {
                      e?.stopPropagation()
                      handleDelete(project.id)
                    }}
                    onCancel={(e) => e?.stopPropagation()}
                  >
                    <DeleteOutlined
                      onClick={(e) => e.stopPropagation()}
                    />
                  </Popconfirm>
                ]}
              >
                <div className={styles.cardHeader}>
                  <div className={styles.projectName}>{project.name}</div>
                  <Tag color={priorityColors[project.priority]}>
                    {priorityText[project.priority]}
                  </Tag>
                </div>

                <p className={styles.projectDesc}>{project.description}</p>

                <div className={styles.projectMeta}>
                  <div className={styles.metaItem}>
                    <TeamOutlined />
                    <Avatar.Group maxCount={3}>
                      {project.members?.map((member) => (
                        <Avatar
                          key={member.id}
                          size="small"
                          src={member.avatar}
                        >
                          {member.username?.charAt(0)?.toUpperCase()}
                        </Avatar>
                      ))}
                    </Avatar.Group>
                  </div>
                  <div className={styles.metaItem}>
                    <CalendarOutlined />
                    <span>
                      {project.startDate
                        ? dayjs(project.startDate).format('MM-DD')
                        : '-'}
                      {' ~ '}
                      {project.endDate
                        ? dayjs(project.endDate).format('MM-DD')
                        : '-'}
                    </span>
                  </div>
                </div>

                <div className={styles.projectProgress}>
                  <div className={styles.progressHeader}>
                    <span>项目进度</span>
                    <Tag color={statusColors[project.status]}>
                      {statusText[project.status]}
                    </Tag>
                  </div>
                  <Progress
                    percent={project.progress || 0}
                    size="small"
                    strokeColor={
                      project.status === 'COMPLETED' ? '#52c41a' : '#1890ff'
                    }
                  />
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      ) : (
        <Empty description="暂无项目" className={styles.empty} />
      )}

      <Modal
        title={editingProject ? '编辑项目' : '新建项目'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="项目名称"
            rules={[{ required: true, message: '请输入项目名称' }]}
          >
            <Input placeholder="请输入项目名称" />
          </Form.Item>
          <Form.Item
            name="description"
            label="项目描述"
            rules={[{ required: true, message: '请输入项目描述' }]}
          >
            <Input.TextArea rows={4} placeholder="请输入项目描述" />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="status"
                label="项目状态"
                rules={[{ required: true, message: '请选择项目状态' }]}
              >
                <Select
                  options={statusOptions.filter((opt) => opt.value !== '')}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="priority"
                label="优先级"
                rules={[{ required: true, message: '请选择优先级' }]}
              >
                <Select
                  options={priorityOptions.filter((opt) => opt.value !== '')}
                />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item
            name={['startDate', 'endDate']}
            label="项目周期"
            rules={[{ required: true, message: '请选择项目周期' }]}
          >
            <RangePicker style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default ProjectList
