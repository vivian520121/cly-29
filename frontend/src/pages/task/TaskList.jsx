import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import {
  Card,
  Table,
  Button,
  Input,
  Select,
  DatePicker,
  Form,
  Avatar,
  Tag,
  Modal,
  Popconfirm,
  message,
  Spin,
  Empty,
  Row,
  Col,
  Space
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  FilterOutlined,
  ReloadOutlined
} from '@ant-design/icons'
import {
  getTaskList,
  createTask,
  updateTask,
  deleteTask
} from '@/services/task'
import { getProjectList } from '@/services/project'
import dayjs from 'dayjs'
import styles from './TaskList.module.scss'

const { RangePicker } = DatePicker

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '待办', value: 'TODO' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '评审中', value: 'REVIEW' },
  { label: '已完成', value: 'DONE' },
  { label: '已取消', value: 'CANCELLED' }
]

const priorityOptions = [
  { label: '全部优先级', value: '' },
  { label: '高', value: 'HIGH' },
  { label: '中', value: 'MEDIUM' },
  { label: '低', value: 'LOW' }
]

const typeOptions = [
  { label: '全部类型', value: '' },
  { label: '需求', value: 'FEATURE' },
  { label: 'Bug', value: 'BUG' },
  { label: '优化', value: 'OPTIMIZATION' },
  { label: '任务', value: 'TASK' }
]

const statusColors = {
  TODO: 'default',
  IN_PROGRESS: 'processing',
  REVIEW: 'warning',
  DONE: 'success',
  CANCELLED: 'error'
}

