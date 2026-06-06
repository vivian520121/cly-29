import { Modal, Image, List, Card, Empty, Tag, Button, message } from 'antd';
import {
  FileOutlined,
  FileImageOutlined,
  FilePdfOutlined,
  FileTextOutlined,
  FileExcelOutlined,
  FilePptOutlined,
  FileZipOutlined,
  DownloadOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { useState } from 'react';
import dayjs from 'dayjs';

const fileTypeMap = {
  image: { icon: <FileImageOutlined />, color: '#1890ff', extensions: ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'] },
  pdf: { icon: <FilePdfOutlined />, color: '#f5222d', extensions: ['pdf'] },
  word: { icon: <FileTextOutlined />, color: '#1890ff', extensions: ['doc', 'docx'] },
  excel: { icon: <FileExcelOutlined />, color: '#52c41a', extensions: ['xls', 'xlsx'] },
  ppt: { icon: <FilePptOutlined />, color: '#fa8c16', extensions: ['ppt', 'pptx'] },
  zip: { icon: <FileZipOutlined />, color: '#fa8c16', extensions: ['zip', 'rar', '7z'] },
  other: { icon: <FileOutlined />, color: '#8c8c8c', extensions: [] },
};

const getFileType = (fileName) => {
  const ext = fileName.split('.').pop()?.toLowerCase();
  for (const [type, config] of Object.entries(fileTypeMap)) {
    if (config.extensions.includes(ext)) {
      return { type, ...config };
    }
  }
  return { type: 'other', ...fileTypeMap.other };
};

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const FilePreview = ({ files = [], onDelete, canDelete = false }) => {
  const [previewImage, setPreviewImage] = useState(null);
  const [previewPdf, setPreviewPdf] = useState(null);

  const handlePreview = (file) => {
    const fileType = getFileType(file.fileName || file.originalName);
    if (fileType.type === 'image') {
      setPreviewImage(file.fileUrl || file.url);
    } else if (fileType.type === 'pdf') {
      setPreviewPdf(file.fileUrl || file.url);
    } else {
      message.info('该文件类型暂不支持在线预览，请下载后查看');
    }
  };

  const handleDownload = (file) => {
    const url = file.fileUrl || file.url;
    if (url) {
      window.open(url, '_blank');
    }
  };

  const handleDelete = (file) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该文件吗？',
      onOk: () => onDelete?.(file),
    });
  };

  const renderFileItem = (file) => {
    const fileType = getFileType(file.fileName || file.originalName);
    const url = file.fileUrl || file.url;
    const name = file.originalName || file.fileName;

    return (
      <Card
        key={file.id}
        hoverable
        className="card-hover"
        bodyStyle={{ padding: 12 }}
        actions={[
          <Button type="text" icon={<EyeOutlined />} onClick={() => handlePreview(file)}>
            预览
          </Button>,
          <Button type="text" icon={<DownloadOutlined />} onClick={() => handleDownload(file)}>
            下载
          </Button>,
          canDelete && (
            <Button type="text" danger onClick={() => handleDelete(file)}>
              删除
            </Button>
          ),
        ].filter(Boolean)}
      >
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
          {fileType.type === 'image' && url ? (
            <Image
              width={60}
              height={60}
              src={url}
              preview={false}
              style={{ objectFit: 'cover', borderRadius: 4, cursor: 'pointer' }}
              onClick={() => handlePreview(file)}
            />
          ) : (
            <div
              style={{
                width: 60,
                height: 60,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: '#f5f5f5',
                borderRadius: 4,
                fontSize: 28,
                color: fileType.color,
              }}
            >
              {fileType.icon}
            </div>
          )}
          <div style={{ flex: 1, minWidth: 0 }}>
            <div className="text-ellipsis" style={{ fontWeight: 500, marginBottom: 4 }}>
              {name}
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 12, color: '#8c8c8c' }}>
              <Tag color={fileType.color} style={{ margin: 0 }}>
                {fileType.type.toUpperCase()}
              </Tag>
              <span>{formatFileSize(file.fileSize)}</span>
            </div>
            {file.createTime && (
              <div style={{ fontSize: 12, color: '#8c8c8c', marginTop: 4 }}>
                {dayjs(file.createTime).format('YYYY-MM-DD HH:mm')}
              </div>
            )}
            {file.uploadUserName && (
              <div style={{ fontSize: 12, color: '#8c8c8c' }}>
                上传者：{file.uploadUserName}
              </div>
            )}
          </div>
        </div>
      </Card>
    );
  };

  if (!files || files.length === 0) {
    return <Empty description="暂无文件" image={Empty.PRESENTED_IMAGE_SIMPLE} />;
  }

  return (
    <>
      <List
        grid={{ gutter: 16, xs: 1, sm: 1, md: 2, lg: 2, xl: 3 }}
        dataSource={files}
        renderItem={renderFileItem}
      />

      <Image.PreviewGroup>
        <Image
          style={{ display: 'none' }}
          src={previewImage}
          preview={{
            visible: !!previewImage,
            onVisibleChange: (visible) => !visible && setPreviewImage(null),
          }}
        />
      </Image.PreviewGroup>

      <Modal
        title="PDF预览"
        open={!!previewPdf}
        onCancel={() => setPreviewPdf(null)}
        footer={null}
        width={1000}
        destroyOnHidden
      >
        {previewPdf && (
          <iframe
            src={previewPdf}
            style={{ width: '100%', height: 600, border: 'none' }}
            title="PDF Preview"
          />
        )}
      </Modal>
    </>
  );
};

export default FilePreview;
