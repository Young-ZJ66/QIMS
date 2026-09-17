CREATE DATABASE IF NOT EXISTS `qims_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `qims_db`;

-- ----------------------------
-- 质量检测系统 核心数据库表设计 v2.0
-- 数据库类型：MySQL 8.0+
-- 框架：Spring Boot + MyBatis (原生)
-- 密码策略：BCrypt
-- ----------------------------

-- 1. 系统账号表
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '密码(BCrypt)',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `role_id` int NOT NULL COMMENT '角色ID (1-管理员 2-检测员)',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `login_fail_count` int DEFAULT 0 COMMENT '连续登录失败次数',
  `lock_until` datetime DEFAULT NULL COMMENT '锁定截止时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1正常，0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人ID',
  `deleted_at` datetime DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统内部用户表';

-- 2. 客户企业表（送检方）
CREATE TABLE `sys_client` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `company_name` varchar(100) NOT NULL COMMENT '企业全称',
  `credit_code` varchar(50) DEFAULT NULL COMMENT '统一社会信用代码',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(200) DEFAULT NULL COMMENT '企业地址',
  `login_account` varchar(50) NOT NULL COMMENT '登录账号',
  `login_password` varchar(100) NOT NULL COMMENT '登录密码(BCrypt)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1正常，0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人ID',
  `deleted_at` datetime DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_client_account` (`login_account`),
  KEY `idx_status` (`status`),
  KEY `idx_company_name` (`company_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='送检客户表';

-- 3. 系统操作日志表
CREATE TABLE `sys_operate_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `delegation_id` bigint DEFAULT NULL COMMENT '关联的委托单ID',
  `operator` varchar(50) NOT NULL COMMENT '操作人姓名或角色',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `action` varchar(50) NOT NULL COMMENT '操作动作',
  `action_type` varchar(20) NOT NULL COMMENT '动作类型(primary,warning,info,success)',
  `description` varchar(255) NOT NULL COMMENT '详细描述',
  `ip_address` varchar(50) DEFAULT NULL COMMENT '操作人IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_delegation_id` (`delegation_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-- 4. 检验标准表
CREATE TABLE `std_standard` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标准ID',
  `standard_code` varchar(50) NOT NULL COMMENT '标准代号 (如: GB/T 19001-2016)',
  `standard_name` varchar(100) NOT NULL COMMENT '标准名称 (如: 质量管理体系要求)',
  `standard_category` varchar(20) NOT NULL DEFAULT '国家标准' COMMENT '标准类别(国家标准/行业标准/地方标准/团体标准/企业标准)',
  `product_category` varchar(50) NOT NULL COMMENT '适用产品大类 (如: 纺织品/食品/建材)',
  `issue_date` date DEFAULT NULL COMMENT '发布日期',
  `effective_date` date DEFAULT NULL COMMENT '实施日期',
  `issuing_authority` varchar(100) DEFAULT NULL COMMENT '发布机构',
  `description` text DEFAULT NULL COMMENT '标准描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：1现行，0废止',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人ID',
  `deleted_at` datetime DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_standard_code` (`standard_code`),
  KEY `idx_status` (`status`),
  KEY `idx_product_category` (`product_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验标准表';

-- 5. 检验项目指标表 (绑定具体标准，定义合格范围)
CREATE TABLE `std_inspection_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `standard_id` bigint NOT NULL COMMENT '关联的标准ID (外键 std_standard.id)',
  `item_name` varchar(100) NOT NULL COMMENT '检验项目名称 (如: 甲醛含量、pH值、抗拉强度)',
  `unit` varchar(50) DEFAULT NULL COMMENT '计量单位 (如: mg/kg, %, MPa)',

  -- 检测判定字段
  `judge_type` tinyint NOT NULL COMMENT '判定方式：1-数值范围，2-上限值，3-下限值，4-文本定性',
  `min_value` decimal(14,6) DEFAULT NULL COMMENT '下限值 (包含)',
  `max_value` decimal(14,6) DEFAULT NULL COMMENT '上限值 (包含)',
  `text_standard` varchar(200) DEFAULT NULL COMMENT '文本标准描述 (用于定性，如：无异味，表面光滑)',

  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` datetime DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_standard_id` (`standard_id`),
  CONSTRAINT `fk_item_standard` FOREIGN KEY (`standard_id`) REFERENCES `std_standard` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标准对应的检验项目及限值表';

-- 6. 检验委托单主表 (客户发起)
CREATE TABLE `biz_delegation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '委托单ID',
  `delegation_no` varchar(50) NOT NULL COMMENT '委托单号 (如: D202310240001)',
  `client_id` bigint NOT NULL COMMENT '送检企业ID (外键 sys_client.id)',
  `sample_name` varchar(100) NOT NULL COMMENT '样品名称',
  `sample_specs` varchar(100) DEFAULT NULL COMMENT '规格型号',
  `sample_quantity` int NOT NULL COMMENT '送样数量',
  `standard_id` bigint NOT NULL COMMENT '要求依据的检测标准ID (外键 std_standard.id)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-待收样，1-检测中，2-审核中，3-已出报告',
  `priority` tinyint DEFAULT 0 COMMENT '优先级：0-普通，1-加急',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
  `expected_finish_date` date DEFAULT NULL COMMENT '期望完成日期',
  `submit_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '委托提交时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人ID',
  `deleted_at` datetime DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_delegation_no` (`delegation_no`),
  KEY `idx_client_id` (`client_id`),
  KEY `idx_status` (`status`),
  KEY `idx_submit_time` (`submit_time`),
  KEY `idx_client_status` (`client_id`, `status`),
  CONSTRAINT `fk_delegation_client` FOREIGN KEY (`client_id`) REFERENCES `sys_client` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_delegation_standard` FOREIGN KEY (`standard_id`) REFERENCES `std_standard` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验委托单主表';

-- 7. 样品与检测任务表 (收样员操作后生成盲样)
CREATE TABLE `biz_sample_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务/样品ID',
  `delegation_id` bigint NOT NULL COMMENT '关联委托单ID (外键 biz_delegation.id)',
  `blind_sample_code` varchar(50) NOT NULL COMMENT '内部盲样编号 (供检测员看，如: SAM-1024-001)',
  `inspector_id` bigint DEFAULT NULL COMMENT '分配的检测员ID (外键 sys_user.id)',
  `receive_time` datetime DEFAULT NULL COMMENT '收样时间',
  `finish_time` datetime DEFAULT NULL COMMENT '检测完成时间',
  `receiver_id` bigint DEFAULT NULL COMMENT '收样人ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '检测状态：0-待检测，1-已检测',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` datetime DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blind_code` (`blind_sample_code`),
  KEY `idx_inspector_id` (`inspector_id`),
  KEY `idx_delegation_id` (`delegation_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_task_delegation` FOREIGN KEY (`delegation_id`) REFERENCES `biz_delegation` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_task_inspector` FOREIGN KEY (`inspector_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内部盲样与检测任务分配表';

-- 8. 检验记录明细表 (检测员录入，系统自动判定)
CREATE TABLE `biz_inspection_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `task_id` bigint NOT NULL COMMENT '关联的任务ID (外键 biz_sample_task.id)',
  `item_id` bigint NOT NULL COMMENT '检测项目ID (外键 std_inspection_item.id)',
  `measured_value` decimal(14,6) DEFAULT NULL COMMENT '实测数值',
  `measured_text` varchar(200) DEFAULT NULL COMMENT '实测文本描述',
  `photo_url` varchar(255) DEFAULT NULL COMMENT '现场照片路径',
  `result` tinyint(1) NOT NULL COMMENT '单项结论：1-合格，0-不合格 (系统根据录入值与标准对比自动得出)',
  `attachment_url` varchar(255) DEFAULT NULL COMMENT '附件图片地址 (如仪器屏幕截图)',
  `remarks` varchar(500) DEFAULT NULL COMMENT '检测员备注',
  `inspect_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` datetime DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_result` (`result`),
  CONSTRAINT `fk_record_task` FOREIGN KEY (`task_id`) REFERENCES `biz_sample_task` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_record_item` FOREIGN KEY (`item_id`) REFERENCES `std_inspection_item` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室单项检测数据记录表';

-- 9. 检测报告表 (审核员出具)
CREATE TABLE `biz_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报告ID',
  `report_no` varchar(50) NOT NULL COMMENT '报告编号 (防伪查询用，如: R202310240001)',
  `delegation_id` bigint NOT NULL COMMENT '关联的委托单ID',
  `reviewer_id` bigint NOT NULL COMMENT '审核/签发人ID',
  `final_conclusion` tinyint(1) NOT NULL COMMENT '最终综合判定：1-合格，0-不合格',
  `report_file_url` varchar(255) DEFAULT NULL COMMENT '生成的PDF报告文件存储路径',
  `report_version` int DEFAULT 1 COMMENT '报告版本号',
  `previous_report_id` bigint DEFAULT NULL COMMENT '前一版本报告ID',
  `remarks` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `issue_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '签发日期',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` datetime DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_no` (`report_no`),
  KEY `idx_delegation_id` (`delegation_id`),
  KEY `idx_issue_time` (`issue_time`),
  KEY `idx_reviewer_id` (`reviewer_id`),
  CONSTRAINT `fk_report_delegation` FOREIGN KEY (`delegation_id`) REFERENCES `biz_delegation` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_report_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='最终检验报告主表';

-- 10. 系统通知表
CREATE TABLE `sys_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收人ID',
  `user_type` tinyint NOT NULL COMMENT '用户类型：1-内部用户，2-客户',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` varchar(500) NOT NULL COMMENT '通知内容',
  `type` varchar(20) NOT NULL DEFAULT 'info' COMMENT '通知类型：info,warning,success',
  `biz_type` varchar(50) DEFAULT NULL COMMENT '业务类型：delegation,report,task',
  `biz_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `user_type`, `is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- 11. 系统配置表
CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` varchar(500) NOT NULL COMMENT '配置值',
  `description` varchar(200) DEFAULT NULL COMMENT '配置描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';


-- ----------------------------
-- 系统数据
-- ----------------------------

-- 1. 初始账号 (密码统一为 123456，每个用户使用独立的 BCrypt 盐值)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role_id`, `phone`, `status`) VALUES
('admin', '$2a$10$Z9e4H/86mNn1CO3WF3TMR.5dEFvQGWvb2sORLX7Fg1G.pHaljYO0u', '王建国', 1, '13800000000', 1),
('inspector', '$2a$10$bHXATrUA/GYeghGSnxM2muEXjAlTruq2m8Ufp431Y5y/dFywSQq52', '张三', 2, '13900000000', 1),
('inspector2', '$2a$10$bHXATrUA/GYeghGSnxM2muEXjAlTruq2m8Ufp431Y5y/dFywSQq52', '李四', 2, '13900000001', 1);

-- 2. 初始客户 (含 status 字段)
INSERT INTO `sys_client` (`company_name`, `contact_person`, `phone`, `address`, `login_account`, `login_password`, `status`) VALUES
('绿源生鲜食品有限公司', '张三', '13700000001', '工业园区1号', 'client1', '$2a$10$GfsTledLoRzDkOlJlAkyte2UaZR.3RS3mRUI4z4YgNfNvR3ewLe6q', 1),
('香满园肉制品有限公司', '李四', '13700000002', '科技园区A栋', 'client2', '$2a$10$yRXmDH9M0Or6GJIy1/5tB.uo/odDX1VJIfgxPyOn3gQOxTL7Xkp8a', 1),
('伊康乳业集团有限公司', '陈浩', '13700000003', '高新环保园区3号', 'client3', '$2a$10$QMgKppYGHEGMk7CyCmRMX.LsOXgmb5XqHWGeoCGfCfJqBNR5lgVaq', 1);

-- 3. 初始标准及项目
INSERT INTO `std_standard` (`id`, `standard_code`, `standard_name`, `standard_category`, `product_category`, `status`) VALUES
(1, 'GB 10770-2025', '食品安全国家标准 婴幼儿罐装辅助食品', '国家标准', '辅助食品', 1),
(2, 'GB/T 29602-2013', '固体饮料', '国家标准', '饮料', 1),
(3, 'GB 10146-2015', '食品安全国家标准 食用动物油脂', '国家标准', '食用油/油脂', 1),
(4, 'GB 13102-2022', '食品安全国家标准 浓缩乳制品', '国家标准', '乳制品', 1),
(5, 'GB 17399-2016', '食品安全国家标准 糖果', '国家标准', '糖果/零食', 1),
(6, 'GB 17401-2014', '食品安全国家标准 膨化食品', '国家标准', '糖果/零食', 1),
(7, 'GB 2715-2016', '食品安全国家标准 粮食', '国家标准', '粮食/谷物', 1),
(8, 'GB 2726-2016', '食品安全国家标准 熟肉制品', '国家标准', '肉制品', 1),
(9, 'GB 7099-2015', '食品安全国家标准 糕点、面包', '国家标准', '糕点/烘焙', 1);

INSERT INTO `std_inspection_item` (`standard_id`, `item_name`, `unit`, `judge_type`, `min_value`, `max_value`, `text_standard`) VALUES
-- GB 10770-2025 婴幼儿罐装辅助食品
(1, '蛋白质 (唯一配料型)', 'g/100kJ', 3, 1.700, NULL, NULL),
(1, '脂肪 (唯一配料型)', 'g/100kJ', 2, NULL, 1.400, NULL),
(1, '蛋白质 (混合配料型)', 'g/100kJ', 3, 0.700, NULL, NULL),
(1, '脂肪 (混合配料型)', 'g/100kJ', 2, NULL, 1.400, NULL),
(1, '组胺 (含鱼肉产品)', 'mg/100g', 2, NULL, 10.000, NULL),
(1, '钠 (即食状态)', 'mg/100g', 2, NULL, 200.000, NULL),
(1, '氯化钠添加', '', 4, NULL, NULL, '未添加'),
(1, '商业无菌', '', 4, NULL, NULL, '符合要求'),
(1, '番茄类霉菌', '%视野', 2, NULL, 40.000, NULL),
(1, '颗粒/片状大小 (6-12月龄)', 'mm', 2, NULL, 4.999, NULL),
(1, '感官(异物/骨鳞刺)', '', 4, NULL, NULL, '无'),
(1, '污染物限量(按GB 2762)', '', 4, NULL, NULL, '符合标准'),
(1, '真菌毒素限量(按GB 2761)', '', 4, NULL, NULL, '符合标准'),

-- GB/T 29602-2013 固体饮料
(2, '色泽', '', 4, NULL, NULL, '符合该产品应有的色泽'),
(2, '滋味与气味', '', 4, NULL, NULL, '具有该产品应有的滋味和气味，无异味'),
(2, '组织状态', '', 4, NULL, NULL, '干燥疏松，无结块，无正常视力可见外来杂质'),
(2, '水分 (果蔬/蛋白固体饮料)', '%', 2, NULL, 5.000, NULL),
(2, '水分 (其他固体饮料)', '%', 2, NULL, 7.000, NULL),
(2, '蛋白质 (蛋白固体饮料)', '%', 3, 4.000, NULL, NULL),
(2, '总糖', 'g/100g', 1, 0.000, 100.000, NULL),
(2, '菌落总数 (n=5,c=2,m=10^4,M=5×10^4)', 'CFU/g', 4, NULL, NULL, '符合限量要求'),
(2, '大肠菌群 (n=5,c=2,m=10,M=10^2)', 'CFU/g', 4, NULL, NULL, '符合限量要求'),
(2, '致病菌 (沙门氏菌/金黄色葡萄球菌等)', '', 4, NULL, NULL, '不应检出'),

-- GB 10146-2015 食用动物油脂
(3, '色泽', '', 4, NULL, NULL, '具有特有的色泽，呈白色或略带黄色，无霉斑'),
(3, '气味、滋味', '', 4, NULL, NULL, '具有特有的气味、滋味，无酸败及其他异味'),
(3, '状态', '', 4, NULL, NULL, '无正常视力可见的外来异物'),
(3, '酸价', 'mg/g', 2, NULL, 2.500, NULL),
(3, '过氧化值', 'g/100g', 2, NULL, 0.200, NULL),
(3, '丙二醛', 'mg/100g', 2, NULL, 0.250, NULL),

-- GB 13102-2022 浓缩乳制品
(4, '色泽', '', 4, NULL, NULL, '呈均匀一致的乳白色或微黄色'),
(4, '滋味与气味', '', 4, NULL, NULL, '具有浓缩乳制品应有的滋味和气味'),
(4, '组织状态', '', 4, NULL, NULL, '质地均匀，无肉眼可见外来杂质'),
(4, '脂肪 (淡炼乳)', 'g/100g', 3, 7.500, NULL, NULL),
(4, '非脂乳固体 (淡炼乳)', 'g/100g', 3, 17.500, NULL, NULL),
(4, '蛋白质', 'g/100g', 3, 34.000, NULL, NULL),
(4, '菌落总数', 'CFU/g', 4, NULL, NULL, '符合限量要求'),
(4, '致病菌', '', 4, NULL, NULL, '不应检出'),

-- GB 17399-2016 糖果
(5, '感官要求(色泽/状态/气味)', '', 4, NULL, NULL, '正常色泽、滋味，无异味及可见异物'),
(5, '菌落总数', 'CFU/g', 4, NULL, NULL, '符合限量要求'),
(5, '大肠菌群', 'CFU/g', 4, NULL, NULL, '符合限量要求'),

-- GB 17401-2014 膨化食品
(6, '感官要求(色泽/状态/气味)', '', 4, NULL, NULL, '正常色泽、滋味，无霉变及异物'),
(6, '水分', 'g/100g', 2, NULL, 7.000, NULL),
(6, '酸价(以脂肪计)', 'mg/g', 2, NULL, 5.000, NULL),
(6, '过氧化值(以脂肪计)', 'g/100g', 2, NULL, 0.250, NULL),
(6, '菌落总数', 'CFU/g', 4, NULL, NULL, '符合限量要求'),
(6, '大肠菌群', 'CFU/g', 4, NULL, NULL, '符合限量要求'),

-- GB 2715-2016 粮食
(7, '感官要求(色泽/气味/状态)', '', 4, NULL, NULL, '无异味、霉变及肉眼可见杂质'),
(7, '热损伤粒(小麦)', '%', 2, NULL, 0.500, NULL),
(7, '霉变粒(大豆)', '%', 2, NULL, 1.000, NULL),
(7, '霉变粒(其他)', '%', 2, NULL, 2.000, NULL),
(7, '总氢氰酸(木薯粉)', 'mg/kg', 2, NULL, 10.000, NULL),

-- GB 2726-2016 熟肉制品
(8, '感官要求(色泽/状态/气味)', '', 4, NULL, NULL, '正常色泽、滋味，无异味及外来异物'),
(8, '菌落总数', 'CFU/g', 4, NULL, NULL, '符合限量要求'),
(8, '大肠菌群', 'CFU/g', 4, NULL, NULL, '符合限量要求'),
(8, '致病菌', '', 4, NULL, NULL, '符合GB 29921规定'),

-- GB 7099-2015 糕点、面包
(9, '感官要求(色泽/状态/气味)', '', 4, NULL, NULL, '正常色泽、滋味，无异味、霉变及可见异物'),
(9, '酸价(以脂肪计)', 'mg/g', 2, NULL, 5.000, NULL),
(9, '过氧化值(以脂肪计)', 'g/100g', 2, NULL, 0.250, NULL),
(9, '菌落总数', 'CFU/g', 4, NULL, NULL, '符合限量要求'),
(9, '大肠菌群', 'CFU/g', 4, NULL, NULL, '符合限量要求'),
(9, '霉菌', 'CFU/g', 2, NULL, 150.000, NULL),
(9, '致病菌', '', 4, NULL, NULL, '符合GB 29921规定');

-- 4. 初始业务流转数据
INSERT INTO `biz_delegation` (`id`, `delegation_no`, `client_id`, `sample_name`, `sample_specs`, `sample_quantity`, `standard_id`, `status`, `submit_time`) VALUES
(1001, 'D202604180001', 1, '婴幼儿牛肉蔬菜泥罐头', '100g/罐', 2, 1, 0, '2026-04-18 09:10:00'),
(1002, 'D202604210001', 2, '固体饮料(蛋白固体饮料)', '20g/袋', 1, 2, 2, '2026-04-21 09:05:00'),
(1003, 'D202604220001', 3, '纯牛奶', '250ml/盒', 3, 4, 3, '2026-04-22 10:00:00');

INSERT INTO `biz_sample_task` (`id`, `delegation_id`, `blind_sample_code`, `inspector_id`, `receive_time`, `finish_time`, `receiver_id`, `status`) VALUES
(2002, 1002, 'SAM-20260421-DRINK-01', 2, '2026-04-21 09:30:00', '2026-04-21 10:05:00', 1, 1),
(2003, 1003, 'SAM-20260422-MILK-01', 2, '2026-04-22 10:30:00', '2026-04-22 11:00:00', 1, 1);

-- 委托 D202604210001 检测记录
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2002, id, NULL, '符合该产品应有的色泽', NULL, 1, '2026-04-21 09:40:00'
FROM std_inspection_item WHERE standard_id = 2 AND item_name = '色泽' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2002, id, NULL, '具有该产品应有的滋味和气味，无异味', NULL, 1, '2026-04-21 09:41:00'
FROM std_inspection_item WHERE standard_id = 2 AND item_name = '滋味与气味' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2002, id, NULL, '干燥疏松，无结块，无正常视力可见外来杂质', NULL, 1, '2026-04-21 09:42:00'
FROM std_inspection_item WHERE standard_id = 2 AND item_name = '组织状态' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2002, id, 4.200, NULL, NULL, 1, '2026-04-21 09:43:00'
FROM std_inspection_item WHERE standard_id = 2 AND item_name = '水分 (果蔬/蛋白固体饮料)' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2002, id, 6.500, NULL, NULL, 1, '2026-04-21 09:44:00'
FROM std_inspection_item WHERE standard_id = 2 AND item_name = '水分 (其他固体饮料)' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2002, id, 18.500, NULL, NULL, 1, '2026-04-21 09:45:00'
FROM std_inspection_item WHERE standard_id = 2 AND item_name = '蛋白质 (蛋白固体饮料)' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2002, id, 32.000, NULL, NULL, 1, '2026-04-21 09:46:00'
FROM std_inspection_item WHERE standard_id = 2 AND item_name = '总糖' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2002, id, NULL, '符合限量要求', NULL, 1, '2026-04-21 09:47:00'
FROM std_inspection_item WHERE standard_id = 2 AND item_name = '菌落总数 (n=5,c=2,m=10^4,M=5×10^4)' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2002, id, NULL, '符合限量要求', NULL, 1, '2026-04-21 09:48:00'
FROM std_inspection_item WHERE standard_id = 2 AND item_name = '大肠菌群 (n=5,c=2,m=10,M=10^2)' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2002, id, NULL, '不应检出', NULL, 1, '2026-04-21 09:49:00'
FROM std_inspection_item WHERE standard_id = 2 AND item_name = '致病菌 (沙门氏菌/金黄色葡萄球菌等)' LIMIT 1;

-- 委托 D202604220001 检测记录 (含不合格项)
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2003, id, NULL, '呈均匀一致的乳白色', NULL, 1, '2026-04-22 10:35:00'
FROM std_inspection_item WHERE standard_id = 4 AND item_name = '色泽' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2003, id, NULL, '具有浓缩乳制品应有的滋味和气味', NULL, 1, '2026-04-22 10:36:00'
FROM std_inspection_item WHERE standard_id = 4 AND item_name = '滋味与气味' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2003, id, NULL, '质地均匀，无肉眼可见外来杂质', NULL, 1, '2026-04-22 10:37:00'
FROM std_inspection_item WHERE standard_id = 4 AND item_name = '组织状态' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2003, id, 8.200, NULL, NULL, 1, '2026-04-22 10:38:00'
FROM std_inspection_item WHERE standard_id = 4 AND item_name = '脂肪 (淡炼乳)' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2003, id, 15.000, NULL, NULL, 0, '2026-04-22 10:39:00'
FROM std_inspection_item WHERE standard_id = 4 AND item_name = '非脂乳固体 (淡炼乳)' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2003, id, 35.000, NULL, NULL, 1, '2026-04-22 10:40:00'
FROM std_inspection_item WHERE standard_id = 4 AND item_name = '蛋白质' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2003, id, NULL, '符合限量要求', NULL, 1, '2026-04-22 10:41:00'
FROM std_inspection_item WHERE standard_id = 4 AND item_name = '菌落总数' LIMIT 1;
INSERT INTO `biz_inspection_record` (`task_id`, `item_id`, `measured_value`, `measured_text`, `photo_url`, `result`, `inspect_time`)
SELECT 2003, id, NULL, '不应检出', NULL, 1, '2026-04-22 10:42:00'
FROM std_inspection_item WHERE standard_id = 4 AND item_name = '致病菌' LIMIT 1;

-- 报告
INSERT INTO `biz_report` (`report_no`, `delegation_id`, `reviewer_id`, `final_conclusion`, `report_file_url`, `issue_time`) VALUES
('R202604220001', 1003, 1, 0, '/uploads/reports/R202604220001.pdf', '2026-04-22 14:00:00');

-- 操作日志
INSERT INTO `sys_operate_log` (`delegation_id`, `operator`, `action`, `action_type`, `description`, `create_time`) VALUES
(1001, '客户', '新委托', 'primary', '收到新的检验委托单 D202604180001', '2026-04-18 09:10:00'),
(1002, '客户', '新委托', 'primary', '收到新的检验委托单 D202604210001', '2026-04-21 09:05:00'),
(1002, '管理员', '收样与派发', 'warning', '已收样并生成盲样任务 SAM-20260421-DRINK-01', '2026-04-21 09:30:00'),
(1002, '检测员', '完成检测', 'info', '检测员完成了盲样 SAM-20260421-DRINK-01 的实测录入', '2026-04-21 10:05:00'),
(1003, '客户', '新委托', 'primary', '收到新的检验委托单 D202604220001', '2026-04-22 10:00:00'),
(1003, '管理员', '收样与派发', 'warning', '已收样并生成盲样任务 SAM-20260422-MILK-01', '2026-04-22 10:30:00'),
(1003, '检测员', '完成检测', 'info', '检测员完成了盲样 SAM-20260422-MILK-01 的实测录入', '2026-04-22 11:00:00'),
(1003, '管理员', '签发报告', 'success', '已签发检验报告 R202604220001', '2026-04-22 14:00:00');

-- 系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`) VALUES
('report_no_prefix', 'R', '报告编号前缀'),
('delegation_no_prefix', 'D', '委托单号前缀'),
('blind_code_prefix', 'SAM', '盲样编号前缀'),
('max_upload_size_mb', '10', '最大上传文件大小(MB)'),
('session_timeout_hours', '24', '会话超时时间(小时)');