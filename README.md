# QIMS - 质检管理系统

基于国家质检体系的产品质量检测管理系统。

## 技术栈

### 前端

- Vue 3
- Vite
- Element Plus
- Vue Router
- Axios
- ECharts

### 后端

- Spring Boot 3.2.4
- MyBatis
- MySQL 8.0
- JWT
- iText PDF

## 项目结构

```
QIMS/
├── qims-frontend/    # 前端工程
│   ├── src/
│   │   ├── views/    # 页面组件
│   │   ├── router/   # 路由配置
│   │   └── utils/    # 工具函数
│   └── package.json
└── qims-backend/     # 后端工程
    ├── src/main/
    │   ├── java/com/young/
    │   │   ├── controller/  # 控制器
    │   │   ├── service/     # 服务层
    │   │   ├── mapper/      # 数据访问层
    │   │   ├── pojo/        # 实体类
    │   │   ├── config/      # 配置类
    │   │   ├── interceptor/ # 拦截器
    │   │   └── utils/       # 工具类
    │   └── resources/
    │       ├── sql/         # 数据库脚本
    │       └── application.yml
    └── pom.xml
```

## 功能模块

### 用户角色

- **管理员** - 系统管理、用户管理、标准管理
- **检验员** - 样品接收、检验执行、记录录入
- **客户** - 委托提交、报告查询

### 核心功能

- 用户认证与授权（JWT）
- 委托管理
- 样品任务管理
- 检验记录管理
- PDF报告生成
- 标准管理
- 检验项目管理
- 客户管理
- 数据看板（ECharts）

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 数据库配置

1. 创建数据库

```sql
CREATE DATABASE qims_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

1. 执行初始化脚本

```bash
# 文件位置：qims-backend/src/main/resources/sql/db_schema.sql
```

1. 修改数据库连接配置

```yaml
# 文件位置：qims-backend/src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/qims_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 后端启动

```bash
cd qims-backend
mvn spring-boot:run
```

后端服务地址：<http://localhost:8080>

### 前端启动

```bash
cd qims-frontend
npm install
npm run dev
```

前端服务地址：<http://localhost:5173>

## 默认账号

数据库初始化后可使用以下账号登录（密码统一为：**123456**）：

| 角色  | 账号        |
| --- | --------- |
| 管理员 | admin     |
| 检验员 | inspector |
| 客户1 | client1   |
| 客户2 | client2   |
| 客户3 | client3   |

## License

MIT License
