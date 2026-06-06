import { useState, useEffect } from 'react'
import {
  Card,
  Row,
  Col,
  Avatar,
  Button,
  Form,
  Input,
  Select,
  DatePicker,
  Tabs,
  List,
  Tag,
  Progress,
  Statistic,
  message,
  Spin,
  Empty,
  Upload,
  Space,
  Modal
} from 'antd'
import {
  UserOutlined,
  EditOutlined,
  CameraOutlined,
  LockOutlined,
  ProjectOutlined,
  UnorderedListOutlined,
  CalendarOutlined,
  MailOutlined,
  PhoneOutlined,
  EnvironmentOutlined
} from '@ant-design/icons'
import ReactECharts from 'echarts-for-react'
import { useAuthStore } from '@/store/authStore'
import {
  getProfile,
  updateProfile,
  updateAvatar,
  getMyProjects,
  getMyTaskStatistics
} from '@/services/profile'
import { updatePassword } from '@/services/auth'
import dayjs from 'dayjs'
import styles from './Profile.module.scss'

const { TabPane } = Tabs

const userTypeOptions = [
  { label: '正式员工', value: 'FORMAL' },
  { label: '实习生', value: 'INTERN' },
  { label: '外包', value: 'OUTSOURCING' }
]

const userTypeText = {
  FORMAL: '正式员工',
  INTERN: '实习生',
  OUTSOURCING: '外包'
}

const userTypeColors = {
  FORMAL: 'blue',
  INTERN: 'green',
  OUTSOURCING: 'orange'
}

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

