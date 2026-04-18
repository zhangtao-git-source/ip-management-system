-- IP地址管理系统数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS ip_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ip_management;

-- IP地址表
CREATE TABLE IF NOT EXISTS tb_ip_address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址',
    ip_type TINYINT DEFAULT 1 COMMENT '地址类型:1-IPv4,2-IPv6',
    subnet_mask VARCHAR(45) DEFAULT NULL COMMENT '子网掩码',
    prefix_length TINYINT DEFAULT NULL COMMENT '前缀长度',
    gateway VARCHAR(45) DEFAULT NULL COMMENT '网关地址',
    dns_primary VARCHAR(45) DEFAULT NULL COMMENT '主DNS',
    dns_secondary VARCHAR(45) DEFAULT NULL COMMENT '辅DNS',
    address_pool_id BIGINT DEFAULT NULL COMMENT '所属地址池ID',
    status TINYINT DEFAULT 1 COMMENT '地址状态:1-未分配,2-已分配,3-预留,4-冻结',
    region_code VARCHAR(20) DEFAULT NULL COMMENT '所属区域编码',
    device_id BIGINT DEFAULT NULL COMMENT '关联设备ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_ip_address (ip_address),
    KEY idx_address_pool_id (address_pool_id),
    KEY idx_status (status),
    KEY idx_region_code (region_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP地址表';

-- 地址池表
CREATE TABLE IF NOT EXISTS tb_address_pool (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    pool_name VARCHAR(100) NOT NULL COMMENT '地址池名称',
    pool_code VARCHAR(50) NOT NULL COMMENT '地址池编码',
    ip_type TINYINT DEFAULT 1 COMMENT '地址类型:1-IPv4,2-IPv6',
    start_address VARCHAR(45) NOT NULL COMMENT '起始地址',
    end_address VARCHAR(45) NOT NULL COMMENT '结束地址',
    total_count INT DEFAULT 0 COMMENT '地址总数',
    available_count INT DEFAULT 0 COMMENT '可用数量',
    region_code VARCHAR(20) DEFAULT NULL COMMENT '所属区域',
    allocation_strategy TINYINT DEFAULT 1 COMMENT '分配策略:1-顺序分配,2-随机分配',
    priority INT DEFAULT 0 COMMENT '优先级',
    status TINYINT DEFAULT 1 COMMENT '状态:1-启用,2-禁用',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_pool_code (pool_code),
    KEY idx_ip_type (ip_type),
    KEY idx_region_code (region_code),
    KEY idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址池表';

-- IP分配记录表
CREATE TABLE IF NOT EXISTS tb_ip_allocation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ip_address_id BIGINT NOT NULL COMMENT 'IP地址ID',
    ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址',
    address_pool_id BIGINT DEFAULT NULL COMMENT '地址池ID',
    allocation_type TINYINT DEFAULT 1 COMMENT '分配类型:1-自动分配,2-手动分配',
    subscriber_id VARCHAR(50) DEFAULT NULL COMMENT '客户ID',
    service_order_id VARCHAR(50) DEFAULT NULL COMMENT '业务工单ID',
    allocation_time DATETIME DEFAULT NULL COMMENT '分配时间',
    expiration_time DATETIME DEFAULT NULL COMMENT '到期时间',
    release_time DATETIME DEFAULT NULL COMMENT '释放时间',
    release_type TINYINT DEFAULT NULL COMMENT '释放类型:1-到期释放,2-主动释放',
    status TINYINT DEFAULT 1 COMMENT '状态:1-使用中,2-已释放',
    operator_id VARCHAR(30) DEFAULT NULL COMMENT '操作员ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ip_address_id (ip_address_id),
    KEY idx_subscriber_id (subscriber_id),
    KEY idx_service_order_id (service_order_id),
    KEY idx_allocation_time (allocation_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP分配记录表';

-- 设备表
CREATE TABLE IF NOT EXISTS tb_device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_code VARCHAR(50) NOT NULL COMMENT '设备编码',
    device_type VARCHAR(30) DEFAULT NULL COMMENT '设备类型:OLT/BRAS/交换机等',
    device_ip VARCHAR(45) DEFAULT NULL COMMENT '设备管理IP地址',
    vendor VARCHAR(50) DEFAULT NULL COMMENT '设备厂商',
    region_code VARCHAR(20) DEFAULT NULL COMMENT '所属区域',
    status TINYINT DEFAULT 2 COMMENT '状态:1-在线,2-离线',
    last_collection_time DATETIME DEFAULT NULL COMMENT '最后采集时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_device_code (device_code),
    KEY idx_device_type (device_type),
    KEY idx_region_code (region_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 地址绑定关系表
CREATE TABLE IF NOT EXISTS tb_ip_binding (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    ip_address_id BIGINT NOT NULL COMMENT 'IP地址ID',
    ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    binding_type TINYINT DEFAULT 1 COMMENT '绑定类型:1-静态绑定,2-动态绑定',
    binding_time DATETIME DEFAULT NULL COMMENT '绑定时间',
    unbinding_time DATETIME DEFAULT NULL COMMENT '解绑时间',
    status TINYINT DEFAULT 1 COMMENT '状态:1-已绑定,2-已解绑',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ip_address_id (ip_address_id),
    KEY idx_device_id (device_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址绑定关系表';

-- 插入测试数据
INSERT INTO tb_address_pool (pool_name, pool_code, ip_type, start_address, end_address, total_count, available_count, region_code, allocation_strategy, priority, status) 
VALUES ('IPv4公网地址池', 'POOL_IPV4_PUBLIC', 1, '218.201.1.0', '218.201.1.255', 256, 256, 'XIAN', 1, 1, 1);

INSERT INTO tb_address_pool (pool_name, pool_code, ip_type, start_address, end_address, total_count, available_count, region_code, allocation_strategy, priority, status) 
VALUES ('IPv4私网地址池', 'POOL_IPV4_PRIVATE', 1, '10.0.1.0', '10.0.1.255', 256, 256, 'XIAN', 1, 2, 1);