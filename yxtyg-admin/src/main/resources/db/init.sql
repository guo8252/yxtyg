-- =============================================
-- 一线体验官专项数据分析系统 - 数据库初始化脚本
-- 数据库：MySQL 8.0
-- 更新时间：2026-04-13
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS yxtyg_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 使用数据库
USE yxtyg_db;

-- =============================================
-- 1. 体验官表
-- =============================================
DROP TABLE IF EXISTS t_experimenter;
CREATE TABLE t_experimenter (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    city VARCHAR(20) NOT NULL COMMENT '地市',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱（选填）',
    role VARCHAR(50) DEFAULT NULL COMMENT '角色（选填）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    is_contact TINYINT DEFAULT 0 COMMENT '是否接口人(0-否,1-是)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_city (city),
    KEY idx_name (name),
    KEY idx_is_contact (is_contact)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体验官表';

-- =============================================
-- 2. 体验官操作日志表
-- =============================================
DROP TABLE IF EXISTS t_experimenter_log;
CREATE TABLE t_experimenter_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：新增、编辑、删除、批量新增、批量删除',
    operator VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    operation_time DATETIME NOT NULL COMMENT '操作时间',
    experimenter_id BIGINT DEFAULT NULL COMMENT '体验官ID',
    city VARCHAR(50) DEFAULT NULL COMMENT '地市',
    name VARCHAR(50) DEFAULT NULL COMMENT '姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    role VARCHAR(100) DEFAULT NULL COMMENT '角色',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    is_contact TINYINT(1) DEFAULT 0 COMMENT '是否接口人 0-否 1-是',
    detail VARCHAR(500) DEFAULT NULL COMMENT '操作详情',
    PRIMARY KEY (id),
    KEY idx_operation_type (operation_type),
    KEY idx_operation_time (operation_time),
    KEY idx_experimenter_id (experimenter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体验官操作日志表';

-- =============================================
-- 3. 工单主表
-- =============================================
DROP TABLE IF EXISTS t_work_order;
CREATE TABLE t_work_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL COMMENT 'BOMC流水号',
    declare_phone VARCHAR(20) DEFAULT NULL COMMENT '申告电话',
    handle_unit VARCHAR(100) DEFAULT NULL COMMENT '处理单位',
    handler_name VARCHAR(50) DEFAULT NULL COMMENT '处理人',
    city VARCHAR(50) DEFAULT NULL COMMENT '申告地市',
    create_time_str VARCHAR(50) DEFAULT NULL COMMENT '创建时间（原始字符串）',
    initiator_name VARCHAR(50) DEFAULT NULL COMMENT '发起人姓名',
    handle_satisfaction_score VARCHAR(20) DEFAULT NULL COMMENT '处理满意度评分',
    dispatch_satisfaction_score VARCHAR(20) DEFAULT NULL COMMENT '派单满意度评分',
    unsatisfied_reason VARCHAR(500) DEFAULT NULL COMMENT '不满意原因',
    status VARCHAR(50) DEFAULT NULL COMMENT '状态',
    solution_history_str TEXT COMMENT '解决方案历史原始字符串',
    content TEXT COMMENT '工单内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_status (order_no, status),
    KEY idx_order_no (order_no),
    KEY idx_city (city),
    KEY idx_status (status),
    KEY idx_create_time (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单主表';

-- =============================================
-- 4. 解决方案历史表
-- =============================================
DROP TABLE IF EXISTS t_solution_history;
CREATE TABLE t_solution_history (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    work_order_id BIGINT NOT NULL COMMENT '工单ID',
    order_no VARCHAR(50) NOT NULL COMMENT 'BOMC流水号（冗余存储）',
    handler_name VARCHAR(50) DEFAULT NULL COMMENT '处理人',
    opinion_content TEXT COMMENT '意见内容',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_work_order_id (work_order_id),
    KEY idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='解决方案历史表';

-- =============================================
-- 5. 评审会议纪要表
-- =============================================
DROP TABLE IF EXISTS t_review_meeting;
CREATE TABLE t_review_meeting (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    demand_no VARCHAR(50) NOT NULL COMMENT '评审方案需求号',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    meeting_time DATETIME NOT NULL COMMENT '会议时间',
    participants JSON DEFAULT NULL COMMENT '与会人列表（JSON: [{name,city}]）',
    content TEXT COMMENT '会议内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_meeting_time (meeting_time),
    KEY idx_demand_no (demand_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评审会议纪要表';

-- =============================================
-- 6. 转培上报记录表
-- =============================================
DROP TABLE IF EXISTS t_training_report;
CREATE TABLE t_training_report (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    city VARCHAR(20) NOT NULL COMMENT '地市',
    report_month VARCHAR(10) NOT NULL COMMENT '培训月份 YYYY-MM',
    report_time DATE DEFAULT NULL COMMENT '上报时间',
    reporter VARCHAR(50) DEFAULT NULL COMMENT '上报人',
    record_count INT DEFAULT 0 COMMENT '培训记录条数',
    training_person_count INT DEFAULT 0 COMMENT '培训人数（覆盖人数合计）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_city_month (city, report_month),
    KEY idx_city (city),
    KEY idx_report_month (report_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转培上报记录表';

-- =============================================
-- 7. 培训记录明细表
-- =============================================
DROP TABLE IF EXISTS t_training_record;
CREATE TABLE t_training_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    report_id BIGINT NOT NULL COMMENT '关联上报记录ID',
    training_time DATETIME DEFAULT NULL COMMENT '培训时间',
    organizer VARCHAR(50) DEFAULT NULL COMMENT '组织人员',
    training_subject VARCHAR(200) DEFAULT NULL COMMENT '培训主体',
    coverage_count INT DEFAULT NULL COMMENT '覆盖人数',
    training_content TEXT COMMENT '培训内容',
    screenshot LONGBLOB COMMENT '培训截图（二进制存储）',
    screenshot_name VARCHAR(100) DEFAULT NULL COMMENT '图片文件名',
    screenshot_type VARCHAR(50) DEFAULT NULL COMMENT '图片类型(image/jpeg等)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_report_id (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训记录明细表';

-- =============================================
-- 8. 大模型配置表
-- =============================================
DROP TABLE IF EXISTS t_model_config;
CREATE TABLE t_model_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '配置名称（别名）',
    provider VARCHAR(50) NOT NULL COMMENT '提供商类型（ollama/openai/custom）',
    base_url VARCHAR(255) NOT NULL COMMENT 'API基础地址',
    api_key VARCHAR(255) DEFAULT NULL COMMENT 'API密钥（加密存储）',
    models TEXT COMMENT '可用模型列表（JSON）',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认模型（0-否,1-是）',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用,1-启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_provider (provider),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大模型配置表';

-- =============================================
-- 9. 向量化模型配置表
-- =============================================
DROP TABLE IF EXISTS t_embedding_config;
CREATE TABLE t_embedding_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '配置名称（别名）',
    provider VARCHAR(50) NOT NULL COMMENT '提供商类型（ollama/openai/custom）',
    base_url VARCHAR(255) NOT NULL COMMENT 'Embedding接口地址',
    api_key VARCHAR(255) DEFAULT NULL COMMENT 'API密钥',
    model VARCHAR(100) NOT NULL COMMENT 'Embedding模型名称',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认配置（0-否,1-是）',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用,1-启用）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_provider (provider),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='向量化模型配置表';

-- =============================================
-- 10. 智能体配置表
-- =============================================
DROP TABLE IF EXISTS t_agent_config;
CREATE TABLE t_agent_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    agent_code VARCHAR(50) NOT NULL COMMENT '智能体编码（唯一标识）',
    agent_name VARCHAR(100) NOT NULL COMMENT '智能体名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '功能描述',
    model_config_id BIGINT DEFAULT NULL COMMENT '关联的大模型配置ID',
    model_name VARCHAR(100) DEFAULT NULL COMMENT '使用的具体模型名称',
    temperature DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度参数（0-2）',
    top_p DECIMAL(3,2) DEFAULT 0.90 COMMENT 'Top P参数（0-1）',
    repetition_penalty DECIMAL(3,2) DEFAULT 1.10 COMMENT '重复惩罚（0-2）',
    system_prompt TEXT COMMENT '系统提示词',
    user_prompt TEXT COMMENT '用户提示词模板，使用{content}占位符',
    max_tokens INT DEFAULT 2048 COMMENT '最大输出token数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_agent_code (agent_code),
    KEY idx_model_config_id (model_config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体配置表';

-- =============================================
-- 初始化智能体数据
-- =============================================
INSERT INTO t_agent_config (agent_code, agent_name, description, temperature, top_p, repetition_penalty, system_prompt, user_prompt, max_tokens) VALUES
-- 与会人员识别智能体
('participant_parser', '与会人员识别', '从签到文本中识别与会人员的姓名和地市', 0.10, 0.10, 1.00, 
'从文本中提取与会人员的姓名和地市。
江苏省地市：南京、苏州、无锡、常州、镇江、扬州、泰州、南通、盐城、淮安、连云港、徐州、宿迁
输入格式通常是"地市+姓名"，如"苏州任丽华"表示地市=苏州，姓名=任丽华，任何空格或非中文都视为连接符
每一行是一次签到，如果没有地市前缀，city字段留空
只输出JSON数组：[{"name":"姓名","city":"地市"}]', 
'{content}', 2048),

-- 转培记录识别智能体
('training_parser', '转培记录识别', '从表格文本中提取培训记录信息', 0.00, 0.10, 1.00,
'从表格文本中提取培训记录信息。
江苏省地市：南京、苏州、无锡、常州、镇江、扬州、泰州、南通、盐城、淮安、连云港、徐州、宿迁
每一行都是一次培训记录，不要遗漏数据，也不要过度思考
字段映射规则（第一行为表头）：
- 地市（可能叫"地市"或"归属"）-> city
- 组织人员 -> organizer
- 辅导/培训主题或主体 -> trainingContent
- 辅导/培训时间 -> trainingTime
- 覆盖人数 -> coverageCount
时间格式处理：
- 转换为当年日期，格式为yyyy-MM-dd
只输出JSON数组：[{"city":"","organizer":"","trainingContent":"","trainingTime":"","coverageCount":""}]',
'{content}', 2048);

-- =============================================
-- 11. 用户表
-- =============================================
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    role VARCHAR(20) NOT NULL COMMENT '角色：PRODUCT_MANAGER/DEV_ADMIN/SYS_ADMIN',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 12. 需求表
-- =============================================
DROP TABLE IF EXISTS t_requirement;
CREATE TABLE t_requirement (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '需求名称',
    description TEXT COMMENT '需求描述',
    product_manager_id BIGINT NOT NULL COMMENT '产品经理ID',
    system_name VARCHAR(100) NOT NULL COMMENT '归属系统',
    initial_workload DECIMAL(10,2) NOT NULL COMMENT '初核工作量（人天）',
    initial_amount DECIMAL(12,2) NOT NULL COMMENT '初核金额（元）',
    final_workload DECIMAL(10,2) DEFAULT NULL COMMENT '最终核定工作量（人天）',
    reduced_workload DECIMAL(10,2) DEFAULT NULL COMMENT '核减工作量（人天）',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/FILLED/APPROVED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_product_manager_id (product_manager_id),
    KEY idx_status (status),
    KEY idx_system_name (system_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求表';

-- =============================================
-- 13. 催办记录表
-- =============================================
DROP TABLE IF EXISTS t_urge_record;
CREATE TABLE t_urge_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    requirement_id BIGINT NOT NULL COMMENT '需求ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '催办时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_requirement_id (requirement_id),
    KEY idx_operator_id (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='催办记录表';

-- 初始化默认系统管理员
INSERT INTO t_user (username, real_name, password, role, status) VALUES
('admin', '系统管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'SYS_ADMIN', 1);

-- =============================================
-- MySQL 配置建议
-- =============================================

-- 修复 MySQL max_allowed_packet 限制（解决上传大图片时报错）
-- SET GLOBAL max_allowed_packet = 67108864;  -- 64MB

-- =============================================
-- 脚本执行完成
-- =============================================
SELECT '数据库初始化完成' AS result;