const Profile = () => {
  const { userInfo, setUserInfo } = useAuthStore()
  const [loading, setLoading] = useState(true)
  const [profile, setProfile] = useState(null)
  const [projects, setProjects] = useState([])
  const [taskStats, setTaskStats] = useState(null)
  const [editModalVisible, setEditModalVisible] = useState(false)
  const [passwordModalVisible, setPasswordModalVisible] = useState(false)
  const [form] = Form.useForm()
  const [passwordForm] = Form.useForm()
  const [activeTab, setActiveTab] = useState('basic')

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const [profileData, projectsData, statsData] = await Promise.all([
        getProfile(),
        getMyProjects({ pageNum: 1, pageSize: 10 }),
        getMyTaskStatistics()
      ])
      setProfile(profileData)
      setProjects(projectsData?.records || [])
      setTaskStats(statsData)
    } catch (error) {
      console.error('Load profile error:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleAvatarUpload = async (file) => {
    try {
      const formData = new FormData()
      formData.append('file', file)
      const data = await updateAvatar(formData)
      setUserInfo({ ...userInfo, avatar: data.url })
      setProfile({ ...profile, avatar: data.url })
      message.success('头像更新成功')
    } catch (error) {
      console.error('Upload avatar error:', error)
    }
    return false
  }

  const handleEditSubmit = async () => {
    try {
      const values = await form.validateFields()
      const submitData = {
        ...values,
        hireDate: values.hireDate?.format('YYYY-MM-DD')
      }
      await updateProfile(submitData)
      message.success('更新成功')
      setEditModalVisible(false)
      loadData()
    } catch (error) {
      console.error('Update profile error:', error)
    }
  }

  const handlePasswordSubmit = async () => {
    try {
      const values = await passwordForm.validateFields()
      if (values.newPassword !== values.confirmPassword) {
        message.error('两次输入的密码不一致')
        return
      }
      await updatePassword({
        oldPassword: values.oldPassword,
        newPassword: values.newPassword
      })
      message.success('密码修改成功')
      setPasswordModalVisible(false)
      passwordForm.resetFields()
    } catch (error) {
      console.error('Update password error:', error)
    }
  }

  const openEditModal = () => {
    form.setFieldsValue({
      ...profile,
      hireDate: profile?.hireDate ? dayjs(profile.hireDate) : null
    })
    setEditModalVisible(true)
  }

  const getTaskChartOption = () => {
    if (!taskStats) return {}
    return {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '我的任务',
          type: 'pie',
          radius: ['40%', '70%'],
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          data: [
            { value: taskStats.todo || 0, name: '待办', itemStyle: { color: '#bfbfbf' } },
            { value: taskStats.inProgress || 0, name: '进行中', itemStyle: { color: '#1890ff' } },
            { value: taskStats.review || 0, name: '评审中', itemStyle: { color: '#faad14' } },
            { value: taskStats.done || 0, name: '已完成', itemStyle: { color: '#52c41a' } }
          ]
        }
      ]
    }
  }

  const getPriorityChartOption = () => {
    if (!taskStats) return {}
    const priorityData = taskStats.priorityStats || {}
    return {
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: ['高优先级', '中优先级', '低优先级']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '任务数',
          type: 'bar',
          data: [priorityData.HIGH || 0, priorityData.MEDIUM || 0, priorityData.LOW || 0],
          itemStyle: {
            color: function (params) {
              const colorList = ['#ff4d4f', '#faad14', '#1890ff']
              return colorList[params.dataIndex]
            },
            borderRadius: [4, 4, 0, 0]
          },
          barWidth: '40%'
        }
      ]
    }
  }

  if (loading) {
    return (
      <div className={styles.loadingContainer}>
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div className={styles.profile}>
      <Card className={styles.profileHeader}>
        <Row gutter={[24, 24]} align="middle">
          <Col xs={24} md={6} className={styles.avatarSection}>
            <div className={styles.avatarWrapper}>
              <Avatar size={120} src={profile?.avatar}>
                {profile?.username?.charAt(0)?.toUpperCase()}
              </Avatar>
              <Upload
                showUploadList={false}
                beforeUpload={handleAvatarUpload}
                accept="image/*"
              >
                <div className={styles.avatarOverlay}>
                  <CameraOutlined />
                  <span>更换头像</span>
                </div>
              </Upload>
            </div>
            <h2 className={styles.userName}>{profile?.realName || profile?.username}</h2>
            <Tag color={userTypeColors[profile?.userType]} className={styles.userType}>
              {userTypeText[profile?.userType]}
            </Tag>
          </Col>
          <Col xs={24} md={12} className={styles.infoSection}>
            <div className={styles.infoGrid}>
              <div className={styles.infoItem}>
                <UserOutlined />
                <span className={styles.infoLabel}>用户名:</span>
                <span className={styles.infoValue}>{profile?.username}</span>
              </div>
              <div className={styles.infoItem}>
                <MailOutlined />
                <span className={styles.infoLabel}>邮箱:</span>
                <span className={styles.infoValue}>{profile?.email}</span>
              </div>
              <div className={styles.infoItem}>
                <PhoneOutlined />
                <span className={styles.infoLabel}>手机号:</span>
                <span className={styles.infoValue}>{profile?.phone || '-'}</span>
              </div>
              <div className={styles.infoItem}>
                <CalendarOutlined />
                <span className={styles.infoLabel}>入职日期:</span>
                <span className={styles.infoValue}>
                  {profile?.hireDate ? dayjs(profile.hireDate).format('YYYY-MM-DD') : '-'}
                </span>
              </div>
              <div className={styles.infoItem}>
                <EnvironmentOutlined />
                <span className={styles.infoLabel}>部门:</span>
                <span className={styles.infoValue}>
                  {profile?.departments?.map((d) => d.name).join(', ') || '-'}
                </span>
              </div>
            </div>
          </Col>
          <Col xs={24} md={6} className={styles.actionSection}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Button
                type="primary"
                icon={<EditOutlined />}
                block
                onClick={openEditModal}
              >
                编辑资料
              </Button>
              <Button
                icon={<LockOutlined />}
                block
                onClick={() => setPasswordModalVisible(true)}
              >
                修改密码
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      <Card className={styles.contentCard}>
        <Tabs activeKey={activeTab} onChange={setActiveTab}>
          <TabPane tab="基本信息" key="basic">
            <Row gutter={[16, 16]} className={styles.statsRow}>
              <Col xs={12} lg={6}>
                <Card>
                  <Statistic
                    title="项目总数"
                    value={taskStats?.projectCount || 0}
                    prefix={<ProjectOutlined />}
                    valueStyle={{ color: '#1890ff' }}
                  />
                </Card>
              </Col>
              <Col xs={12} lg={6}>
                <Card>
                  <Statistic
                    title="任务总数"
                    value={taskStats?.total || 0}
                    prefix={<UnorderedListOutlined />}
                    valueStyle={{ color: '#722ed1' }}
                  />
                </Card>
              </Col>
              <Col xs={12} lg={6}>
                <Card>
                  <Statistic
                    title="已完成"
                    value={taskStats?.done || 0}
                    valueStyle={{ color: '#52c41a' }}
                  />
                </Card>
              </Col>
              <Col xs={12} lg={6}>
                <Card>
                  <Statistic
                    title="完成率"
                    value={taskStats?.completionRate || 0}
                    precision={1}
                    suffix="%"
                    valueStyle={{ color: '#fa8c16' }}
                  />
                </Card>
              </Col>
            </Row>
          </TabPane>

          <TabPane tab="我的项目" key="projects">
            {projects.length > 0 ? (
              <List
                dataSource={projects}
                renderItem={(project) => (
                  <List.Item
                    key={project.id}
                    actions={[
                      <Tag key="status" color={statusColors[project.status]}>
                        {statusText[project.status]}
                      </Tag>
                    ]}
                  >
                    <List.Item.Meta
                      avatar={<ProjectOutlined style={{ fontSize: '24px', color: '#1890ff' }} />}
                      title={project.name}
                      description={
                        <div className={styles.projectMeta}>
                          <span>
                            <CalendarOutlined /> {dayjs(project.startDate).format('YYYY-MM-DD')} ~{' '}
                            {dayjs(project.endDate).format('YYYY-MM-DD')}
                          </span>
                          <Progress
                            percent={project.progress || 0}
                            size="small"
                            style={{ width: 200 }}
                          />
                        </div>
                      }
                    />
                  </List.Item>
                )}
              />
            ) : (
              <Empty description="暂无项目" />
            )}
          </TabPane>

          <TabPane tab="我的任务统计" key="tasks">
            <Row gutter={[16, 16]}>
              <Col xs={24} lg={12}>
                <Card title="任务状态分布">
                  <ReactECharts option={getTaskChartOption()} style={{ height: 300 }} />
                </Card>
              </Col>
              <Col xs={24} lg={12}>
                <Card title="优先级分布">
                  <ReactECharts option={getPriorityChartOption()} style={{ height: 300 }} />
                </Card>
              </Col>
            </Row>
          </TabPane>
        </Tabs>
      </Card>

      <Modal
        title="编辑资料"
        open={editModalVisible}
        onOk={handleEditSubmit}
        onCancel={() => setEditModalVisible(false)}
        width={600}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="realName"
                label="真实姓名"
                rules={[{ required: true, message: '请输入真实姓名' }]}
              >
                <Input placeholder="请输入真实姓名" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="username"
                label="用户名"
                rules={[{ required: true, message: '请输入用户名' }]}
              >
                <Input placeholder="请输入用户名" disabled />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="email"
                label="邮箱"
                rules={[
                  { required: true, message: '请输入邮箱' },
                  { type: 'email', message: '请输入有效的邮箱地址' }
                ]}
              >
                <Input placeholder="请输入邮箱" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="phone" label="手机号">
                <Input placeholder="请输入手机号" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="userType"
                label="用户类型"
                rules={[{ required: true, message: '请选择用户类型' }]}
              >
                <Select options={userTypeOptions} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="hireDate" label="入职日期">
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>

      <Modal
        title="修改密码"
        open={passwordModalVisible}
        onOk={handlePasswordSubmit}
        onCancel={() => setPasswordModalVisible(false)}
        width={400}
        destroyOnClose
      >
        <Form form={passwordForm} layout="vertical">
          <Form.Item
            name="oldPassword"
            label="原密码"
            rules={[{ required: true, message: '请输入原密码' }]}
          >
            <Input.Password placeholder="请输入原密码" />
          </Form.Item>
          <Form.Item
            name="newPassword"
            label="新密码"
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 6, message: '密码至少6位' }
            ]}
          >
            <Input.Password placeholder="请输入新密码" />
          </Form.Item>
          <Form.Item
            name="confirmPassword"
            label="确认新密码"
            rules={[{ required: true, message: '请再次输入新密码' }]}
          >
            <Input.Password placeholder="请再次输入新密码" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default Profile
