import { Select, Avatar, Spin, Empty } from 'antd';
import { useState, useEffect } from 'react';
import { UserOutlined } from '@ant-design/icons';
import request from '@/utils/request';
import useDebounce from '@/hooks/useDebounce';

const { Option } = Select;

const MemberSelect = ({
  value,
  onChange,
  mode = 'multiple',
  placeholder = '请选择成员',
  projectId,
  disabled = false,
}) => {
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const debouncedSearch = useDebounce(searchText, 300);

  useEffect(() => {
    fetchMembers();
  }, [projectId, debouncedSearch]);

  const fetchMembers = async () => {
    setLoading(true);
    try {
      const params = { keyword: debouncedSearch };
      if (projectId) {
        params.projectId = projectId;
      }
      const data = await request({
        url: projectId ? '/project/member/list' : '/user/list',
        method: 'GET',
        params,
      });
      setMembers(Array.isArray(data) ? data : data?.records || []);
    } catch (error) {
      console.error('获取成员列表失败:', error);
      setMembers([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (value) => {
    setSearchText(value);
  };

  const renderOption = (member) => {
    const id = member.id || member.userId;
    const name = member.realName || member.nickname || member.username;
    const avatar = member.avatar;

    return (
      <Option key={id} value={id} label={name}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <Avatar size="small" src={avatar} icon={<UserOutlined />} />
          <span>{name}</span>
        </div>
      </Option>
    );
  };

  const renderTag = (props) => {
    const { label, value, closable, onClose } = props;
    const member = members.find((m) => (m.id || m.userId) === value);

    return (
      <div
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: 4,
          padding: '0 4px 0 2px',
          background: '#f0f0f0',
          borderRadius: 4,
          marginRight: 4,
          marginBottom: 4,
        }}
      >
        <Avatar size={16} src={member?.avatar} icon={<UserOutlined style={{ fontSize: 10 }} />} />
        <span style={{ fontSize: 12 }}>{label}</span>
        {closable && (
          <span
            style={{ cursor: 'pointer', fontSize: 12, color: '#8c8c8c' }}
            onClick={onClose}
          >
            ×
          </span>
        )}
      </div>
    );
  };

  return (
    <Select
      mode={mode}
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      showSearch
      onSearch={handleSearch}
      filterOption={false}
      disabled={disabled}
      tagRender={mode === 'multiple' ? renderTag : undefined}
      notFoundContent={loading ? <Spin size="small" /> : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无成员" />}
      style={{ width: '100%' }}
      optionLabelProp="label"
    >
      {members.map(renderOption)}
    </Select>
  );
};

export default MemberSelect;
