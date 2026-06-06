import { Modal, Form, DatePicker, InputNumber, Input, message } from 'antd';
import { useState } from 'react';
import dayjs from 'dayjs';
import request from '@/utils/request';

const { TextArea } = Input;

const WorklogModal = ({ open, onCancel, onSuccess, taskId, initialValues }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const data = {
        ...values,
        taskId,
        workDate: values.workDate.format('YYYY-MM-DD'),
        hours: values.hours,
      };

      if (initialValues?.id) {
        data.id = initialValues.id;
        await request({ url: '/task/worklog', method: 'PUT', data });
        message.success('工时更新成功');
      } else {
        await request({ url: '/task/worklog', method: 'POST', data });
        message.success('工时填报成功');
      }

      onSuccess?.();
      handleCancel();
    } catch (error) {
      if (error?.errorFields) {
        return;
      }
      console.error('保存工时失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    form.resetFields();
    onCancel?.();
  };

  return (
    <Modal
      title={initialValues?.id ? '编辑工时' : '填报工时'}
      open={open}
      onOk={handleOk}
      onCancel={handleCancel}
      confirmLoading={loading}
      destroyOnHidden
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          workDate: initialValues?.workDate ? dayjs(initialValues.workDate) : dayjs(),
          hours: initialValues?.hours || 1,
          description: initialValues?.description || '',
        }}
      >
        <Form.Item
          name="workDate"
          label="工作日期"
          rules={[{ required: true, message: '请选择工作日期' }]}
        >
          <DatePicker style={{ width: '100%' }} disabledDate={(d) => d && d.isAfter(dayjs())} />
        </Form.Item>

        <Form.Item
          name="hours"
          label="工时（小时）"
          rules={[
            { required: true, message: '请填写工时' },
            { type: 'number', min: 0.1, message: '工时最小为0.1小时' },
            { type: 'number', max: 24, message: '工时最大为24小时' },
          ]}
        >
          <InputNumber style={{ width: '100%' }} min={0.1} max={24} step={0.5} precision={1} />
        </Form.Item>

        <Form.Item
          name="description"
          label="工作描述"
          rules={[{ max: 500, message: '描述不能超过500字' }]}
        >
          <TextArea rows={4} placeholder="请输入工作内容描述..." maxLength={500} showCount />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default WorklogModal;
