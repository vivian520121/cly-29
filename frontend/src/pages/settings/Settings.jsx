import { useState, useEffect } from 'react'
import {
  Card,
  Button,
  Form,
  Input,
  Modal,
  message,
  Spin,
  Empty,
  Space,
  Row,
  Col,
  Tabs,
  Table,
  Popconfirm,
  Tag
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ApartmentOutlined,
  SettingOutlined,
  GlobalOutlined,
  PhoneOutlined,
  MailOutlined,
  EnvironmentOutlined
} from '@ant-design/icons'
import {
  getCompanyList,
  getCompanyListAll,
  getCompanyDetail,
  createCompany,
  updateCompany,
  deleteCompany
} from '@/services/company'
import dayjs from 'dayjs'
import styles from './Settings.module.scss'

const { TabPane } = Tabs

const Settings = () => {
  const [loading, setLoading] = useState(false)
  const [companies, setCompanies] = useState([])
  const [selectedCompany, setSelectedCompany] = useState(null)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingCompany, setEditingCompany] = useState(null)
  const [form] = Form.useForm()
  const [activeTab, setActiveTab] = useState('company')
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  })

  useEffect(() => {
    loadCompanies()
  }, [pagination.current, pagination.pageSize])

  useEffect(() => {
    if (companies.length > 0 && !selectedCompany) {
      setSelectedCompany(companies[0])
    }
  }, [companies])

  const loadCompanies = async () => {
    setLoading(true)
    try {
      const data = await getCompanyList({
        pageNum: pagination.current,
        pageSize: pagination.pageSize
      })
      setCompanies(data?.records || [])
      setPagination((prev) => ({ ...prev, total: data?.total || 0 }))
      if (data?.records?.length > 0 && !selectedCompany) {
        setSelectedCompany(data.records[0])
      }
    } catch (error) {
      console.error('Load companies error:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleAddCompany = () => {
    setEditingCompany(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEditCompany = (company) => {
    setEditingCompany(company)
    form.setFieldsValue(company)
    setModalVisible(true)
  }

  const handleDeleteCompany = async (id) => {
    try {
      await deleteCompany(id)
      message.success('删除成功')
      loadCompanies()
      if (selectedCompany?.id === id) {
        setSelectedCompany(null)
      }
    } catch (error) {
      console.error('Delete company error:', error)
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      if (editingCompany) {
        await updateCompany({ ...editingCompany, ...values })
        message.success('更新成功')
      } else {
        await createCompany(values)
        message.success('创建成功')
      }
      setModalVisible(false)
      loadCompanies()
    } catch (error) {
      console.error('Submit company error:', error)
    }
  }

  const columns = [
    {
      title: '公司名称',
      dataIndex: 'name',
      key: 'name',
      render: (text) => (
        <Space>
          <ApartmentOutlined style={{ color: '#1890ff' }} />
          <span>{text}</span>
        </Space>
      )
    },
    {
      title: '公司编码',
      dataIndex: 'code',
      key: 'code'
    },
    {
      title: '联系人',
      dataIndex: 'contactPerson',
      key: 'contactPerson'
    },
    {
      title: '联系电话',
      dataIndex: 'contactPhone',
      key: 'contactPhone'
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => (
        <Tag color={status === 1 ? 'green' : 'default'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
      )
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (date) => (date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-')
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="text"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEditCompany(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除该公司吗?"
            onConfirm={() => handleDeleteCompany(record.id)}
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
    <div className={styles.settings}>
      <div className="page-header">
        <h1 className="page-title">系统设置</h1>
      </div>

      <Card className={styles.companyCard}>
        <Tabs activeKey={activeTab} onChange={setActiveTab}>
          <TabPane tab="公司信息" key="company">
            {selectedCompany ? (
              <div className={styles.companyInfo}>
                <div className={styles.companyLogo}>
                  <ApartmentOutlined />
                </div>
                <div className={styles.companyDetails}>
                  <h2 className={styles.companyName}>{selectedCompany.name}</h2>
                  <div className={styles.companyCode}>编码: {selectedCompany.code}</div>
                  <div className={styles.infoGrid}>
                    <div className={styles.infoItem}>
                      <span className={styles.infoLabel}>
                        <GlobalOutlined /> 官网:
                      </span>
                      <span className={styles.infoValue}>
                        {selectedCompany.website || '-'}
                      </span>
                    </div>
                    <div className={styles.infoItem}>
                      <span className={styles.infoLabel}>
                        <PhoneOutlined /> 电话:
                      </span>
                      <span className={styles.infoValue}>
                        {selectedCompany.contactPhone || '-'}
                      </span>
                    </div>
                    <div className={styles.infoItem}>
                      <span className={styles.infoLabel}>
                        <MailOutlined /> 邮箱:
                      </span>
                      <span className={styles.infoValue}>
                        {selectedCompany.email || '-'}
                      </span>
                    </div>
                    <div className={styles.infoItem}>
                      <span className={styles.infoLabel}>
                        <EnvironmentOutlined /> 地址:
                      </span>
                      <span className={styles.infoValue}>
                        {selectedCompany.address || '-'}
                      </span>
                    </div>
                  </div>
                  <div className={styles.actionSection}>
                    <Button
                      type="primary"
                      icon={<EditOutlined />}
                      onClick={() => handleEditCompany(selectedCompany)}
                    >
                      编辑公司信息
                    </Button>
                  </div>
                </div>
              </div>
            ) : (
              <Empty description="暂无公司信息" />
            )}
          </TabPane>

          <TabPane tab="公司管理" key="manage">
            <div style={{ marginBottom: 16, textAlign: 'right' }}>
              <Button type="primary" icon={<PlusOutlined />} onClick={handleAddCompany}>
                新增公司
              </Button>
            </div>
            <Table
              rowKey="id"
              loading={loading}
              columns={columns}
              dataSource={companies}
              pagination={{
                ...pagination,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total) => `共 ${total} 条记录`
              }}
              onChange={(p) =>
                setPagination((prev) => ({
                  ...prev,
                  current: p.current,
                  pageSize: p.pageSize
                }))
              }
              onRow={(record) => ({
                onClick: () => setSelectedCompany(record)
              })}
            />
          </TabPane>
        </Tabs>
      </Card>

      <Modal
        title={editingCompany ? '编辑公司' : '新增公司'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="name"
                label="公司名称"
                rules={[{ required: true, message: '请输入公司名称' }]}
              >
                <Input placeholder="请输入公司名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="code"
                label="公司编码"
                rules={[{ required: true, message: '请输入公司编码' }]}
              >
                <Input placeholder="请输入公司编码" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="contactPerson" label="联系人">
                <Input placeholder="请输入联系人" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="contactPhone" label="联系电话">
                <Input placeholder="请输入联系电话" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="email" label="邮箱">
                <Input placeholder="请输入邮箱" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="website" label="官网">
                <Input placeholder="请输入官网地址" />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="address" label="地址">
            <Input placeholder="请输入公司地址" />
          </Form.Item>
          <Form.Item name="description" label="公司描述">
            <Input.TextArea rows={3} placeholder="请输入公司描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default Settings
