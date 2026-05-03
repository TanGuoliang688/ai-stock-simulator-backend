# AI 模拟炒股大师 (AI Stock Simulator)

一个基于 Spring Boot + React 的智能模拟炒股平台，集成 AI 助手提供专业投资建议。

## ✨ 功能特性

### 核心功能
- 👤 **用户认证**：注册/登录、JWT Token 无状态认证
- 📊 **股票行情**：实时搜索、列表展示、K 线图分析
-  **模拟交易**：买入/卖出、持仓管理、自动计算手续费/印花税
-  **资产分析**：总资产曲线、每日盈亏、持仓分布饼图
- 📋 **交易记录**：历史交易明细查询与统计
- 🤖 **AI 智能助手**：
  - 智能选股推荐
  - 个性化交易建议
  - 实时市场分析
  - 持仓风险评估

### 技术亮点
- ⚡ **实时价格模拟**：每 3 秒自动波动
- 🎨 **Markdown 渲染**：AI 分析内容格式美观
-  **响应式设计**：完美适配桌面端
- 🔒 **类型安全**：前端完整的 TypeScript 定义

## 🛠️ 技术栈

### 后端
- Java 21
- Spring Boot 3.4.6
- Spring Security + JWT
- MyBatis-Plus 3.5.5
- MySQL 8.0+
- Redis 5.0+
- RabbitMQ 3.12+
- 通义千问 API（AI 服务）

### 前端
- React 18.2
- TypeScript 5.x
- Vite 5.x
- Ant Design 5.x
- Axios
- Zustand
- ECharts
- ReactMarkdown

## 📋 环境要求

- JDK 21+
- Node.js 20.19+
- MySQL 8.0+
- Redis 5.0+
- RabbitMQ 3.12+

##  快速开始

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/ai-stock-simulator.git
cd ai-stock-simulator
```

### 2. 创建数据库

```sql
CREATE DATABASE ai-stock-simulator DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置后端
编辑 `src/main/resources/application-dev.yml`：

```
spring: 
	datasource: url: jdbc:mysql://localhost:3306/ai-stock-simulator?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai 
				username: root 
				password: your_password

ai: 
	qwen: 
		api-key: your_qwen_api_key_here
```

### 4. 启动后端

```bash
cd ai-stock-simulator-backend 
mvn clean install 
mvn spring-boot:run
```

后端地址：http://localhost:8081/api

### 5. 启动前端

```bash
cd ai-stock-simulator-frontend 
npm install 
npm run dev
```

前端地址：http://localhost:5173

## 📚 API 文档

启动后端后访问：http://localhost:8081/api/swagger-ui.html

## 🐳 Docker 部署

### 构建镜像

```bash
docker-compose up -d
```

### 服务访问
- 前端：http://localhost:80
- 后端：http://localhost:8081
- Swagger：http://localhost:8081/api/swagger-ui.html

## 📦 项目结构

ai-stock-simulator/ 

├── ai-stock-simulator-backend/ # 后端服务

│ ├── src/main/java/ 

│ │ ├── controller/ # REST API 控制器 

│ │ ├── service/ # 业务逻辑 

│ │ ├── mapper/ # 数据库访问 

│ │ ├── entity/ # 实体类 

│ │ ├── security/ # 安全配置 

│ │ └── task/ # 定时任务 

│ ── src/main/resources/ # 配置文件 

├── ai-stock-simulator-frontend/ # 前端应用 

│ ├── src/ 

│ │ ├── pages/ # 页面组件 

│ │ ├── components/ # 通用组件 

│ │ ├── services/ # API 服务 

│ │ ├── stores/ # 状态管理 

│ │ └── types/ # 类型定义 

│ └── public/ # 静态资源 

└── README.md

## 📝 数据库表

- `user`：用户信息
- `stock`：股票基本信息
- `price_history`：历史价格
- `trade_order`：交易订单
- `position`：持仓信息
- `trade_record`：交易记录
- `watchlist`：自选股

## 🔐 安全说明

- JWT Token 有效期：15 分钟
- 密码使用 BCrypt 加密
- 所有敏感接口需要认证
- API Key 保存在环境变量中

## 📄 许可证

MIT License

## 👨💻 作者

- Alex-[GitHub]([TanGuoliang688 (谭国良)](https://github.com/TanGuoliang688))
- 营养快线-[Gitee]([TanGuoliang688 (谭国良)]([大轴 (major-axis) - Gitee.com](https://gitee.com/major-axis))

---

**如果觉得这个项目有帮助，欢迎 Star ⭐！**