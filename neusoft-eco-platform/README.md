# 东软环保公众监督平台 (NEPV)

基于 Spring Boot + Vue3 的环境监测网格化管理平台，实现公众反馈、网格员处理、决策者统计的完整业务闭环。

## 项目结构

```
neusoft-eco-platform/
├── backend/              # 后端服务（Spring Boot + MyBatis-Plus + MySQL）
│   ├── src/              # Java 源码
│   ├── sql/              # 数据库初始化脚本
│   ├── docs/             # 后端文档
│   └── pom.xml           # Maven 配置
│
├── frontend-admin/       # 管理端（Vue3 + Element Plus + Vite）
│   ├── src/
│   │   ├── api/          # 接口封装
│   │   ├── pages/        # 页面组件
│   │   ├── components/   # 公共组件
│   │   ├── router/       # 路由配置
│   │   └── stores/       # Pinia 状态管理
│   └── package.json
│
├── frontend-grid/        # 网格员端（Vue3 + Vant4 + Vite）
│   ├── src/
│   │   ├── api/          # 接口封装
│   │   ├── views/        # 页面组件
│   │   └── router/       # 路由配置
│   └── package.json
│
└── docs/                 # 项目文档
```

## 技术栈

### 后端
- **框架**：Spring Boot 2.7.x
- **ORM**：MyBatis-Plus 3.5.x
- **数据库**：MySQL 8.0
- **构建工具**：Maven
- **认证**：Token 机制

### 管理端前端
- **框架**：Vue 3 + Vite
- **UI 组件库**：Element Plus
- **状态管理**：Pinia
- **路由**：Vue Router 4
- **HTTP 客户端**：Axios
- **图表**：ECharts

### 网格员端前端
- **框架**：Vue 3 + Vite
- **UI 组件库**：Vant 4
- **路由**：Vue Router 4
- **HTTP 客户端**：Axios

## 功能模块

### 后端接口
- 公众监督员反馈提交
- 管理员反馈指派（智能指派 + 手动指派）
- 网格员任务列表与实测数据提交
- AQI 统计分析与大屏展示
- 省份/城市/网格员基础数据管理

### 管理端 (Admin)
- 反馈列表与详情查看
- 反馈指派给网格员
- AQI 统计分析
- 环境监测大屏（PM2.5 / SO₂ / CO 超标统计）
- 管理员登录与权限控制

### 网格员端 (Grid)
- 网格员登录
- 待检测 / 已完成任务列表
- 任务详情查看
- 实测数据提交（PM2.5 / PM10 / SO₂ / NO₂ / CO / O₃）
- AQI 实时计算

## 快速开始

### 环境要求
- JDK 1.8+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 数据库初始化
```bash
# 执行初始化脚本
mysql -u root -p < backend/sql/init.sql
```

### 后端启动
```bash
cd backend
mvn spring-boot:run
```
服务默认端口：`8080`

### 管理端启动
```bash
cd frontend-admin
npm install
npm run dev
```
访问地址：`http://localhost:5173`

### 网格员端启动
```bash
cd frontend-grid
npm install
npm run dev
```
访问地址：`http://localhost:5174`（可配置）

## 超标阈值说明

| 污染物 | 超标阈值 | 单位 |
|--------|----------|------|
| PM2.5 | > 75 | μg/m³ |
| PM10 | > 150 | μg/m³ |
| SO₂ | > 150 | μg/m³ |
| NO₂ | > 80 | μg/m³ |
| CO | > 4 | mg/m³ |
| O₃ | > 160 | μg/m³ |

## AQI 等级划分

| AQI 范围 | 等级 | 颜色 |
|----------|------|------|
| 0-50 | 优 | 绿色 |
| 51-100 | 良 | 黄色 |
| 101-150 | 轻度污染 | 橙色 |
| 151-200 | 中度污染 | 红色 |
| 201-300 | 重度污染 | 紫色 |
| > 300 | 严重污染 | 褐红色 |

## 测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | admin | 123456 |
| 网格员 | grid01 | 123456 |

## 许可证

本项目仅供学习交流使用。
