-- IP地址管理系统数据库初始化脚本 (H2 Compatible)

-- 地市区县表
CREATE TABLE IF NOT EXISTS tb_region (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    region_code VARCHAR(20) NOT NULL UNIQUE,
    region_name VARCHAR(50) NOT NULL,
    region_type TINYINT DEFAULT 1,
    parent_code VARCHAR(20) DEFAULT NULL,
    province_code VARCHAR(20) DEFAULT '61',
    province_name VARCHAR(50) DEFAULT '陕西省',
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 地址池表
CREATE TABLE IF NOT EXISTS tb_address_pool (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pool_name VARCHAR(100) NOT NULL,
    pool_code VARCHAR(50) NOT NULL,
    ip_type TINYINT DEFAULT 1,
    start_address VARCHAR(45) NOT NULL,
    end_address VARCHAR(45) NOT NULL,
    total_count INT DEFAULT 0,
    available_count INT DEFAULT 0,
    region_code VARCHAR(20) DEFAULT NULL,
    allocation_strategy TINYINT DEFAULT 1,
    priority INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- IP地址表
CREATE TABLE IF NOT EXISTS tb_ip_address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ip_address VARCHAR(45) NOT NULL,
    ip_type TINYINT DEFAULT 1,
    subnet_mask VARCHAR(45) DEFAULT NULL,
    prefix_length TINYINT DEFAULT NULL,
    gateway VARCHAR(45) DEFAULT NULL,
    dns_primary VARCHAR(45) DEFAULT NULL,
    dns_secondary VARCHAR(45) DEFAULT NULL,
    address_pool_id BIGINT DEFAULT NULL,
    status TINYINT DEFAULT 1,
    region_code VARCHAR(20) DEFAULT NULL,
    device_id BIGINT DEFAULT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- IP分配记录表
CREATE TABLE IF NOT EXISTS tb_ip_allocation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ip_address_id BIGINT NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    address_pool_id BIGINT DEFAULT NULL,
    allocation_type TINYINT DEFAULT 1,
    subscriber_id VARCHAR(50) DEFAULT NULL,
    service_order_id VARCHAR(50) DEFAULT NULL,
    allocation_time TIMESTAMP DEFAULT NULL,
    expiration_time TIMESTAMP DEFAULT NULL,
    release_time TIMESTAMP DEFAULT NULL,
    release_type TINYINT DEFAULT NULL,
    status TINYINT DEFAULT 1,
    operator_id VARCHAR(30) DEFAULT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 设备表
CREATE TABLE IF NOT EXISTS tb_device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_name VARCHAR(100) NOT NULL,
    device_code VARCHAR(50) NOT NULL,
    device_type VARCHAR(30) DEFAULT NULL,
    device_ip VARCHAR(45) DEFAULT NULL,
    vendor VARCHAR(50) DEFAULT NULL,
    region_code VARCHAR(20) DEFAULT NULL,
    status TINYINT DEFAULT 2,
    last_collection_time TIMESTAMP DEFAULT NULL,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 地址绑定关系表
CREATE TABLE IF NOT EXISTS tb_ip_binding (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ip_address_id BIGINT NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    device_id BIGINT NOT NULL,
    binding_type TINYINT DEFAULT 1,
    binding_time TIMESTAMP DEFAULT NULL,
    unbinding_time TIMESTAMP DEFAULT NULL,
    status TINYINT DEFAULT 1,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
