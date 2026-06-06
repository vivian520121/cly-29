import { useState, useEffect } from 'react'
import {
  Card,
  Table,
  Button,
  Input,
  Select,
  Modal,
  Popconfirm,
  message,
  Spin,
  Empty,
  Space,
  Tag,
  Progress,
  Row,
  Col,
  Statistic,
  Upload
} from 'antd'
import {
  PlusOutlined,
  DownloadOutlined,
  DeleteOutlined,
  EyeOutlined,
  SearchOutlined,
  UploadOutlined,
  FileOutlined,
  FolderOutlined,
  PictureOutlined,
  FileTextOutlined,
  VideoCameraOutlined,
  ReloadOutlined
} from '@ant-design/icons'
import {
  getFileList,
  deleteFile,
  previewFile,
  downloadFile
} from '@/services/file'
import FileUploader from '@/components/FileUploader'
import dayjs from 'dayjs'
import styles from './FileManager.module.scss'

const fileTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '文档', value: 'DOCUMENT' },
  { label: '图片', value: 'IMAGE' },
  { label: '视频', value: 'VIDEO' },
  { label: '其他', value: 'OTHER' }
]

const fileTypeIcons = {
  DOCUMENT: <FileTextOutlined />,
  IMAGE: <PictureOutlined />,
  VIDEO: <VideoCameraOutlined />,
  OTHER: <FileOutlined />
}

const fileTypeColors = {
  DOCUMENT: 'blue',
  IMAGE: 'green',
  VIDEO: 'purple',
  OTHER: 'default'
}

const fileTypeText = {
  DOCUMENT: '文档',
  IMAGE: '图片',
  VIDEO: '视频',
  OTHER: '其他'
}

const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

