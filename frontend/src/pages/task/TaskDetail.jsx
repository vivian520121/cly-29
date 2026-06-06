import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Card,
  Row,
  Col,
  Tag,
  Avatar,
  Button,
  Form,
  Input,
  Select,
  DatePicker,
  InputNumber,
  Modal,
  Drawer,
  List,
  Timeline,
  Upload,
  message,
  Popconfirm,
  Spin,
  Empty,
  Space,
  Divider,
  Progress,
  Checkbox
} from 'antd'
import {
  ArrowLeftOutlined,
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
  UserOutlined,
  CalendarOutlined,
  ClockCircleOutlined,
  PaperClipOutlined,
  HistoryOutlined,
  DownloadOutlined,
  EyeOutlined,
  DeleteTwoTone
} from '@ant-design/icons'
import {
  getTaskDetail,
  getTaskSubtasks,
  getTaskLogs,
  getTaskWorklogs,
  getTaskAttachments,
  addWorklog,
  updateTask,
  deleteTask
} from '@/services/task'
import { previewFile, uploadFile } from '@/services/file'
import dayjs from 'dayjs'
import styles from './TaskDetail.module.scss'

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

const actionTypeText = {
  CREATE: '创建了任务',
  UPDATE: '更新了任务',
  STATUS_CHANGE: '变更状态',
  ASSIGNEE_CHANGE: '变更负责人',
  PRIORITY_CHANGE: '变更优先级',
  COMMENT: '添加了评论',
  ATTACHMENT: '添加了附件',
  WORKLOG: '记录了工时'
}

