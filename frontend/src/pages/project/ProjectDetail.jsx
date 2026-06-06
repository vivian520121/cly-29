import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Card,
  Tabs,
  Row,
  Col,
  Statistic,
  Progress,
  Tag,
  Avatar,
  Button,
  Table,
  List,
  Form,
  Input,
  Select,
  DatePicker,
  Modal,
  message,
  Popconfirm,
  Empty,
  Spin,
  Space
} from 'antd'
import {
  ArrowLeftOutlined,
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
  UserOutlined,
  CalendarOutlined,
  TeamOutlined
} from '@ant-design/icons'
import ReactECharts from 'echarts-for-react'
import TaskCard from '@/components/TaskCard'
import {
  getProjectDetail,
  getProjectOverview,
  getProjectMembers,
  getProjectMilestones,
  addProjectMember,
  updateMemberRole,
  removeProjectMember,
  createMilestone,
  updateMilestone,
  deleteMilestone,
  updateProject
} from '@/services/project'
import { getTaskList } from '@/services/task'
import dayjs from 'dayjs'
import styles from './ProjectDetail.module.scss'

const { TabPane } = Tabs
const { RangePicker } = DatePicker

const roleOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '开发者', value: 'DEVELOPER' },
  { label: '观察者', value: 'VIEWER' }
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

const milestoneStatusColors = {
  NOT_STARTED: 'default',
  IN_PROGRESS: 'processing',
  COMPLETED: 'success',
  DELAYED: 'error'
}

const milestoneStatusText = {
  NOT_STARTED: '未开始',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  DELAYED: '已延期'
}

