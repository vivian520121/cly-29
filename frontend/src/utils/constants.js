import {
  DashboardOutlined,
  ProjectOutlined,
  CalendarOutlined,
  FolderOpenOutlined,
  TeamOutlined,
  SettingOutlined,
  UserOutlined,
} from '@ant-design/icons';

export const TASK_STATUS = [
  { code: 'TODO', name: '待办', color: '#91d5ff' },
  { code: 'IN_PROGRESS', name: '进行中', color: '#ffd666' },
  { code: 'REVIEW', name: '评审中', color: '#b37feb' },
  { code: 'DONE', name: '已完成', color: '#95de64' },
  { code: 'CLOSED', name: '已关闭', color: '#8c8c8c' },
];

export const TASK_PRIORITY = [
  { code: 'LOW', name: '低', color: '#52c41a' },
  { code: 'MEDIUM', name: '中', color: '#faad14' },
  { code: 'HIGH', name: '高', color: '#f5222d' },
  { code: 'URGENT', name: '紧急', color: '#cf1322' },
];

export const TASK_TYPE = [
  { code: 'TASK', name: '任务', color: '#1890ff' },
  { code: 'BUG', name: '缺陷', color: '#f5222d' },
  { code: 'FEATURE', name: '需求', color: '#722ed1' },
  { code: 'IMPROVEMENT', name: '改进', color: '#13c2c2' },
  { code: 'DOC', name: '文档', color: '#fa8c16' },
];

export const PROJECT_STATUS = [
  { code: 'DRAFT', name: '草稿' },
  { code: 'IN_PROGRESS', name: '进行中' },
  { code: 'SUSPENDED', name: '已暂停' },
  { code: 'COMPLETED', name: '已完成' },
  { code: 'ARCHIVED', name: '已归档' },
];

export const PROJECT_ROLE = [
  { code: 'OWNER', name: '项目负责人' },
  { code: 'ADMIN', name: '管理员' },
  { code: 'MEMBER', name: '成员' },
  { code: 'VIEWER', name: '查看者' },
];

export const MENU_ITEMS = [
  {
    key: '/dashboard',
    icon: DashboardOutlined,
    label: '工作台',
  },
  {
    key: '/projects',
    icon: ProjectOutlined,
    label: '项目管理',
  },
  {
    key: '/tasks',
    icon: CalendarOutlined,
    label: '任务中心',
  },
  {
    key: '/files',
    icon: FolderOpenOutlined,
    label: '文件管理',
  },
  {
    key: '/organization',
    icon: TeamOutlined,
    label: '组织架构',
    children: [
      { key: '/organization/dept', label: '部门管理' },
      { key: '/organization/user', label: '用户管理' },
    ],
  },
  {
    key: '/profile',
    icon: UserOutlined,
    label: '个人中心',
  },
  {
    key: '/settings',
    icon: SettingOutlined,
    label: '系统设置',
  },
];
