import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import { message } from 'antd';
import { useState, useRef } from 'react';
import request from '@/utils/request';

const RichEditor = ({ value, onChange, placeholder = '请输入内容...', readOnly = false }) => {
  const quillRef = useRef(null);
  const [uploading, setUploading] = useState(false);

  const imageHandler = () => {
    const input = document.createElement('input');
    input.setAttribute('type', 'file');
    input.setAttribute('accept', 'image/*');
    input.click();

    input.onchange = async () => {
      const file = input.files?.[0];
      if (!file) return;

      if (file.size > 5 * 1024 * 1024) {
        message.error('图片大小不能超过5MB');
        return;
      }

      setUploading(true);
      try {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('businessType', 'task');

        const data = await request({
          url: '/file/upload',
          method: 'POST',
          data: formData,
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        });

        const quill = quillRef.current?.getEditor();
        if (quill) {
          const range = quill.getSelection(true);
          quill.insertEmbed(range.index, 'image', data.fileUrl || data.url);
          quill.setSelection(range.index + 1);
        }
        message.success('图片上传成功');
      } catch (error) {
        console.error('图片上传失败:', error);
        message.error('图片上传失败');
      } finally {
        setUploading(false);
      }
    };
  };

  const modules = {
    toolbar: {
      container: [
        [{ header: [1, 2, 3, false] }],
        ['bold', 'italic', 'underline', 'strike'],
        [{ list: 'ordered' }, { list: 'bullet' }],
        [{ indent: '-1' }, { indent: '+1' }],
        [{ align: [] }],
        [{ color: [] }, { background: [] }],
        ['link', 'image'],
        ['clean'],
      ],
      handlers: {
        image: imageHandler,
      },
    },
  };

  const formats = [
    'header',
    'bold',
    'italic',
    'underline',
    'strike',
    'list',
    'bullet',
    'indent',
    'align',
    'color',
    'background',
    'link',
    'image',
  ];

  return (
    <div style={{ position: 'relative' }}>
      <ReactQuill
        ref={quillRef}
        theme="snow"
        value={value || ''}
        onChange={onChange}
        modules={readOnly ? { toolbar: false } : modules}
        formats={formats}
        placeholder={placeholder}
        readOnly={readOnly}
        style={{ background: '#fff' }}
      />
      {uploading && (
        <div
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(255,255,255,0.8)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 10,
          }}
        >
          <span>图片上传中...</span>
        </div>
      )}
    </div>
  );
};

export default RichEditor;