const TaskDetail = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [task, setTask] = useState(null)
  const [subtasks, setSubtasks] = useState([])
  const [logs, setLogs] = useState([])
  const [worklogs, setWorklogs] = useState([])
  const [attachments, setAttachments] = useState([])
  const [editDrawerVisible, setEditDrawerVisible] = useState(false)
  const [worklogModalVisible, setWorklogModalVisible] = useState(false)
  const [form] = Form.useForm()
  const [worklogForm] = Form.useForm()
  const [activeTab, setActiveTab] = useState('overview')

  useEffect(() => {
    if (id) {
      loadData()
    }
  }, [id])

  const loadData = async () => {
    setLoading(true)
    try {
      const [taskData, subtasksData, logsData, worklogsData, attachmentsData] = await Promise.all([
        getTaskDetail(id),
        getTaskSubtasks(id),
        getTaskLogs(id),
        getTaskWorklogs(id),
        getTaskAttachments(id)
      ])
      setTask(taskData)
      setSubtasks(subtasksData || [])
      setLogs(logsData || [])
      setWorklogs(worklogsData || [])
      setAttachments(attachmentsData || [])
    } catch (error) {
      console.error('Load task detail error:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleEdit = () => {
    form.setFieldsValue({
      ...task,
      dueDate: task?.dueDate ? dayjs(task.dueDate) : null
    })
    setEditDrawerVisible(true)
  }

  const handleEditSubmit = async () => {
    try {
      const values = await form.validateFields()
      const submitData = {
        ...values,
        dueDate: values.dueDate?.format('YYYY-MM-DD')
      }
      await updateTask(id, submitData)
      message.success('更新成功')
      setEditDrawerVisible(false)
      loadData()
    } catch (error) {
      console.error('Update task error:', error)
    }
  }

  const handleDelete = async () => {
    try {
      await deleteTask(id)
      message.success('删除成功')
      navigate('/task/list')
    } catch (error) {
      console.error('Delete task error:', error)
    }
  }

  const handleWorklogSubmit = async () => {
    try {
      const values = await worklogForm.validateFields()
      await addWorklog(id, values)
      message.success('工时记录成功')
      setWorklogModalVisible(false)
      worklogForm.resetFields()
      loadData()
    } catch (error) {
      console.error('Add worklog error:', error)
    }
  }

  const handleSubtaskToggle = async (subtaskId, checked) => {
    try {
      await updateTask(subtaskId, { status: checked ? 'DONE' : 'TODO' })
      message.success(checked ? '子任务已完成' : '子任务已取消完成')
      loadData()
    } catch (error) {
      console.error('Update subtask error:', error)
    }
  }

  const handleFileUpload = async (file) => {
    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('taskId', id)
      await uploadFile(file)
      message.success('上传成功')
      loadData()
    } catch (error) {
      console.error('Upload error:', error)
    }
    return false
  }

  const handlePreview = async (fileId) => {
    try {
      const data = await previewFile(fileId)
      window.open(data.url, '_blank')
    } catch (error) {
      console.error('Preview error:', error)
    }
  }

  const handleDownload = async (fileId) => {
    try {
      const blob = await previewFile(fileId)
      const url = window.URL.createObjectURL(new Blob([blob]))
      const link = document.createElement('a')
      link.href = url
      link.download = 'file'
      link.click()
    } catch (error) {
      console.error('Download error:', error)
    }
  }

  const isOverdue = task?.dueDate && dayjs(task.dueDate).isBefore(dayjs(), 'day')
  const completedSubtasks = subtasks.filter((s) => s.status === 'DONE').length
  const subtaskProgress = subtasks.length ? Math.round((completedSubtasks / subtasks.length) * 100) : 0

  if (loading) {
    return (
      <div className={styles.loadingContainer}>
        <Spin size="large" />
      </div>
    )
  }

  if (!task) {
    return <Empty description="任务不存在" className={styles.empty} />
  }

  return (
    <div className={styles.taskDetail}>
      <div className={styles.pageHeader}>
        <div className={styles.headerLeft}>
          <Button
            type="text"
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate(-1)}
          >
            返回
          </Button>
          <div>
            <h1 className={styles.taskTitle}>{task.name}</h1>
            <div className={styles.taskMeta}>
              <Tag color={typeColors[task.type]}>{typeText[task.type]}</Tag>
              <Tag color={statusColors[task.status]}>{statusText[task.status]}</Tag>
              <Tag color={priorityColors[task.priority]}>
                {priorityText[task.priority]}优先级
              </Tag>
              <span className={styles.metaText}>
                <UserOutlined /> 负责人:
                <Avatar size="small" src={task.assignee?.avatar}>
                  {task.assignee?.username?.charAt(0)?.toUpperCase()}
                </Avatar>
                {task.assignee?.username || '未分配'}
              </span>
              <span className={styles.metaText}>
                <CalendarOutlined />
                所属项目: {task.project?.name || '未关联'}
              </span>
              <span className={`${styles.metaText} ${isOverdue ? styles.overdue : ''}`}>
                <ClockCircleOutlined />
                截止日期: {task.dueDate ? dayjs(task.dueDate).format('YYYY-MM-DD') : '无'}
                {isOverdue && <Tag color="red">已逾期</Tag>}
              </span>
            </div>
          </div>
        </div>
        <div className={styles.headerRight}>
          <Space>
            <Button icon={<EditOutlined />} onClick={handleEdit}>
              编辑
            </Button>
            <Popconfirm title="确定要删除这个任务吗?" onConfirm={handleDelete}>
              <Button danger icon={<DeleteOutlined />}>
                删除
              </Button>
            </Popconfirm>
          </Space>
        </div>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card className={styles.contentCard}>
            <div className={styles.section}>
              <h3 className={styles.sectionTitle}>任务描述</h3>
              <div className={styles.description}>
                {task.description || '暂无描述'}
              </div>
            </div>

            <Divider />

            <div className={styles.section}>
              <div className={styles.sectionHeader}>
                <h3 className={styles.sectionTitle}>
                  子任务 ({completedSubtasks}/{subtasks.length})
                </h3>
                <Button type="link" size="small" icon={<PlusOutlined />}>
                  添加子任务
                </Button>
              </div>
              {subtasks.length > 0 && (
                <div>
                  <Progress percent={subtaskProgress} size="small" className={styles.subtaskProgress} />
                  <List
                    dataSource={subtasks}
                    renderItem={(subtask) => (
                      <List.Item key={subtask.id}>
                        <Checkbox
                        checked={subtask.status === 'DONE'}
                        onChange={(e) => handleSubtaskToggle(subtask.id, e.target.checked)}
                      >
                        <span className={subtask.status === 'DONE' ? styles.completedSubtask : ''}>
                          {subtask.name}
                        </span>
                      </Checkbox>
                      </List.Item>
                    )}
                  />
                </div>
              )}
              {subtasks.length === 0 && (
                <Empty description="暂无子任务" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </div>

            <Divider />

            <div className={styles.section}>
              <div className={styles.sectionHeader}>
                <h3 className={styles.sectionTitle}>
                  <HistoryOutlined /> 流转日志
                </h3>
              </div>
              {logs.length > 0 ? (
                <Timeline
                  className={styles.timeline}
                  items={logs.map((log) => ({
                    color: log.actionType === 'CREATE' ? 'blue' : 'gray',
                    children: (
                      <div className={styles.logItem}>
                        <div className={styles.logHeader}>
                          <Avatar size="small" src={log.operator?.avatar}>
                            {log.operator?.username?.charAt(0)?.toUpperCase()}
                          </Avatar>
                          <span className={styles.logUser}>{log.operator?.username || '系统'}</span>
                          <span className={styles.logAction}>
                            {actionTypeText[log.actionType] || log.actionType}
                          </span>
                          <span className={styles.logTime}>
                            {dayjs(log.createTime).format('YYYY-MM-DD HH:mm')}
                          </span>
                        </div>
                        {log.remark && (
                          <div className={styles.logContent}>{log.remark}</div>
                        )}
                        {log.oldValue && log.newValue && (
                          <div className={styles.logChange}>
                            <span className={styles.oldValue}>{log.oldValue}</span>
                            <span className={styles.arrow}>→</span>
                            <span className={styles.newValue}>{log.newValue}</span>
                          </div>
                        )}
                      </div>
                    )
                  }))}
                />
              ) : (
                <Empty description="暂无日志" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </div>
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card className={styles.sidebarCard}>
            <div className={styles.section}>
              <div className={styles.sectionHeader}>
                <h3 className={styles.sectionTitle}>
                  <ClockCircleOutlined /> 工时填报
                </h3>
                <Button
                  type="link"
                  size="small"
                  icon={<PlusOutlined />}
                  onClick={() => setWorklogModalVisible(true)}
                >
                  记录工时
                </Button>
              </div>
              <div className={styles.worklogTotal}>
                <div className={styles.worklogStat}>
                  <span className={styles.worklogNumber}>
                    {worklogs.reduce((sum, w) => sum + w.hours, 0)}
                  </span>
                  <span className={styles.worklogUnit}>小时</span>
                </div>
                <span className={styles.worklogLabel}>已记录总工时</span>
              </div>
              {worklogs.length > 0 ? (
                <List
                  size="small"
                  dataSource={worklogs}
                  renderItem={(worklog) => (
                    <List.Item key={worklog.id}>
                      <div className={styles.worklogItem}>
                        <div className={styles.worklogHeader}>
                          <Avatar size="small" src={worklog.user?.avatar}>
                            {worklog.user?.username?.charAt(0)?.toUpperCase()}
                          </Avatar>
                          <span className={styles.worklogUser}>
                            {worklog.user?.username}
                          </span>
                          <Tag color="blue" className={styles.worklogHours}>
                            {worklog.hours}h
                          </Tag>
                        </div>
                        <div className={styles.worklogContent}>
                          {worklog.description}
                        </div>
                        <div className={styles.worklogDate}>
                          {dayjs(worklog.workDate).format('YYYY-MM-DD')}
                        </div>
                      </div>
                    </List.Item>
                  )}
                />
              ) : (
                <Empty description="暂无工时记录" image={Empty.PRESENTED_IMAGE_SIMPLE} />
              )}
            </div>

            <Divider />

            <div className={styles.section}>
              <div className={styles.sectionHeader}>
                <h3 className={styles.sectionTitle}>
                  <PaperClipOutlined /> 附件管理
                </h3>
                <Upload
                  showUploadList={false}
                  beforeUpload={handleFileUpload}
                  accept="*"
                >
                  <Button type="link" size="small" icon={<PlusOutlined />}>
                    上传附件
                  </Button>
                </Upload>
              </div>
              {attachments.length > 0 ? (
                <List
                  size="small"
                  dataSource={attachments}
                  renderItem={(file) => (
                    <List.Item
                    key={file.id}
                    actions={[
                      <Button
                        type="text"
                        size="small"
                        icon={<EyeOutlined />}
                        onClick={() => handlePreview(file.id)}
                      >
                        预览
                      </Button>,
                      <Button
                        type="text"
                        size="small"
                        icon={<DownloadOutlined />}
                        onClick={() => handleDownload(file.id)}
                      >
                        下载
                      </Button>
                    ]}
                  >
                    <List.Item.Meta
                      avatar={<PaperClipOutlined />}
                      title={
                        <span className={styles.fileName}>{file.name}</span>
                      }
                      description={
                        <span className={styles.fileInfo}>
                          {(file.size / 1024).toFixed(2)} KB
                        </span>
                      }
                    />
                  </List.Item>
                )}
              />
            ) : (
              <Empty description="暂无附件" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </div>
        </Card>
      </Col>
    </Row>

    <Drawer
      title="编辑任务"
      open={editDrawerVisible}
      onClose={() => setEditDrawerVisible(false)}
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
          <Col span={12}>
            <Form.Item
              name="type"
              label="任务类型"
              rules={[{ required: true, message: '请选择任务类型' }]}
            >
              <Select
                options={Object.keys(typeText).map((key) => ({
                  label: typeText[key],
                  value: key
                }))}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              name="status"
              label="任务状态"
              rules={[{ required: true, message: '请选择任务状态' }]}
            >
              <Select
                options={Object.keys(statusText).map((key) => ({
                  label: statusText[key],
                  value: key
                }))}
              />
            </Form.Item>
          </Col>
        </Row>
        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              name="priority"
              label="优先级"
              rules={[{ required: true, message: '请选择优先级' }]}
            >
              <Select
                options={Object.keys(priorityText).map((key) => ({
                  label: priorityText[key],
                  value: key
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
        <Form.Item>
          <Button type="primary" onClick={handleEditSubmit}>
            保存修改
          </Button>
        </Form.Item>
      </Form>
    </Drawer>

    <Modal
      title="记录工时"
      open={worklogModalVisible}
      onOk={handleWorklogSubmit}
      onCancel={() => setWorklogModalVisible(false)}
      destroyOnClose
    >
      <Form form={worklogForm} layout="vertical">
        <Form.Item
          name="hours"
          label="工时(小时)"
          rules={[{ required: true, message: '请输入工时' }]}
        >
          <InputNumber min={0.5} max={24} step={0.5} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item
          name="workDate"
          label="工作日期"
          rules={[{ required: true, message: '请选择日期' }]}
        >
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item
          name="description"
          label="工作内容"
          rules={[{ required: true, message: '请输入工作内容' }]}
        >
          <Input.TextArea rows={3} placeholder="请输入工作内容" />
        </Form.Item>
      </Form>
    </Modal>
  </div>
  )
}

export default TaskDetail
