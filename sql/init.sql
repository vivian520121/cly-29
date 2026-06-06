SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS cly_project DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cly_project;

DROP TABLE IF EXISTS `sys_company`;
CREATE TABLE `sys_company` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_name` varchar(100) NOT NULL COMMENT '企业名称',
  `company_code` varchar(50) NOT NULL COMMENT '企业编码',
  `legal_person` varchar(50) DEFAULT NULL COMMENT '法人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `logo_url` varchar(255) DEFAULT NULL COMMENT 'Logo地址',
  `description` varchar(500) DEFAULT NULL COMMENT '企业描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1启用 0禁用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_code` (`company_code`),
  KEY `idx_company_name` (`company_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业表';

DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '企业ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID',
  `dept_name` varchar(100) NOT NULL COMMENT '部门名称',
  `dept_code` varchar(50) NOT NULL COMMENT '部门编码',
  `dept_type` tinyint DEFAULT '1' COMMENT '部门类型 1公司 2部门 3小组',
  `leader_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1启用 0禁用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `tree_path` varchar(500) DEFAULT NULL COMMENT '树形路径',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_dept_name` (`dept_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `gender` tinyint DEFAULT '0' COMMENT '性别 0未知 1男 2女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `user_type` tinyint NOT NULL DEFAULT '1' COMMENT '用户类型 1管理员 2普通用户',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1启用 0禁用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_real_name` (`real_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

DROP TABLE IF EXISTS `sys_user_dept`;
CREATE TABLE `sys_user_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `is_main` tinyint NOT NULL DEFAULT '0' COMMENT '是否主部门 1是 0否',
  `position` varchar(50) DEFAULT NULL COMMENT '职位',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_dept` (`user_id`,`dept_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户部门关联表';

DROP TABLE IF EXISTS `pm_project`;
CREATE TABLE `pm_project` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_id` bigint NOT NULL COMMENT '企业ID',
  `project_name` varchar(100) NOT NULL COMMENT '项目名称',
  `project_code` varchar(50) NOT NULL COMMENT '项目编码',
  `project_type` tinyint DEFAULT '1' COMMENT '项目类型 1内部项目 2外部项目',
  `description` text COMMENT '项目描述',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `actual_start_date` date DEFAULT NULL COMMENT '实际开始日期',
  `actual_end_date` date DEFAULT NULL COMMENT '实际结束日期',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1未开始 2进行中 3已暂停 4已完成 5已取消',
  `progress` int DEFAULT '0' COMMENT '进度 0-100',
  `priority` tinyint DEFAULT '2' COMMENT '优先级 1高 2中 3低',
  `manager_id` bigint DEFAULT NULL COMMENT '项目经理ID',
  `budget` decimal(15,2) DEFAULT NULL COMMENT '预算',
  `color` varchar(20) DEFAULT '#1890ff' COMMENT '项目颜色',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_code` (`project_code`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_status` (`status`),
  KEY `idx_manager_id` (`manager_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

DROP TABLE IF EXISTS `pm_project_phase`;
CREATE TABLE `pm_project_phase` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `phase_name` varchar(100) NOT NULL COMMENT '阶段名称',
  `description` varchar(500) DEFAULT NULL COMMENT '阶段描述',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1未开始 2进行中 3已完成',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目阶段表';

DROP TABLE IF EXISTS `pm_milestone`;
CREATE TABLE `pm_milestone` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `milestone_name` varchar(100) NOT NULL COMMENT '里程碑名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `plan_date` date NOT NULL COMMENT '计划日期',
  `actual_date` date DEFAULT NULL COMMENT '实际完成日期',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1未开始 2进行中 3已完成 4已延期',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_plan_date` (`plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='里程碑表';

DROP TABLE IF EXISTS `pm_project_member`;
CREATE TABLE `pm_project_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` tinyint NOT NULL DEFAULT '3' COMMENT '角色 1管理员 2项目经理 3成员 4查看者',
  `join_time` datetime DEFAULT NULL COMMENT '加入时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`,`user_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';

DROP TABLE IF EXISTS `pm_task`;
CREATE TABLE `pm_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `phase_id` bigint DEFAULT NULL COMMENT '阶段ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父任务ID',
  `task_name` varchar(200) NOT NULL COMMENT '任务名称',
  `task_no` varchar(50) NOT NULL COMMENT '任务编号',
  `description` text COMMENT '任务描述',
  `task_type` tinyint DEFAULT '1' COMMENT '任务类型 1需求 2缺陷 3优化 4其他',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1待办 2进行中 3审核中 4已完成 5已取消',
  `priority` tinyint NOT NULL DEFAULT '2' COMMENT '优先级 1紧急 2高 3中 4低',
  `assignee_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '截止日期',
  `actual_start_date` datetime DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_date` datetime DEFAULT NULL COMMENT '实际结束时间',
  `estimate_hours` decimal(8,2) DEFAULT '0.00' COMMENT '预估工时(小时)',
  `actual_hours` decimal(8,2) DEFAULT '0.00' COMMENT '实际工时(小时)',
  `progress` int DEFAULT '0' COMMENT '进度 0-100',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_phase_id` (`phase_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_assignee_id` (`assignee_id`),
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

DROP TABLE IF EXISTS `pm_task_tag`;
CREATE TABLE `pm_task_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
  `tag_color` varchar(20) DEFAULT '#1890ff' COMMENT '标签颜色',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务标签表';

DROP TABLE IF EXISTS `pm_task_tag_rel`;
CREATE TABLE `pm_task_tag_rel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_tag` (`task_id`,`tag_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务标签关联表';

DROP TABLE IF EXISTS `pm_task_worklog`;
CREATE TABLE `pm_task_worklog` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `work_date` date NOT NULL COMMENT '工作日期',
  `hours` decimal(4,1) NOT NULL COMMENT '工时(小时)',
  `description` varchar(500) DEFAULT NULL COMMENT '工作描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_work_date` (`work_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工时表';

DROP TABLE IF EXISTS `pm_task_log`;
CREATE TABLE `pm_task_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `user_id` bigint NOT NULL COMMENT '操作人ID',
  `action_type` varchar(50) NOT NULL COMMENT '操作类型 CREATE/UPDATE/STATUS_CHANGE/ASSIGNEE_CHANGE等',
  `field_name` varchar(50) DEFAULT NULL COMMENT '变更字段',
  `old_value` text COMMENT '旧值',
  `new_value` text COMMENT '新值',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务流转日志表';

DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_url` varchar(500) DEFAULT NULL COMMENT '访问URL',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_ext` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `md5` varchar(50) DEFAULT NULL COMMENT '文件MD5',
  `upload_id` varchar(100) DEFAULT NULL COMMENT '分片上传ID',
  `chunk_size` bigint DEFAULT NULL COMMENT '分片大小',
  `total_chunks` int DEFAULT NULL COMMENT '总分片数',
  `chunk_index` int DEFAULT NULL COMMENT '当前分片索引',
  `is_completed` tinyint DEFAULT '1' COMMENT '是否上传完成 1是 0否',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
  `business_id` bigint DEFAULT NULL COMMENT '业务ID',
  `upload_user_id` bigint DEFAULT NULL COMMENT '上传人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_upload_id` (`upload_id`),
  KEY `idx_md5` (`md5`),
  KEY `idx_business` (`business_type`,`business_id`),
  KEY `idx_upload_user` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件表';

DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `module` varchar(50) DEFAULT NULL COMMENT '模块',
  `operation` varchar(100) DEFAULT NULL COMMENT '操作',
  `business_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
  `business_id` bigint DEFAULT NULL COMMENT '业务ID',
  `method` varchar(200) DEFAULT NULL COMMENT '方法名',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方式',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `request_param` text COMMENT '请求参数',
  `response_result` text COMMENT '响应结果',
  `user_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `username` varchar(50) DEFAULT NULL COMMENT '操作人用户名',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `location` varchar(255) DEFAULT NULL COMMENT '操作地点',
  `os` varchar(100) DEFAULT NULL COMMENT '操作系统',
  `browser` varchar(100) DEFAULT NULL COMMENT '浏览器',
  `cost_time` bigint DEFAULT NULL COMMENT '耗时(毫秒)',
  `status` tinyint DEFAULT '1' COMMENT '状态 1成功 0失败',
  `error_msg` varchar(2000) DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_business` (`business_type`,`business_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

INSERT INTO `sys_company` (`id`, `company_name`, `company_code`, `legal_person`, `contact_phone`, `address`, `description`, `status`, `sort_order`) VALUES
(1, '飞鼠科技有限公司', 'FS_TECH', '张三', '13800138000', '北京市朝阳区科技园', '企业级项目协作管理平台开发公司', 1, 1);

INSERT INTO `sys_dept` (`id`, `company_id`, `parent_id`, `dept_name`, `dept_code`, `dept_type`, `description`, `status`, `sort_order`, `tree_path`) VALUES
(1, 1, 0, '总公司', 'HEAD_OFFICE', 1, '飞鼠科技总公司', 1, 1, '0,1'),
(2, 1, 1, '技术部', 'TECH_DEPT', 2, '技术研发部门', 1, 1, '0,1,2'),
(3, 1, 1, '产品部', 'PRODUCT_DEPT', 2, '产品设计部门', 1, 2, '0,1,3'),
(4, 1, 1, '运营部', 'OPER_DEPT', 2, '运营部门', 1, 3, '0,1,4'),
(5, 1, 2, '前端组', 'FRONTEND_TEAM', 3, '前端开发组', 1, 1, '0,1,2,5'),
(6, 1, 2, '后端组', 'BACKEND_TEAM', 3, '后端开发组', 1, 2, '0,1,2,6'),
(7, 1, 2, '测试组', 'TEST_TEAM', 3, '测试组', 1, 3, '0,1,2,7');

INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `real_name`, `email`, `phone`, `gender`, `user_type`, `status`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', '系统管理员', 'admin@cly.com', '13800138001', 1, 1, 1),
(2, 'user', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '普通用户', '张三', 'user@cly.com', '13800138002', 1, 2, 1),
(3, 'manager', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '项目经理', '李四', 'manager@cly.com', '13800138003', 1, 2, 1),
(4, 'developer', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '开发人员', '王五', 'dev@cly.com', '13800138004', 1, 2, 1);

INSERT INTO `sys_user_dept` (`user_id`, `dept_id`, `is_main`, `position`) VALUES
(1, 2, 1, '技术总监'),
(2, 5, 1, '前端工程师'),
(2, 6, 0, '后端工程师'),
(3, 2, 1, '项目经理'),
(4, 6, 1, '后端工程师'),
(4, 7, 0, '测试工程师');

INSERT INTO `pm_project` (`id`, `company_id`, `project_name`, `project_code`, `project_type`, `description`, `start_date`, `end_date`, `status`, `progress`, `priority`, `manager_id`, `color`) VALUES
(1, 1, '项目协作管理平台', 'PROJ_001', 1, '企业级项目协作管理平台，包含组织架构、项目管理、任务管理、文件管理等功能。', '2024-01-01', '2024-06-30', 2, 65, 1, 3, '#1890ff'),
(2, 1, '移动端APP开发', 'PROJ_002', 1, '企业移动办公APP，支持iOS和Android双平台。', '2024-03-01', '2024-09-30', 2, 30, 2, 3, '#52c41a'),
(3, 1, '官网重构项目', 'PROJ_003', 2, '企业官网重构，提升用户体验和品牌形象。', '2024-02-01', '2024-04-30', 4, 100, 3, 3, '#722ed1');

INSERT INTO `pm_project_phase` (`id`, `project_id`, `phase_name`, `description`, `start_date`, `end_date`, `status`, `sort_order`) VALUES
(1, 1, '需求分析', '需求调研与分析', '2024-01-01', '2024-01-31', 3, 1),
(2, 1, '系统设计', '系统架构设计与详细设计', '2024-02-01', '2024-02-29', 3, 2),
(3, 1, '开发阶段', '编码开发与单元测试', '2024-03-01', '2024-05-31', 2, 3),
(4, 1, '测试阶段', '系统测试与UAT', '2024-06-01', '2024-06-20', 1, 4),
(5, 1, '上线部署', '生产环境部署', '2024-06-21', '2024-06-30', 1, 5);

INSERT INTO `pm_milestone` (`id`, `project_id`, `milestone_name`, `description`, `plan_date`, `status`, `sort_order`) VALUES
(1, 1, '需求评审完成', '完成所有需求评审并确认', '2024-01-25', 3, 1),
(2, 1, '设计评审完成', '完成系统设计评审', '2024-02-25', 3, 2),
(3, 1, '核心功能开发完成', '完成核心模块开发', '2024-05-15', 2, 3),
(4, 1, '用户验收通过', 'UAT测试通过', '2024-06-25', 1, 4),
(5, 1, '正式上线', '生产环境正式上线', '2024-06-30', 1, 5);

INSERT INTO `pm_project_member` (`project_id`, `user_id`, `role`, `join_time`) VALUES
(1, 1, 1, '2024-01-01 00:00:00'),
(1, 3, 2, '2024-01-01 00:00:00'),
(1, 2, 3, '2024-01-01 00:00:00'),
(1, 4, 3, '2024-01-01 00:00:00');

INSERT INTO `pm_task` (`id`, `project_id`, `phase_id`, `parent_id`, `task_name`, `task_no`, `description`, `task_type`, `status`, `priority`, `assignee_id`, `creator_id`, `start_date`, `end_date`, `estimate_hours`, `progress`, `sort_order`) VALUES
(1, 1, 3, 0, '用户管理模块开发', 'TASK_001', '用户登录、注册、权限管理等功能', 1, 4, 1, 4, 1, '2024-03-01', '2024-03-15', 40.00, 100, 1),
(2, 1, 3, 0, '组织架构模块开发', 'TASK_002', '企业、部门、成员管理', 1, 2, 4, 1, '2024-03-10', '2024-03-25', 32.00, 75, 2),
(3, 1, 3, 2, '部门树状结构开发', 'TASK_003', '部门树状结构展示与拖拽', 1, 2, 2, 3, '2024-03-15', '2024-03-20', 16.00, 100, 1),
(4, 1, 3, 2, '成员多部门挂靠', 'TASK_004', '支持成员挂靠多个部门', 1, 1, 4, 3, '2024-03-18', '2024-03-25', 16.00, 50, 2),
(5, 1, 3, 0, '项目管理模块开发', 'TASK_005', '项目CRUD、阶段管理、里程碑管理', 1, 2, 3, 1, '2024-03-20', '2024-04-10', 48.00, 40, 3),
(6, 1, 3, 0, '任务看板开发', 'TASK_006', '看板视图、列表视图、拖拽功能', 1, 1, 2, 1, '2024-04-01', '2024-04-20', 40.00, 30, 4),
(7, 1, 3, 0, '甘特图开发', 'TASK_007', '项目进度甘特图展示', 2, 3, 2, 1, '2024-04-15', '2024-04-30', 24.00, 0, 5),
(8, 1, 4, 0, '系统性能测试', 'TASK_008', '系统压力测试、性能优化', 3, 1, 4, 1, '2024-06-01', '2024-06-10', 16.00, 0, 1);

INSERT INTO `pm_task_tag` (`id`, `project_id`, `tag_name`, `tag_color`) VALUES
(1, 1, '紧急', '#f5222d'),
(2, 1, '前端', '#1890ff'),
(3, 1, '后端', '#52c41a'),
(4, 1, 'Bug', '#fa8c16'),
(5, 1, '优化', '#722ed1');

INSERT INTO `pm_task_tag_rel` (`task_id`, `tag_id`) VALUES
(1, 3),
(2, 3),
(3, 2),
(4, 3),
(5, 3),
(6, 2),
(7, 2),
(8, 5);

INSERT INTO `pm_task_worklog` (`task_id`, `user_id`, `work_date`, `hours`, `description`) VALUES
(1, 4, '2024-03-01', 8.0, '数据库设计与实体类创建'),
(1, 4, '2024-03-02', 8.0, '登录接口开发'),
(1, 4, '2024-03-03', 8.0, '权限拦截器实现'),
(1, 4, '2024-03-04', 8.0, '用户CRUD接口开发'),
(1, 4, '2024-03-05', 8.0, '单元测试与调试'),
(3, 2, '2024-03-15', 8.0, '树形组件封装'),
(3, 2, '2024-03-16', 8.0, '拖拽功能实现');

INSERT INTO `pm_task_log` (`task_id`, `user_id`, `action_type`, `field_name`, `old_value`, `new_value`, `remark`) VALUES
(1, 1, 'CREATE', NULL, NULL, NULL, '创建任务'),
(1, 1, 'STATUS_CHANGE', 'status', '1', '2', '开始任务'),
(1, 1, 'ASSIGNEE_CHANGE', 'assignee_id', NULL, '4', '指派给王五'),
(1, 4, 'STATUS_CHANGE', 'status', '2', '3', '提交审核'),
(1, 1, 'STATUS_CHANGE', 'status', '3', '4', '审核通过，任务完成'),
(2, 1, 'CREATE', NULL, NULL, NULL, '创建任务'),
(2, 1, 'STATUS_CHANGE', 'status', '1', '2', '开始任务');

INSERT INTO `sys_file` (`id`, `file_name`, `original_name`, `file_path`, `file_url`, `file_size`, `file_type`, `file_ext`, `business_type`, `business_id`, `upload_user_id`) VALUES
(1, '20240101/abc123.pdf', '需求文档.pdf', '/uploads/20240101/abc123.pdf', '/uploads/20240101/abc123.pdf', 1048576, 'application/pdf', 'pdf', 'project', 1, 1),
(2, '20240102/def456.docx', '设计方案.docx', '/uploads/20240102/def456.docx', '/uploads/20240102/def456.docx', 2097152, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'docx', 'task', 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
