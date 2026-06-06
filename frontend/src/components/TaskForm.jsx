import { Modal, Form, Input, Select, DatePicker, InputNumber, message, Row, Col } from 'antd';
import { useState, useEffect } from 'react';
import dayjs from 'dayjs';
import request from '@/utils/request';
import MemberSelect from './MemberSelect';
import RichEditor from './RichEditor';

const { Option } = Select;
const { TextArea } = Input;

const taskTypeOptions = [
  { value: 1, label: '需求' },
  { value: 2, label: '开发' },
  { value: 3, label: '测试' },
  { value: 4, label: 'Bug' },
  { value: 5, label: '优化' },
];

const taskStatusOptions = [
  { value: 1, label: '待办' },
  { value: 2, label: '进行中' },
  { value: 3, label: '审核中' },
  { value: 4, label: '已完成' },
  { value: 5, label: '已取消' },
];

const taskPriorityOptions = [
  { value: 1, label: '紧急' },
  { value: 2, label: '高' },
  { value: 3, label: '中' },
  { value: 4, label: '低' },
];

const TaskForm = ({ open, onCancel, onSuccess, projectId, initialValues, parentId }) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [phases, setPhases] = useState([]);
  const [tags, setTags] = useState([]);

  useEffect(() => {
    if (projectId) {
      fetchPhases();
      fetchTags();
    }
    if (initialValues) {
      form.setFieldsValue({
        ...initialValues,
        startDate: initialValues.startDate ? dayjs(initialValues.startDate) : null,
        endDate: initialValues.endDate ? dayjs(initialValues.endDate) : null,
        assigneeId: initialValues.assigneeId ? [initialValues.assigneeId] : [],
      });
    } else {
      form.resetFields();
      form.setFieldsValue({
        projectId,
        parentId,
        status: 1,
        priority: 3,
        taskType: 2,
        progress: 0,
      });
    }
  }, [projectId, initialValues, open]);

  const fetchPhases = async () => {
    try {
      const data = await request({ url: '/project/phase/list', method: 'GET', params: { projectId } });
      setPhases(Array.isArray(data) ? data : data?.records || []);
    } catch (error) {
      console.error('获取阶段列表失败:', error);
    }
  };

  const fetchTags = async () => {
    try {
      const data = await request({ url: '/task/tag/list', method: 'GET', params: { projectId } });
      setTags(Array.isArray(data) ? data : data?.records || []);
    } catch (error) {
      console.error('获取标签列表失败:', error);
    }
  };

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const data = {
        ...values,
        projectId,
        parentId: values.parentId || parentId,
        startDate: values.startDate ? values.startDate.format('YYYY-MM-DD') : null,
        endDate: values.endDate ? values.endDate.format('YYYY-MM-DD') : null,
        assigneeId: values.assigneeId?.[0] || null,
      };

      if (initialValues?.id) {
        data.id = initialValues.id;
        await request({ url: '/task', method: 'PUT', data });
        message.success('任务更新成功');
      } else {
        await request({ url: '/task', method: 'POST', data });
        message.success('任务创建成功');
      }

      onSuccess?.();
      handleCancel();
    } catch (error) {
      if (error?.errorFields) {
        return;
      }
      console.error('保存任务失败:', error);
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
      title={initialValues?.id ? '编辑任务' : '新建任务'}
      open={open}
      onOk={handleOk}
      onCancel={handleCancel}
      confirmLoading={loading}
      width={800}
      destroyOnHidden
    >
      <Form form={form} layout="vertical">
        <Row gutter={16}>
          <Col span={16}>
            <Form.Item
              name="taskName"
              label="任务名称"
              rules={[{ required: true, message: '请输入任务名称' }, { max: 100, message: '任务名称不能超过100字符' }]}
            >
              <Input placeholder="请输入任务名称" />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item
              name="taskType"
              label="任务类型"
              rules={[{ required: true, message: '请选择任务类型' }]}
            >
              <Select placeholder="请选择任务类型">
                {taskTypeOptions.map((opt) => (
                  <Option key={opt.value} value={opt.value}>{opt.label}</Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={16}>
          <Col span={8}>
            <Form.Item
              name="status"
              label="任务状态"
              rules={[{ required: true, message: '请选择任务状态' }]}
            >
              <Select placeholder="请选择任务状态">
                {taskStatusOptions.map((opt) => (
                  <Option key={opt.value} value={opt.value}>{opt.label}</Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item
              name="priority"
              label="优先级"
              rules={[{ required: true, message: '请选择优先级' }]}
            >
              <Select placeholder="请选择优先级">
                {taskPriorityOptions.map((opt) => (
                  <Option key={opt.value} value={opt.value}>{opt.label}</Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item
              name="progress"
              label="进度(%)"
              rules={[{ type: 'number', min: 0, max: 100, message: '进度必须在0-100之间' }]}
            >
              <InputNumber style={{ width: '100%' }} min={0} max={100} />
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item name="phaseId" label="所属阶段">
              <Select placeholder="请选择阶段" allowClear>
                {phases.map((phase) => (
                  <Option key={phase.id} value={phase.id}>{phase.phaseName}</Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="assigneeId" label="负责人">
              <MemberSelect mode="multiple" maxTagCount={1} projectId={projectId} />
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item name="startDate" label="开始日期">
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item
              name="endDate"
              label="结束日期"
              dependencies={['startDate']}
              rules={[
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || !getFieldValue('startDate') || value.isAfter(getFieldValue('startDate'))) {
                      return Promise.resolve();
                    }
                    return Promise.reject(new Error('结束日期必须大于开始日期'));
                  },
                }),
              ]}
            >
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item
              name="estimateHours"
              label="预估工时(小时)"
              rules={[{ type: 'number', min: 0, message: '预估工时不能为负数' }]}
            >
              <InputNumber style={{ width: '100%' }} min={0} step={0.5} precision={1} />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item name="tagIds" label="任务标签">
              <Select mode="multiple" placeholder="请选择标签" allowClear>
                {tags.map((tag) => (
                  <Option key={tag.id} value={tag.id}>
                    <span
                      style={{
                        display: 'inline-block',
                        width: 8,
                        height: 8,
                        borderRadius: '50%',
                        background: tag.tagColor,
                        marginRight: 8,
                      }}
                    />
                    {tag.tagName}
                  </Option>
                ))}
              </Select>
            </Form.Item>
          </Col>
        </Row>

        <Form.Item name="description" label="任务描述">
          <RichEditor />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default TaskForm;
