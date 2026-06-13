-- ============================================================
-- 东软环保公众监督平台 建表脚本
-- 数据库：MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS neuedu_ep CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE neuedu_ep;

-- ----------------------------
-- 1. 省区域表
-- ----------------------------
DROP TABLE IF EXISTS province;
CREATE TABLE province (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    province_code VARCHAR(10) NOT NULL COMMENT '省份编码',
    province_name VARCHAR(50) NOT NULL COMMENT '省份名称'
) COMMENT '系统网格覆盖省区域表';

-- ----------------------------
-- 2. 市区域表
-- ----------------------------
DROP TABLE IF EXISTS city;
CREATE TABLE city (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    province_id BIGINT NOT NULL COMMENT '所属省份ID',
    city_code VARCHAR(10) NOT NULL COMMENT '城市编码',
    city_name VARCHAR(50) NOT NULL COMMENT '城市名称'
) COMMENT '系统网格覆盖市区域表';

-- ----------------------------
-- 3. 空气质量指数级别表
-- ----------------------------
DROP TABLE IF EXISTS aqi_level;
CREATE TABLE aqi_level (
    id INT PRIMARY KEY AUTO_INCREMENT,
    aqi_min INT NOT NULL COMMENT 'AQI最小值',
    aqi_max INT NOT NULL COMMENT 'AQI最大值',
    level VARCHAR(10) NOT NULL COMMENT '级别',
    description VARCHAR(50) COMMENT '级别描述',
    color VARCHAR(20) COMMENT '展示颜色'
) COMMENT '空气质量指数级别表';

-- ----------------------------
-- 4. 公众监督员表
-- ----------------------------
DROP TABLE IF EXISTS public_supervisor;
CREATE TABLE public_supervisor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '联系电话',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) COMMENT '公众监督员表';

-- ----------------------------
-- 5. 系统管理员表
-- ----------------------------
DROP TABLE IF EXISTS sys_admin;
CREATE TABLE sys_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    role VARCHAR(20) NOT NULL COMMENT '角色（super/admin）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '系统管理员表';

-- ----------------------------
-- 6. 网格员表
-- ----------------------------
DROP TABLE IF EXISTS grid_member;
CREATE TABLE grid_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '联系电话',
    grid_address VARCHAR(200) NOT NULL COMMENT '负责网格地址',
    city_id BIGINT NOT NULL COMMENT '所属城市ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用'
) COMMENT '空气质量监测网格员表';

-- ----------------------------
-- 7. 反馈表
-- ----------------------------
DROP TABLE IF EXISTS feedback;
CREATE TABLE feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    public_user_id BIGINT NOT NULL COMMENT '公众监督员ID',
    grid_address VARCHAR(200) NOT NULL COMMENT '网格地址',
    aqi_value INT NOT NULL COMMENT 'AQI数值',
    pm25 DECIMAL(10,2) COMMENT 'PM2.5浓度',
    pm10 DECIMAL(10,2) COMMENT 'PM10浓度',
    so2 DECIMAL(10,2) COMMENT 'SO₂浓度',
    no2 DECIMAL(10,2) COMMENT 'NO₂浓度',
    co DECIMAL(10,2) COMMENT 'CO浓度',
    o3 DECIMAL(10,2) COMMENT 'O₃浓度',
    feedback_time DATETIME NOT NULL COMMENT '反馈时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-未指派，1-已指派，2-已确认',
    assign_grid_member_id BIGINT COMMENT '指派网格员ID'
) COMMENT '空气质量公众监督反馈表';

-- ----------------------------
-- 8. 监测数据统计表
-- ----------------------------
DROP TABLE IF EXISTS statistics;
CREATE TABLE statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    grid_address VARCHAR(200) NOT NULL COMMENT '网格地址',
    aqi_value INT NOT NULL COMMENT 'AQI数值',
    pm25 DECIMAL(10,2) COMMENT 'PM2.5浓度',
    measure_time DATETIME NOT NULL COMMENT '监测时间',
    grid_member_id BIGINT NOT NULL COMMENT '网格员ID'
) COMMENT '空气质量监测数据统计表';

-- ----------------------------
-- 9. 监测数据确认表
-- ----------------------------
DROP TABLE IF EXISTS measure_data;
CREATE TABLE measure_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    feedback_id BIGINT NOT NULL COMMENT '对应反馈ID',
    grid_member_id BIGINT NOT NULL COMMENT '网格员ID',
    aqi_value INT NOT NULL COMMENT '实测AQI值',
    pm25 DECIMAL(10,2) COMMENT '实测PM2.5',
    measure_time DATETIME NOT NULL COMMENT '实测时间',
    confirm_result TINYINT NOT NULL COMMENT '1-一致，2-不一致'
) COMMENT '空气质量监测数据表';

-- ============================================================
-- 初始化测试数据
-- ============================================================

-- 省份数据
INSERT INTO province (province_code, province_name) VALUES
('110000', '北京市'),
('210000', '辽宁省');

-- 城市数据
INSERT INTO city (province_id, city_code, city_name) VALUES
(1, '110105', '朝阳区'),
(1, '110108', '海淀区'),
(2, '210200', '大连市');

-- AQI级别数据
INSERT INTO aqi_level (aqi_min, aqi_max, level, description, color) VALUES
(0, 50, '优', '空气质量令人满意，基本无空气污染', '#00e400'),
(51, 100, '良', '空气质量可接受，但某些污染物可能对极少数异常敏感人群健康有较弱影响', '#ffff00'),
(101, 150, '轻度污染', '易感人群症状有轻度加剧，健康人群出现刺激症状', '#ff7e00'),
(151, 200, '中度污染', '进一步加剧易感人群症状，可能对健康人群心脏、呼吸系统有影响', '#ff0000'),
(201, 300, '重度污染', '心脏病和肺病患者症状显著加剧，运动耐受力降低，健康人群普遍出现症状', '#99004c'),
(301, 500, '严重污染', '健康人群运动耐受力降低，有明显强烈症状，提前出现某些疾病', '#7e0023');

-- 管理员数据
INSERT INTO sys_admin (username, password, role) VALUES
('admin', '123456', 'super'),
('operator', '123456', 'admin');

-- 公众监督员数据
INSERT INTO public_supervisor (username, password, real_name, phone) VALUES
('zhangsan', '123456', '张三', '13800138001'),
('lisi', '123456', '李四', '13800138002');

-- 网格员数据
INSERT INTO grid_member (username, password, real_name, phone, grid_address, city_id) VALUES
('grid01', '123456', '王网格', '13900139001', '北京市朝阳区望京街道', 1),
('grid02', '123456', '赵网格', '13900139002', '北京市海淀区中关村街道', 2),
('grid03', '123456', '刘网格', '13900139003', '大连市沙河口区', 3);

-- 反馈测试数据
INSERT INTO feedback (public_user_id, grid_address, aqi_value, pm25, pm10, so2, no2, co, o3, feedback_time, status, assign_grid_member_id) VALUES
(1, '北京市朝阳区望京街道', 85, 35.20, 68.50, 12.00, 45.00, 0.80, 120.00, '2026-06-01 10:30:00', 1, 1),
(2, '北京市海淀区中关村街道', 120, 55.80, 95.30, 18.50, 62.00, 1.20, 85.00, '2026-06-02 14:20:00', 0, NULL),
(1, '大连市沙河口区', 45, 18.00, 42.00, 8.00, 28.00, 0.50, 95.00, '2026-06-03 09:15:00', 2, 3);
