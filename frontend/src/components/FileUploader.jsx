import { useState, useRef } from 'react'
import { Upload, Progress, Button, message } from 'antd'
import { UploadOutlined, InboxOutlined } from '@ant-design/icons'
import { uploadFile, checkChunk, uploadChunk, mergeChunks } from '@/services/file'
import styles from './FileUploader.module.scss'

const { Dragger } = Upload

const CHUNK_SIZE = 5 * 1024 * 1024

const FileUploader = ({ onSuccess, accept, maxSize = 500, chunkThreshold = 100 }) => {
  const [uploading, setUploading] = useState(false)
  const [progress, setProgress] = useState(0)
  const [currentFile, setCurrentFile] = useState(null)
  const fileInputRef = useRef(null)

  const calculateMD5 = async (file) => {
    return new Promise((resolve) => {
      const reader = new FileReader()
      reader.onload = (e) => {
        const hash = simpleHash(e.target.result)
        resolve(hash)
      }
      reader.readAsArrayBuffer(file.slice(0, Math.min(file.size, 10 * 1024 * 1024)))
    })
  }

  const simpleHash = (buffer) => {
    let hash = 0
    const view = new Uint8Array(buffer)
    for (let i = 0; i < view.length; i++) {
      hash = ((hash << 5) - hash + view[i]) | 0
    }
    return Math.abs(hash).toString(16)
  }

  const handleNormalUpload = async (file) => {
    try {
      setCurrentFile(file)
      setProgress(0)
      const data = await uploadFile(file, (progressEvent) => {
        if (progressEvent.total) {
          const percent = Math.round((progressEvent.loaded / progressEvent.total) * 100)
          setProgress(percent)
        }
      })
      message.success('上传成功')
      onSuccess?.(data)
      return data
    } catch (error) {
      message.error('上传失败')
      throw error
    } finally {
      setUploading(false)
      setCurrentFile(null)
      setProgress(0)
    }
  }

  const handleChunkUpload = async (file) => {
    try {
      setCurrentFile(file)
      setProgress(0)

      const fileMd5 = await calculateMD5(file)
      const totalChunks = Math.ceil(file.size / CHUNK_SIZE)

      const uploadedChunks = []
      for (let i = 0; i < totalChunks; i++) {
        try {
          const checkData = await checkChunk({
            fileName: file.name,
            fileMd5,
            chunkIndex: i,
            totalChunks
          })
          if (checkData?.uploaded) {
            uploadedChunks.push(i)
          }
        } catch (e) {
          console.error('Check chunk error:', e)
        }
      }

      for (let i = 0; i < totalChunks; i++) {
        if (uploadedChunks.includes(i)) {
          const percent = Math.round(((i + 1) / totalChunks) * 100)
          setProgress(percent)
          continue
        }

        const start = i * CHUNK_SIZE
        const end = Math.min(start + CHUNK_SIZE, file.size)
        const chunk = file.slice(start, end)

        await uploadChunk(
          {
            chunk,
            chunkIndex: i,
            totalChunks,
            fileName: file.name,
            fileMd5
          },
          (progressEvent) => {
            if (progressEvent.total) {
              const chunkPercent = progressEvent.loaded / progressEvent.total
              const overallPercent = Math.round(
                ((i + chunkPercent) / totalChunks) * 100
              )
              setProgress(overallPercent)
            }
          }
        )
      }

      const data = await mergeChunks({
        fileName: file.name,
        fileMd5,
        totalChunks,
        fileSize: file.size
      })

      message.success('上传成功')
      onSuccess?.(data)
      return data
    } catch (error) {
      message.error('上传失败')
      throw error
    } finally {
      setUploading(false)
      setCurrentFile(null)
      setProgress(0)
    }
  }

  const beforeUpload = (file) => {
    const isLtMaxSize = file.size / 1024 / 1024 < maxSize
    if (!isLtMaxSize) {
      message.error(`文件不能超过 ${maxSize}MB`)
      return Upload.LIST_IGNORE
    }
    return true
  }

  const handleUpload = async (file) => {
    setUploading(true)
    const fileSizeMB = file.size / 1024 / 1024

    if (fileSizeMB > chunkThreshold) {
      await handleChunkUpload(file)
    } else {
      await handleNormalUpload(file)
    }
    return false
  }

  const uploadProps = {
    accept,
    multiple: false,
    showUploadList: false,
    beforeUpload,
    customRequest: ({ file }) => handleUpload(file)
  }

  return (
    <div className={styles.fileUploader}>
      <Dragger {...uploadProps} disabled={uploading}>
        <p className="ant-upload-drag-icon">
          <InboxOutlined />
        </p>
        <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
        <p className="ant-upload-hint">
          支持单个文件上传，{maxSize}MB以内，大于{chunkThreshold}MB将自动分片上传
        </p>
      </Dragger>

      {uploading && currentFile && (
        <div className={styles.uploadProgress}>
          <div className={styles.fileName}>{currentFile.name}</div>
          <Progress percent={progress} status="active" />
          <div className={styles.progressInfo}>
            {progress}% 已上传
          </div>
        </div>
      )}

      <div className={styles.uploadBtn}>
        <Upload {...uploadProps} disabled={uploading}>
          <Button icon={<UploadOutlined />} loading={uploading}>
            {uploading ? '上传中...' : '选择文件上传'}
          </Button>
        </Upload>
      </div>

      <input type="file" ref={fileInputRef} style={{ display: 'none' }} accept={accept} />
    </div>
  )
}

export default FileUploader
