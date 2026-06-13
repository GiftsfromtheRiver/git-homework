# NEPG 网格员端

环境公众监督平台（NEPG）网格员移动端应用，基于 Vue 3 + Vant 4 构建。

## 功能特性

- 🔐 **网格员登录**：账号密码登录，token 鉴权
- 📋 **任务列表**：待检测/已完成任务分类展示，支持下拉刷新和上拉加载
- 📝 **任务详情**：查看反馈信息、AQI 数据
- 📊 **实测数据录入**：填写 PM2.5、PM10、SO₂、NO₂、CO、O₃ 六项污染物浓度
- 🧮 **AQI 自动计算**：根据国家标准自动计算 AQI 指数、首要污染物和各污染物分指数
- ✅ **一致性确认**：标记实测结果与反馈是否一致
- 📱 **移动端适配**：基于 Vant 组件库，完美适配移动端

## 技术栈

- **框架**：Vue 3 + Composition API
- **构建工具**：Vite 5
- **UI 组件库**：Vant 4
- **路由**：Vue Router 4
- **HTTP 客户端**：Axios
- **AQI 计算**：自定义工具函数，符合 HJ 633-2012 标准

## 项目结构

```
nepg-grid/
├── src/
│   ├── api/              # 接口封装
│   │   └── grid.js       # 网格员相关接口
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── utils/            # 工具函数
│   │   ├── request.js    # Axios 封装
│   │   └── aqi.js        # AQI 计算工具
│   ├── views/            # 页面组件
│   │   ├── Login.vue     # 登录页
│   │   ├── TaskList.vue  # 任务列表页
│   │   └── TaskDetail.vue # 任务详情页
│   ├── App.vue
│   └── main.js
├── index.html
├── vite.config.js
└── package.json
```

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## 接口说明

后端接口基础地址：`http://localhost:8080/api`

### 1. 网格员登录

- **接口**：`POST /auth/grid/login`
- **参数**：`{ username, password }`
- **返回**：token、网格员信息

### 2. 获取任务列表

- **接口**：`GET /grid/tasks/list`
- **参数**：`gridMemberId, pageNum, pageSize`
- **返回**：分页任务列表

### 3. 获取任务详情

- **接口**：`GET /grid/tasks/{id}`
- **返回**：任务详细信息

### 4. 提交实测数据

- **接口**：`POST /grid/measure/confirm`
- **参数**：`{ feedbackId, gridMemberId, aqiValue, pm25, pm10, so2, no2, co, o3, confirmResult }`
- **返回**：提交结果

## 测试账号

| 账号 | 密码 | 负责区域 |
|------|------|----------|
| grid01 | 123456 | 北京市朝阳区望京街道 |
| grid02 | 123456 | 北京市海淀区中关村街道 |
| grid03 | 123456 | 大连市沙河口区 |

## AQI 计算标准

根据《环境空气质量指数（AQI）技术规定（试行）》HJ 633-2012：

| AQI 范围 | 等级 | 颜色 |
|---------|------|------|
| 0 ~ 50 | 优 | #00e400 |
| 51 ~ 100 | 良 | #ffff00 |
| 101 ~ 150 | 轻度污染 | #ff7e00 |
| 151 ~ 200 | 中度污染 | #ff0000 |
| 201 ~ 300 | 重度污染 | #99004c |
| 301 ~ 500 | 严重污染 | #7e0023 |

参与评价的污染物：SO₂、NO₂、PM10、PM2.5、CO、O₃