const statusText = {
  TODO: '待办',
  IN_PROGRESS: '进行中',
  REVIEW: '评审中',
  DONE: '已完成',
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

const typeColors = {
  FEATURE: 'blue',
  BUG: 'red',
  OPTIMIZATION: 'cyan',
  TASK: 'default'
}

const typeText = {
  FEATURE: '需求',
  BUG: 'Bug',
  OPTIMIZATION: '优化',
  TASK: '任务'
}

const TaskList = () => {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [tasks, setTasks] = useState([])
  const [projects, setProjects] = useState([])
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  })
  const [filters, setFilters] = useState({
    keyword: '',
    status: '',
    priority: '',
    type: '',
    assigneeId: '',
    projectId: searchParams.get('projectId') || '',
    startTime: '',
    endTime: ''
  })
  const [filterVisible, setFilterVisible] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingTask, setEditingTask] = useState(null)
  const [form] = Form.useForm()

  useEffect(() => {
    loadProjects()
  }, [])

  useEffect(() => {
    loadTasks()
  }, [pagination.current, pagination.pageSize, filters])

  const loadProjects = async () => {
    try {
      const data = await getProjectList({ pageNum: 1, pageSize: 100 })
      setProjects(data?.records || [])
    } catch (error) {
      console.error('Load projects error:', error)
    }
  }

  const loadTasks = async () => {
    setLoading(true)
    try {
      const params = {
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
        ...filters
      }
      if (filters.startTime) {
        params.startTime = filters.startTime
      }
      if (filters.endTime) {
        params.endTime = filters.endTime
      }
      const data = await getTaskList(params)
      setTasks(data?.records || [])
      setPagination((prev) => ({ ...prev, total: data?.total || 0 }))
    } catch (error) {
      console.error('Load tasks error:', error)
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

  const handleDateRangeChange = (dates) => {
    if (dates && dates[0] && dates[1]) {
      setFilters((prev) => ({
        ...prev,
        startTime: dates[0].format('YYYY-MM-DD'),
        endTime: dates[1].format('YYYY-MM-DD')
      }))
    } else {
      setFilters((prev) => ({ ...prev, startTime: '', endTime: '' }))
    }
    setPagination((prev) => ({ ...prev, current: 1 }))
  }

  const handleReset = () => {
    setFilters({
      keyword: '',
      status: '',
      priority: '',
      type: '',
      assigneeId: '',
      projectId: '',
      startTime: '',
      endTime: ''
    })
    setPagination((prev) => ({ ...prev, current: 1 }))
  }

  const handleCreate = () => {
    setEditingTask(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (task) => {
    setEditingTask(task)
    form.setFieldsValue({
      ...task,
      dueDate: task.dueDate ? dayjs(task.dueDate) : null
    })
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    try {
      await deleteTask(id)
      message.success('删除成功')
      loadTasks()
    } catch (error) {
      console.error('Delete task error:', error)
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const submitData = {
        ...values,
        dueDate: values.dueDate?.format('YYYY-MM-DD')
      }

      if (editingTask) {
        await updateTask(editingTask.id, submitData)
        message.success('更新成功')
      } else {
        await createTask(submitData)
        message.success('创建成功')
      }
      setModalVisible(false)
      loadTasks()
    } catch (error) {
      console.error('Submit task error:', error)
    }
  }

  const columns = [
    {
      title: '任务名称',
      dataIndex: 'name',
      key: 'name',
      width: 250,
      render: (text, record) => (
        <a onClick={() => navigate(`/task/${record.id}`)} className={styles.taskName}>
          {text}
        </a>
      )
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type) => (
        <Tag color={typeColors[type]}>{typeText[type]}</Tag>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => (
        <Tag color={statusColors[status]}>{statusText[status]}</Tag>
      )
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      key: 'priority',
      width: 100,
      render: (priority) => (
        <Tag color={priorityColors[priority]}>{priorityText[priority]}</Tag>
      )
    },
    {
      title: '负责人',
      dataIndex: 'assignee',
      key: 'assignee',
      width: 120,
      render: (assignee) => (
        <Space>
          <Avatar size="small" src={assignee?.avatar}>
            {assignee?.username?.charAt(0)?.toUpperCase()}
          </Avatar>
          <span>{assignee?.username || '未分配'}</span>
        </Space>
      )
    },
    {
      title: '所属项目',
      dataIndex: 'project',
      key: 'project',
      width: 150,
      render: (project) => (
        <span className={styles.projectName}>
          {project?.name || '未关联'}
        </span>
      )
    },
    {
      title: '截止日期',
      dataIndex: 'dueDate',
      key: 'dueDate',
      width: 120,
      sorter: true,
      render: (date) =>
        date ? dayjs(date).format('YYYY-MM-DD') : '-',
      sortDirections: ['descend', 'ascend']
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 150,
      sorter: true,
      render: (time) => dayjs(time).format('YYYY-MM-DD HH:mm'),
      sortDirections: ['descend', 'ascend']
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button
            type="text"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个任务吗?"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="text" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div className={styles.taskList}>
      <div className="page-header">
        <h1 className="page-title">任务列表</h1>
        <div className={styles.headerActions}>
          <Button icon={<ReloadOutlined />} onClick={loadTasks}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            新建任务
          </Button>
        </div>
      </div>

      <Card className={styles.filterCard}>
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} sm={12} md={8} lg={6}>
            <Input.Search
              placeholder="搜索任务名称"
              allowClear
              onSearch={handleSearch}
              prefix={<SearchOutlined />}
              value={filters.keyword}
              onChange={(e) => handleFilterChange('keyword', e.target.value)}
            />
          </Col>
          <Col xs={24} sm={12} md={8} lg={5}>
            <Select
              placeholder="任务状态"
              allowClear
              style={{ width: '100%' }}
              options={statusOptions}
              value={filters.status || undefined}
              onChange={(value) => handleFilterChange('status', value || '')}
            />
          </Col>
          <Col xs={24} sm={12} md={8} lg={5}>
            <Select
              placeholder="优先级"
              allowClear
              style={{ width: '100%' }}
              options={priorityOptions}
              value={filters.priority || undefined}
              onChange={(value) => handleFilterChange('priority', value || '')}
            />
          </Col>
          <Col xs={24} sm={12} md={8} lg={5}>
            <Select
              placeholder="任务类型"
              allowClear
              style={{ width: '100%' }}
              options={typeOptions}
              value={filters.type || undefined}
              onChange={(value) => handleFilterChange('type', value || '')}
            />
          </Col>
          <Col xs={24} sm={12} md={8} lg={5}>
            <Select
              placeholder="所属项目"
              allowClear
              style={{ width: '100%' }}
              showSearch
              optionFilterProp="label"
              options={projects.map((p) => ({
                label: p.name,
                value: p.id
              }))}
              value={filters.projectId || undefined}
              onChange={(value) => handleFilterChange('projectId', value || '')}
            />
          </Col>
          <Col xs={24} sm={12} md={16} lg={10}>
            <RangePicker
              style={{ width: '100%' }}
              onChange={handleDateRangeChange}
              placeholder={['开始日期', '结束日期']}
            />
          </Col>
          <Col xs={24} sm={12} md={8} lg={4}>
            <Space>
              <Button icon={<FilterOutlined />} onClick={() => setFilterVisible(!filterVisible)}>
                高级筛选
              </Button>
              <Button onClick={handleReset}>重置</Button>
            </Space>
          </Col>
        </Row>
      </Card>

      <Card className={styles.tableCard}>
        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={tasks}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`
          }}
          scroll={{ x: 1200 }}
          onChange={(p) => setPagination((prev) => ({ ...prev, current: p.current, pageSize: p.pageSize }))}
        />
      </Card>

      <Modal
        title={editingTask ? '编辑任务' : '新建任务'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="任务名称"
            rules={[{ required: true, message: '请输入任务名称' }]}
          >
            <Input placeholder="请输入任务名称" />
          </Form.Item>
          <Form.Item
            name="description"
            label="任务描述"
            rules={[{ required: true, message: '请输入任务描述' }]}
          >
            <Input.TextArea rows={4} placeholder="请输入任务描述" />
          </Form.Item>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="type"
                label="任务类型"
                rules={[{ required: true, message: '请选择任务类型' }]}
              >
                <Select
                  options={typeOptions.filter((opt) => opt.value !== '')}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="status"
                label="任务状态"
                rules={[{ required: true, message: '请选择任务状态' }]}
              >
                <Select
                  options={statusOptions.filter((opt) => opt.value !== '')}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
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
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="projectId"
                label="所属项目"
                rules={[{ required: true, message: '请选择项目' }]}
              >
                <Select
                  showSearch
                  optionFilterProp="label"
                  options={projects.map((p) => ({
                    label: p.name,
                    value: p.id
                  }))}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="dueDate"
                label="截止日期"
                rules={[{ required: true, message: '请选择截止日期' }]}
              >
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  )
}

export default TaskList