const ProjectDetail = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [project, setProject] = useState(null)
  const [overview, setOverview] = useState(null)
  const [members, setMembers] = useState([])
  const [milestones, setMilestones] = useState([])
  const [tasks, setTasks] = useState([])
  const [activeTab, setActiveTab] = useState('overview')
  const [memberModalVisible, setMemberModalVisible] = useState(false)
  const [milestoneModalVisible, setMilestoneModalVisible] = useState(false)
  const [editingMilestone, setEditingMilestone] = useState(null)
  const [memberForm] = Form.useForm()
  const [milestoneForm] = Form.useForm()
  const [settingsForm] = Form.useForm()
  const [settingsModalVisible, setSettingsModalVisible] = useState(false)

  useEffect(() => {
    if (id) {
      loadData()
    }
  }, [id])

  const loadData = async () => {
    setLoading(true)
    try {
      const [projectData, overviewData, membersData, milestonesData, tasksData] = await Promise.all([
        getProjectDetail(id),
        getProjectOverview(id),
        getProjectMembers(id),
        getProjectMilestones(id),
        getTaskList({ projectId: id, pageNum: 1, pageSize: 10 })
      ])
      setProject(projectData)
      setOverview(overviewData)
      setMembers(membersData || [])
      setMilestones(milestonesData || [])
      setTasks(tasksData?.records || [])
    } catch (error) {
      console.error('Load project detail error:', error)
    } finally {
      setLoading(false)
    }
  }

  const getTaskChartOption = () => {
    if (!overview) return {}
    return {
      tooltip: {
        trigger: 'item'
      },
      series: [
        {
          name: '任务状态',
          type: 'pie',
          radius: ['40%', '70%'],
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          data: [
            { value: overview?.todoCount || 0, name: '待办', itemStyle: { color: '#bfbfbf' } },
            { value: overview?.inProgressCount || 0, name: '进行中', itemStyle: { color: '#1890ff' } },
            { value: overview?.reviewCount || 0, name: '评审中', itemStyle: { color: '#faad14' } },
            { value: overview?.doneCount || 0, name: '已完成', itemStyle: { color: '#52c41a' } }
          ]
        }
      ]
    }
  }

  const handleAddMember = async () => {
    try {
      const values = await memberForm.validateFields()
      await addProjectMember(id, values)
      message.success('添加成功')
      setMemberModalVisible(false)
      memberForm.resetFields()
      loadData()
    } catch (error) {
      console.error('Add member error:', error)
    }
  }

  const handleRoleChange = async (memberId, role) => {
    try {
      await updateMemberRole(id, memberId, { role })
      message.success('角色更新成功')
      loadData()
    } catch (error) {
      console.error('Update role error:', error)
    }
  }

  const handleRemoveMember = async (memberId) => {
    try {
      await removeProjectMember(id, memberId)
      message.success('移除成功')
      loadData()
    } catch (error) {
      console.error('Remove member error:', error)
    }
  }

  const handleMilestoneSubmit = async () => {
    try {
      const values = await milestoneForm.validateFields()
      const submitData = {
        ...values,
        dueDate: values.dueDate?.format('YYYY-MM-DD')
      }
      if (editingMilestone) {
        await updateMilestone(id, editingMilestone.id, submitData)
        message.success('更新成功')
      } else {
        await createMilestone(id, submitData)
        message.success('创建成功')
      }
      setMilestoneModalVisible(false)
      setEditingMilestone(null)
      milestoneForm.resetFields()
      loadData()
    } catch (error) {
      console.error('Milestone submit error:', error)
    }
  }

  const handleEditMilestone = (milestone) => {
    setEditingMilestone(milestone)
    milestoneForm.setFieldsValue({
      ...milestone,
      dueDate: milestone.dueDate ? dayjs(milestone.dueDate) : null
    })
    setMilestoneModalVisible(true)
  }

  const handleDeleteMilestone = async (milestoneId) => {
    try {
      await deleteMilestone(id, milestoneId)
      message.success('删除成功')
      loadData()
    } catch (error) {
      console.error('Delete milestone error:', error)
    }
  }

  const handleSettingsSubmit = async () => {
    try {
      const values = await settingsForm.validateFields()
      const submitData = {
        ...values,
        startDate: values.startDate?.[0]?.format('YYYY-MM-DD'),
        endDate: values.startDate?.[1]?.format('YYYY-MM-DD')
      }
      delete submitData.startDate
      submitData.startDate = values.startDate?.[0]?.format('YYYY-MM-DD')
      submitData.endDate = values.startDate?.[1]?.format('YYYY-MM-DD')
      await updateProject(id, submitData)
      message.success('更新成功')
      setSettingsModalVisible(false)
      loadData()
    } catch (error) {
      console.error('Update settings error:', error)
    }
  }

  const openSettings = () => {
    settingsForm.setFieldsValue({
      ...project,
      startDate: project?.startDate && project?.endDate
        ? [dayjs(project.startDate), dayjs(project.endDate)]
        : null
    })
    setSettingsModalVisible(true)
  }

  const memberColumns = [
    {
      title: '成员',
      dataIndex: 'user',
      key: 'user',
      render: (user) => (
        <Space>
          <Avatar src={user?.avatar}>
            {user?.username?.charAt(0)?.toUpperCase()}
          </Avatar>
          <span>{user?.username}</span>
        </Space>
      )
    },
    {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      render: (role, record) => (
        <Select
          value={role}
          style={{ width: 120 }}
          options={roleOptions}
          onChange={(value) => handleRoleChange(record.id, value)}
        />
      )
    },
    {
      title: '加入时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (time) => dayjs(time).format('YYYY-MM-DD HH:mm')
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Popconfirm
          title="确定要移除该成员吗?"
          onConfirm={() => handleRemoveMember(record.id)}
        >
          <Button type="text" danger size="small">
            移除
          </Button>
        </Popconfirm>
      )
    }
  ]

  if (loading) {
    return (
      <div className={styles.loadingContainer}>
        <Spin size="large" />
      </div>
    )
  }

  if (!project) {
    return <Empty description="项目不存在" className={styles.empty} />
  }

  return (
    <div className={styles.projectDetail}>
      <div className={styles.pageHeader}>
        <div className={styles.headerLeft}>
          <Button
            type="text"
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate('/project')}
          >
            返回
          </Button>
          <div>
            <h1 className={styles.projectTitle}>{project.name}</h1>
            <div className={styles.projectMeta}>
              <Tag color={statusColors[project.status]}>
                {statusText[project.status]}
              </Tag>
              <span className={styles.metaText}>
                <CalendarOutlined />
                {dayjs(project.startDate).format('YYYY-MM-DD')} ~ {dayjs(project.endDate).format('YYYY-MM-DD')}
              </span>
              <span className={styles.metaText}>
                <TeamOutlined />
                {members.length} 位成员
              </span>
            </div>
          </div>
        </div>
        <div className={styles.headerRight}>
          <Button icon={<EditOutlined />} onClick={openSettings}>
            项目设置
          </Button>
        </div>
      </div>

      <Card className={styles.contentCard}>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'overview',
              label: '概览',
              children: (
                <div className={styles.tabContent}>
                  <Row gutter={[16, 16]} className={styles.statsRow}>
                    <Col xs={12} lg={6}>
                      <Card>
                        <Statistic
                          title="任务总数"
                          value={overview?.taskCount || 0}
                          valueStyle={{ color: '#1890ff' }}
                        />
                      </Card>
                    </Col>
                    <Col xs={12} lg={6}>
                      <Card>
                        <Statistic
                          title="已完成"
                          value={overview?.doneCount || 0}
                          valueStyle={{ color: '#52c41a' }}
                        />
                      </Card>
                    </Col>
                    <Col xs={12} lg={6}>
                      <Card>
                        <Statistic
                          title="进行中"
                          value={overview?.inProgressCount || 0}
                          valueStyle={{ color: '#fa8c16' }}
                        />
                      </Card>
                    </Col>
                    <Col xs={12} lg={6}>
                      <Card>
                        <Statistic
                          title="完成率"
                          value={overview?.completionRate || 0}
                          precision={1}
                          suffix="%"
                          valueStyle={{ color: '#722ed1' }}
                        />
                      </Card>
                    </Col>
                  </Row>

                  <Row gutter={[16, 16]} className={styles.chartRow}>
                    <Col xs={24} lg={12}>
                      <Card title="任务统计">
                        <ReactECharts
                          option={getTaskChartOption()}
                          style={{ height: 300 }}
                        />
                      </Card>
                    </Col>
                    <Col xs={24} lg={12}>
                      <Card title="项目进度">
                        <div className={styles.progressSection}>
                          <div className={styles.progressItem}>
                            <div className={styles.progressLabel}>
                              <span>总体进度</span>
                              <span>{project.progress || 0}%</span>
                            </div>
                            <Progress percent={project.progress || 0} />
                          </div>
                          <div className={styles.progressItem}>
                            <div className={styles.progressLabel}>
                              <span>里程碑完成</span>
                              <span>
                                {milestones.filter((m) => m.status === 'COMPLETED').length} / {milestones.length}
                              </span>
                            </div>
                            <Progress
                              percent={
                                milestones.length
                                  ? Math.round(
                                      (milestones.filter((m) => m.status === 'COMPLETED').length /
                                        milestones.length) *
                                        100
                                    )
                                  : 0
                              }
                            />
                          </div>
                        </div>
                      </Card>
                    </Col>
                  </Row>
                </div>
              )
            },
            {
              key: 'tasks',
              label: '任务',
              children: (
                <div className={styles.tabContent}>
                  <div className={styles.sectionHeader}>
                    <h3>任务列表</h3>
                    <Button
                      type="primary"
                      icon={<PlusOutlined />}
                      onClick={() => navigate(`/task/list?projectId=${id}`)}
                    >
                      新建任务
                    </Button>
                  </div>
                  {tasks.length > 0 ? (
                    <Row gutter={[16, 16]}>
                      {tasks.map((task) => (
                        <Col xs={24} md={12} lg={8} key={task.id}>
                          <TaskCard
                            task={task}
                            onClick={(t) => navigate(`/task/${t.id}`)}
                          />
                        </Col>
                      ))}
                    </Row>
                  ) : (
                    <Empty description="暂无任务" />
                  )}
                </div>
              )
            },
            {
              key: 'milestones',
              label: '里程碑',
              children: (
                <div className={styles.tabContent}>
                  <div className={styles.sectionHeader}>
                    <h3>里程碑列表</h3>
                    <Button
                      type="primary"
                      icon={<PlusOutlined />}
                      onClick={() => {
                        setEditingMilestone(null)
                        milestoneForm.resetFields()
                        setMilestoneModalVisible(true)
                      }}
                    >
                      新建里程碑
                    </Button>
                  </div>
                  {milestones.length > 0 ? (
                    <List
                      dataSource={milestones}
                      renderItem={(milestone) => (
                        <List.Item
                          key={milestone.id}
                          actions={[
                            <Button
                              key="edit"
                              type="text"
                              size="small"
                              icon={<EditOutlined />}
                              onClick={() => handleEditMilestone(milestone)}
                            >
                              编辑
                            </Button>,
                            <Popconfirm
                              key="delete"
                              title="确定要删除这个里程碑吗?"
                              onConfirm={() => handleDeleteMilestone(milestone.id)}
                            >
                              <Button
                                type="text"
                                size="small"
                                danger
                                icon={<DeleteOutlined />}
                              >
                                删除
                              </Button>
                            </Popconfirm>
                          ]}
                        >
                          <List.Item.Meta
                            title={
                              <div className={styles.milestoneTitle}>
                                <span>{milestone.name}</span>
                                <Tag color={milestoneStatusColors[milestone.status]}>
                                  {milestoneStatusText[milestone.status]}
                                </Tag>
                              </div>
                            }
                            description={
                              <div className={styles.milestoneDesc}>
                                <p>{milestone.description}</p>
                                <p className={styles.milestoneDate}>
                                  <CalendarOutlined /> 截止日期:{' '}
                                  {dayjs(milestone.dueDate).format('YYYY-MM-DD')}
                                </p>
                              </div>
                            }
                          />
                        </List.Item>
                      )}
                    />
                  ) : (
                    <Empty description="暂无里程碑" />
                  )}
                </div>
              )
            },
            {
              key: 'members',
              label: '成员',
              children: (
                <div className={styles.tabContent}>
                  <div className={styles.sectionHeader}>
                    <h3>项目成员</h3>
                    <Button
                      type="primary"
                      icon={<PlusOutlined />}
                      onClick={() => setMemberModalVisible(true)}
                    >
                      添加成员
                    </Button>
                  </div>
                  <Table
                    dataSource={members}
                    columns={memberColumns}
                    rowKey="id"
                    pagination={false}
                  />
                </div>
              )
            },
            {
              key: 'settings',
              label: '设置',
              children: (
                <div className={styles.tabContent}>
                  <Card title="基本信息" className={styles.settingsCard}>
                    <Form form={settingsForm} layout="vertical">
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
                              options={Object.keys(statusText).map((key) => ({
                                label: statusText[key],
                                value: key
                              }))}
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
                              options={[
                                { label: '高', value: 'HIGH' },
                                { label: '中', value: 'MEDIUM' },
                                { label: '低', value: 'LOW' }
                              ]}
                            />
                          </Form.Item>
                        </Col>
                      </Row>
                      <Form.Item
                        name="startDate"
                        label="项目周期"
                        rules={[{ required: true, message: '请选择项目周期' }]}
                      >
                        <RangePicker style={{ width: '100%' }} />
                      </Form.Item>
                      <Form.Item>
                        <Button type="primary" onClick={handleSettingsSubmit}>
                          保存设置
                        </Button>
                      </Form.Item>
                    </Form>
                  </Card>
                </div>
              )
            }
          ]}
        />
      </Card>

      <Modal
        title="添加项目成员"
        open={memberModalVisible}
        onOk={handleAddMember}
        onCancel={() => setMemberModalVisible(false)}
        destroyOnClose
      >
        <Form form={memberForm} layout="vertical">
          <Form.Item
            name="userId"
            label="选择用户"
            rules={[{ required: true, message: '请选择用户' }]}
          >
            <Select placeholder="请选择用户" showSearch optionFilterProp="label">
              {/* 这里应该从用户列表中选择，暂时留空 */}
            </Select>
          </Form.Item>
          <Form.Item
            name="role"
            label="角色"
            rules={[{ required: true, message: '请选择角色' }]}
          >
            <Select options={roleOptions} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={editingMilestone ? '编辑里程碑' : '新建里程碑'}
        open={milestoneModalVisible}
        onOk={handleMilestoneSubmit}
        onCancel={() => {
          setMilestoneModalVisible(false)
          setEditingMilestone(null)
        }}
        destroyOnClose
      >
        <Form form={milestoneForm} layout="vertical">
          <Form.Item
            name="name"
            label="里程碑名称"
            rules={[{ required: true, message: '请输入里程碑名称' }]}
          >
            <Input placeholder="请输入里程碑名称" />
          </Form.Item>
          <Form.Item
            name="description"
            label="里程碑描述"
            rules={[{ required: true, message: '请输入里程碑描述' }]}
          >
            <Input.TextArea rows={3} placeholder="请输入里程碑描述" />
          </Form.Item>
          <Form.Item
            name="status"
            label="状态"
            rules={[{ required: true, message: '请选择状态' }]}
          >
            <Select
              options={Object.keys(milestoneStatusText).map((key) => ({
                label: milestoneStatusText[key],
                value: key
              }))}
            />
          </Form.Item>
          <Form.Item
            name="dueDate"
            label="截止日期"
            rules={[{ required: true, message: '请选择截止日期' }]}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default ProjectDetail