const FileManager = () => {
  const [loading, setLoading] = useState(false)
  const [files, setFiles] = useState([])
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0
  })
  const [filters, setFilters] = useState({
    keyword: '',
    fileType: ''
  })
  const [uploadModalVisible, setUploadModalVisible] = useState(false)
  const [previewModalVisible, setPreviewModalVisible] = useState(false)
  const [previewFileData, setPreviewFileData] = useState(null)
  const [statistics, setStatistics] = useState({
    total: 0,
    totalSize: 0,
    documentCount: 0,
    imageCount: 0,
    videoCount: 0
  })

  useEffect(() => {
    loadFiles()
  }, [pagination.current, pagination.pageSize, filters])

  const loadFiles = async () => {
    setLoading(true)
    try {
      const data = await getFileList({
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
        ...filters
      })
      setFiles(data?.records || [])
      setPagination((prev) => ({ ...prev, total: data?.total || 0 }))

      if (data?.records) {
        setStatistics({
          total: data.total || 0,
          totalSize: data.records.reduce((sum, f) => sum + f.size, 0),
          documentCount: data.records.filter((f) => f.fileType === 'DOCUMENT').length,
          imageCount: data.records.filter((f) => f.fileType === 'IMAGE').length,
          videoCount: data.records.filter((f) => f.fileType === 'VIDEO').length
        })
      }
    } catch (error) {
      console.error('Load files error:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = (value) => {
    setFilters((prev) => ({ ...prev, keyword: value }))
    setPagination((prev) => ({ ...prev, current: 1 }))
  }

  const handleTypeChange = (value) => {
    setFilters((prev) => ({ ...prev, fileType: value || '' }))
    setPagination((prev) => ({ ...prev, current: 1 }))
  }

  const handleDelete = async (id) => {
    try {
      await deleteFile(id)
      message.success('删除成功')
      loadFiles()
    } catch (error) {
      console.error('Delete file error:', error)
    }
  }

  const handlePreview = async (file) => {
    try {
      const data = await previewFile(file.id)
      setPreviewFileData({
        ...file,
        url: data.url || `/uploads/${file.path}`
      })
      setPreviewModalVisible(true)
    } catch (error) {
      console.error('Preview file error:', error)
    }
  }

  const handleDownload = async (file) => {
    try {
      const blob = await downloadFile(file.id)
      const url = window.URL.createObjectURL(new Blob([blob]))
      const link = document.createElement('a')
      link.href = url
      link.download = file.name
      link.click()
      window.URL.revokeObjectURL(url)
      message.success('下载成功')
    } catch (error) {
      console.error('Download file error:', error)
    }
  }

  const handleUploadSuccess = () => {
    setUploadModalVisible(false)
    loadFiles()
  }

  const renderPreviewContent = () => {
    if (!previewFileData) return null

    const { fileType, url, name } = previewFileData

    if (fileType === 'IMAGE') {
      return (
        <div className={styles.previewImage}>
          <img src={url} alt={name} style={{ maxWidth: '100%', maxHeight: '60vh' }} />
        </div>
      )
    }

    if (fileType === 'VIDEO') {
      return (
        <div className={styles.previewVideo}>
          <video src={url} controls style={{ width: '100%', maxHeight: '60vh' }} />
        </div>
      )
    }

    return (
      <div className={styles.previewDocument}>
        <iframe
          src={url}
          title={name}
          style={{ width: '100%', height: '60vh', border: 'none' }}
        />
      </div>
    )
  }

  const columns = [
    {
      title: '文件名',
      dataIndex: 'name',
      key: 'name',
      render: (text, record) => (
        <Space>
          <span style={{ color: '#1890ff' }}>
            {fileTypeIcons[record.fileType] || <FileOutlined />}
          </span>
          <span className={styles.fileName}>{text}</span>
        </Space>
      )
    },
    {
      title: '类型',
      dataIndex: 'fileType',
      key: 'fileType',
      width: 100,
      render: (type) => (
        <Tag color={fileTypeColors[type]}>{fileTypeText[type]}</Tag>
      )
    },
    {
      title: '大小',
      dataIndex: 'size',
      key: 'size',
      width: 120,
      render: (size) => formatFileSize(size),
      sorter: true
    },
    {
      title: '上传者',
      dataIndex: 'uploader',
      key: 'uploader',
      width: 120,
      render: (uploader) => uploader?.username || '-'
    },
    {
      title: '上传时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 160,
      render: (time) => dayjs(time).format('YYYY-MM-DD HH:mm'),
      sorter: true
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button
            type="text"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handlePreview(record)}
          >
            预览
          </Button>
          <Button
            type="text"
            size="small"
            icon={<DownloadOutlined />}
            onClick={() => handleDownload(record)}
          >
            下载
          </Button>
          <Popconfirm
            title="确定要删除这个文件吗?"
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
    <div className={styles.fileManager}>
      <div className="page-header">
        <h1 className="page-title">文件管理</h1>
        <div className={styles.headerActions}>
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadFiles}>
              刷新
            </Button>
            <Button
              type="primary"
              icon={<UploadOutlined />}
              onClick={() => setUploadModalVisible(true)}
            >
              上传文件
            </Button>
          </Space>
        </div>
      </div>

      <Row gutter={[16, 16]} className={styles.statsRow}>
        <Col xs={12} lg={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="文件总数"
              value={statistics.total}
              prefix={<FileOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={12} lg={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="总大小"
              value={parseFloat((statistics.totalSize / (1024 * 1024)).toFixed(2))}
              suffix="MB"
              prefix={<FolderOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={12} lg={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="文档"
              value={statistics.documentCount}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={12} lg={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="图片"
              value={statistics.imageCount}
              prefix={<PictureOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      <Card className={styles.filterCard}>
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} sm={12} md={8}>
            <Input.Search
              placeholder="搜索文件名"
              allowClear
              onSearch={handleSearch}
              prefix={<SearchOutlined />}
              value={filters.keyword}
              onChange={(e) => setFilters((prev) => ({ ...prev, keyword: e.target.value }))}
            />
          </Col>
          <Col xs={24} sm={12} md={8}>
            <Select
              placeholder="文件类型"
              allowClear
              style={{ width: '100%' }}
              options={fileTypeOptions}
              value={filters.fileType || undefined}
              onChange={handleTypeChange}
            />
          </Col>
        </Row>
      </Card>

      <Card className={styles.tableCard}>
        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={files}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`
          }}
          scroll={{ x: 1000 }}
          onChange={(p) =>
            setPagination((prev) => ({
              ...prev,
              current: p.current,
              pageSize: p.pageSize
            }))
          }
        />
      </Card>

      <Modal
        title="上传文件"
        open={uploadModalVisible}
        onCancel={() => setUploadModalVisible(false)}
        width={600}
        footer={null}
        destroyOnClose
      >
        <FileUploader onSuccess={handleUploadSuccess} />
      </Modal>

      <Modal
        title={previewFileData?.name}
        open={previewModalVisible}
        onCancel={() => setPreviewModalVisible(false)}
        width={800}
        footer={[
          <Button
            key="download"
            icon={<DownloadOutlined />}
            onClick={() => handleDownload(previewFileData)}
          >
            下载
          </Button>,
          <Button key="close" onClick={() => setPreviewModalVisible(false)}>
            关闭
          </Button>
        ]}
        destroyOnClose
      >
        {renderPreviewContent()}
      </Modal>
    </div>
  )
}

export default FileManager
