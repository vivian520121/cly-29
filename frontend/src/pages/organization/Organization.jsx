import { useState, useEffect } from 'react'
import {
  Card,
  Tree,
  Table,
  Button,
  Form,
  Input,
  Select,
  Modal,
  Popconfirm,
  message,
  Spin,
  Empty,
  Space,
  Avatar,
  Tag,
  Row,
  Col,
  Drawer,
  Checkbox
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  TeamOutlined,
  UserOutlined,
  ApartmentOutlined
} from '@ant-design/icons'
import {
  getDepartmentTree,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  getUserDepartments,
  updateUserDepartments
} from '@/services/organization'
import dayjs from 'dayjs'
import styles from './Organization.module.scss'

const { TreeNode } = Tree

const deptTypeOptions = [
  { label: '公司', value: 'COMPANY' },
  { label: '部门', value: 'DEPARTMENT' },
  { label: '小组', value: 'TEAM' }
]

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

const Organization = () => {
  const [loading, setLoading] = useState(false)
  const [deptTree, setDeptTree] = useState([])
  const [users, setUsers] = useState([])
  const [selectedDept, setSelectedDept] = useState(null)
  const [expandedKeys, setExpandedKeys] = useState([])
  const [deptModalVisible, setDeptModalVisible] = useState(false)
  const [userModalVisible, setUserModalVisible] = useState(false)
  const [deptDrawerVisible, setDeptDrawerVisible] = useState(false)
  const [editingDept, setEditingDept] = useState(null)
  const [editingUser, setEditingUser] = useState(null)
  const [deptForm] = Form.useForm()
  const [userForm] = Form.useForm()
  const [userDepts, setUserDepts] = useState([])
  const [selectedUserDepts, setSelectedUserDepts] = useState([])
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  })

  useEffect(() => {
    loadDeptTree()
  }, [])

  useEffect(() => {
    loadUsers()
  }, [selectedDept, pagination.current, pagination.pageSize])

  const loadDeptTree = async () => {
    setLoading(true)
    try {
      const data = await getDepartmentTree()
      setDeptTree(data || [])
      if (data && data.length > 0) {
        setExpandedKeys([data[0].id])
        setSelectedDept(data[0].id)
      }
    } catch (error) {
      console.error('Load department tree error:', error)
    } finally {
      setLoading(false)
    }
  }

  const loadUsers = async () => {
    if (!selectedDept) return
    setLoading(true)
    try {
      const data = await getUserList({
        departmentId: selectedDept,
        pageNum: pagination.current,
        pageSize: pagination.pageSize
      })
      setUsers(data?.records || [])
      setPagination((prev) => ({ ...prev, total: data?.total || 0 }))
    } catch (error) {
      console.error('Load users error:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleDeptSelect = (selectedKeys) => {
    if (selectedKeys.length > 0) {
      setSelectedDept(selectedKeys[0])
      setPagination((prev) => ({ ...prev, current: 1 }))
    }
  }

  const handleAddDept = () => {
    setEditingDept(null)
    deptForm.resetFields()
    deptForm.setFieldsValue({
      parentId: selectedDept
    })
    setDeptModalVisible(true)
  }

  const handleEditDept = (dept) => {
    setEditingDept(dept)
    deptForm.setFieldsValue(dept)
    setDeptModalVisible(true)
  }

  const handleDeleteDept = async (id) => {
    try {
      await deleteDepartment(id)
      message.success('删除成功')
      loadDeptTree()
    } catch (error) {
      console.error('Delete department error:', error)
    }
  }

  const handleDeptSubmit = async () => {
    try {
      const values = await deptForm.validateFields()
      if (editingDept) {
        await updateDepartment(editingDept.id, values)
        message.success('更新成功')
      } else {
        await createDepartment(values)
        message.success('创建成功')
      }
      setDeptModalVisible(false)
      loadDeptTree()
    } catch (error) {
      console.error('Submit department error:', error)
    }
  }

  const handleAddUser = () => {
    setEditingUser(null)
    userForm.resetFields()
    userForm.setFieldsValue({
      departmentId: selectedDept
    })
    setSelectedUserDepts([selectedDept])
    setUserModalVisible(true)
  }

  const handleEditUser = async (user) => {
    setEditingUser(user)
    userForm.setFieldsValue(user)
    try {
      const depts = await getUserDepartments(user.id)
      setUserDepts(depts || [])
      setSelectedUserDepts(depts?.map((d) => d.id) || [])
    } catch (error) {
      console.error('Get user departments error:', error)
    }
    setUserModalVisible(true)
  }

  const handleDeleteUser = async (id) => {
    try {
      await deleteUser(id)
      message.success('删除成功')
      loadUsers()
    } catch (error) {
      console.error('Delete user error:', error)
    }
  }

  const handleUserSubmit = async () => {
    try {
      const values = await userForm.validateFields()
      if (editingUser) {
        await updateUser(editingUser.id, values)
        if (selectedUserDepts.length > 0) {
          await updateUserDepartments(editingUser.id, {
            departmentIds: selectedUserDepts
          })
        }
        message.success('更新成功')
      } else {
        const user = await createUser(values)
        if (selectedUserDepts.length > 0) {
          await updateUserDepartments(user.id, {
            departmentIds: selectedUserDepts
          })
        }
        message.success('创建成功')
      }
      setUserModalVisible(false)
      loadUsers()
    } catch (error) {
      console.error('Submit user error:', error)
    }
  }

  const renderTreeNodes = (nodes) => {
    return nodes.map((node) => {
      return (
        <TreeNode
          key={node.id}
          title={
            <div className={styles.treeNode}>
              <ApartmentOutlined />
              <span>{node.name}</span>
              <div className={styles.treeActions}>
                <Button
                  type="text"
                  size="small"
                  icon={<EditOutlined />}
                  onClick={(e) => {
                    e.stopPropagation()
                    handleEditDept(node)
                  }}
                />
                <Popconfirm
                  title="确定要删除这个部门吗?"
                  onConfirm={(e) => {
                    e?.stopPropagation()
                    handleDeleteDept(node.id)
                  }}
                  onCancel={(e) => e?.stopPropagation()}
                >
                  <Button
                    type="text"
                    size="small"
                    danger
                    icon={<DeleteOutlined />}
                    onClick={(e) => e.stopPropagation()}
                  />
                </Popconfirm>
              </div>
            </div>
          }
        >
          {node.children && renderTreeNodes(node.children)}
        </TreeNode>
      )
    })
  }

  const getAllDeptIds = (nodes) => {
    let ids = []
    nodes.forEach((node) => {
      ids.push(node.id)
      if (node.children) {
        ids = ids.concat(getAllDeptIds(node.children))
      }
    })
    return ids
  }

  const flatDeptTree = (nodes) => {
    let result = []
    nodes.forEach((node) => {
      result.push({
        id: node.id,
        name: node.name,
        type: node.type
      })
      if (node.children) {
        result = result.concat(flatDeptTree(node.children))
      }
    })
    return result
  }

  const userColumns = [
    {
      title: '成员',
      dataIndex: 'user',
      key: 'user',
      render: (_, record) => (
        <Space>
          <Avatar src={record.avatar}>
            {record.username?.charAt(0)?.toUpperCase()}
          </Avatar>
          <div>
            <div className={styles.userName}>{record.username}</div>
            <div className={styles.userEmail}>{record.email}</div>
          </div>
        </Space>
      )
    },
    {
      title: '用户类型',
      dataIndex: 'userType',
      key: 'userType',
      width: 120,
      render: (type) => (
        <Tag color={userTypeColors[type]}>{userTypeText[type]}</Tag>
      )
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone',
      width: 120
    },
    {
      title: '所属部门',
      dataIndex: 'departments',
      key: 'departments',
      render: (depts) => (
        <Space wrap>
          {depts?.map((dept) => (
            <Tag key={dept.id} color="blue">
              {dept.name}
            </Tag>
          ))}
        </Space>
      )
    },
    {
      title: '入职日期',
      dataIndex: 'hireDate',
      key: 'hireDate',
      width: 120,
      render: (date) => (date ? dayjs(date).format('YYYY-MM-DD') : '-')
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Space>
          <Button
            type="text"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEditUser(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除该成员吗?"
            onConfirm={() => handleDeleteUser(record.id)}
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
    <div className={styles.organization}>
      <div className="page-header">
        <h1 className="page-title">组织架构</h1>
        <div className={styles.headerActions}>
          <Space>
            <Button icon={<PlusOutlined />} onClick={handleAddDept}>
              新增部门
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAddUser}>
              新增成员
            </Button>
          </Space>
        </div>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={8} xl={6}>
          <Card className={styles.treeCard} title="部门树">
            {loading && !deptTree.length ? (
              <div className={styles.loadingContainer}>
                <Spin size="large" />
              </div>
            ) : deptTree.length > 0 ? (
              <div className={styles.deptTree}>
                <Tree
                  showLine
                  expandedKeys={expandedKeys}
                  selectedKeys={selectedDept ? [selectedDept] : []}
                  onExpand={setExpandedKeys}
                  onSelect={handleDeptSelect}
                  defaultExpandAll
                >
                  {renderTreeNodes(deptTree)}
                </Tree>
              </div>
            ) : (
              <Empty description="暂无部门" />
            )}
          </Card>
        </Col>

        <Col xs={24} lg={16} xl={18}>
          <Card className={styles.tableCard}>
            <Table
              rowKey="id"
              loading={loading}
              columns={userColumns}
              dataSource={users}
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
            />
          </Card>
        </Col>
      </Row>

      <Modal
        title={editingDept ? '编辑部门' : '新增部门'}
        open={deptModalVisible}
        onOk={handleDeptSubmit}
        onCancel={() => setDeptModalVisible(false)}
        destroyOnClose
      >
        <Form form={deptForm} layout="vertical">
          <Form.Item
            name="name"
            label="部门名称"
            rules={[{ required: true, message: '请输入部门名称' }]}
          >
            <Input placeholder="请输入部门名称" />
          </Form.Item>
          <Form.Item
            name="parentId"
            label="上级部门"
            rules={[{ required: true, message: '请选择上级部门' }]}
          >
            <Select
              placeholder="请选择上级部门"
              options={flatDeptTree(deptTree).map((d) => ({
                label: d.name,
                value: d.id
              }))}
            />
          </Form.Item>
          <Form.Item
            name="type"
            label="部门类型"
            rules={[{ required: true, message: '请选择部门类型' }]}
          >
            <Select options={deptTypeOptions} />
          </Form.Item>
          <Form.Item name="description" label="部门描述">
            <Input.TextArea rows={3} placeholder="请输入部门描述" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={editingUser ? '编辑成员' : '新增成员'}
        open={userModalVisible}
        onOk={handleUserSubmit}
        onCancel={() => setUserModalVisible(false)}
        width={600}
        destroyOnClose
      >
        <Form form={userForm} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="username"
                label="用户名"
                rules={[{ required: true, message: '请输入用户名' }]}
              >
                <Input placeholder="请输入用户名" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="realName"
                label="真实姓名"
                rules={[{ required: true, message: '请输入真实姓名' }]}
              >
                <Input placeholder="请输入真实姓名" />
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
                <Input placeholder="请输入入职日期" />
              </Form.Item>
            </Col>
          </Row>
          {!editingUser && (
            <Form.Item
              name="password"
              label="初始密码"
              rules={[{ required: true, message: '请输入初始密码' }]}
            >
              <Input.Password placeholder="请输入初始密码" />
            </Form.Item>
          )}
          <Form.Item label="所属部门(支持多部门)">
            <Checkbox.Group
              value={selectedUserDepts}
              onChange={setSelectedUserDepts}
              options={flatDeptTree(deptTree).map((d) => ({
                label: d.name,
                value: d.id
              }))}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default Organization
