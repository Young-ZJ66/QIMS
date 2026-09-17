# QIMS - 食品质检管理系统

基于国家质检体系的产品质量检测管理系统，实现从委托提交、盲样派发、检验录入到报告签发的全流程管理。

## 技术栈

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue 3 | 3.5+ | Composition API |
| Vite | 8.0+ | 构建工具 |
| Element Plus | 2.13+ | UI 组件库（中文国际化） |
| Vue Router | 4.6+ | 路由管理（声明式角色守卫） |
| Axios | 1.15+ | HTTP 客户端（JWT 自动续期） |
| ECharts | 6.0+ | 数据可视化 |

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.4 | 应用框架 |
| MyBatis | 3.0.3 | ORM 框架（XML 映射） |
| MySQL | 8.0+ | 数据库 |
| JWT (jjwt) | 0.11.5 | 认证令牌（带 issuer 校验） |
| BCrypt (jbcrypt) | 0.4 | 密码加密 |
| iText PDF | 5.5.13.3 | PDF 报告生成 |
| PageHelper | 1.4.7 | 分页插件 |
| Spring AOP | - | @RequireRole 角色权限切面 |
| Knife4j | 4.4.0 | OpenAPI3 接口文档 |
| Spring Actuator | - | 健康检查端点 |

## 项目结构

```
QIMS/
├── qims-frontend/                # 前端工程
│   ├── src/
│   │   ├── views/
│   │   │   ├── login/           # 登录页 + 客户注册
│   │   │   ├── layout/          # 主布局（侧边栏 + 头部）
│   │   │   ├── dashboard/       # 数据看板（ECharts）
│   │   │   ├── client/          # 客户：委托提交、报告查询
│   │   │   ├── admin/           # 管理员：收样派发、报告审核
│   │   │   ├── inspector/       # 检验员：检测任务
│   │   │   ├── sys/             # 系统管理：标准、客户、用户
│   │   │   └── error/           # 404 页面
│   │   ├── components/          # 公共组件（NotificationBell）
│   │   ├── router/              # 路由配置（角色守卫）
│   │   └── utils/               # 工具函数（request, format）
│   └── package.json
└── qims-backend/                 # 后端工程
    ├── src/main/
    │   ├── java/com/young/
    │   │   ├── controller/      # 12 个 REST 控制器
    │   │   ├── service/         # 8 个服务接口 + 实现
    │   │   ├── mapper/          # 10 个 MyBatis Mapper
    │   │   ├── pojo/            # 10 个实体类 + 5 个枚举
    │   │   ├── common/          # Result、BusinessException、GlobalExceptionHandler
    │   │   ├── config/          # WebMvc、UploadPath、OpenApi 配置
    │   │   ├── interceptor/     # JWT 登录拦截器
    │   │   ├── annotation/      # @RequireRole 自定义注解
    │   │   ├── aspect/          # 权限校验 AOP 切面
    │   │   └── utils/           # JwtUtils、PasswordUtils、PdfReportHelper、NotificationHelper
    │   └── resources/
    │       ├── sql/db_schema.sql   # 数据库初始化脚本（含索引、外键、种子数据）
    │       └── application.yml     # 应用配置（支持环境变量覆盖）
    └── pom.xml
```

## 功能模块

### 用户角色

| 角色 | roleId | 权限范围 |
|------|--------|----------|
| 管理员 | 1 | 全部功能：收样派发、报告审核签发、系统管理 |
| 检验员 | 2 | 查看分配任务、录入检测数据 |
| 客户 | 3 | 提交委托单、查看自己的报告 |

### 核心业务流程

```
客户提交委托 → 管理员收样生成盲样 → 检验员录入数据（系统自动判定）→ 管理员审核签发PDF报告 → 客户查看报告
```

### 功能清单

- **认证授权** — JWT 登录、静默 Token 续期、BCrypt 密码加密、登录失败锁定
- **委托管理** — 客户提交、管理员收样、状态流转
- **盲样机制** — 检验员看不到客户信息，保证检验公正性
- **自动判定** — 支持范围、上限、下限、定性四种判定类型
- **PDF 报告** — 自动生成带签章的检验报告
- **数据看板** — 趋势图、缺陷分析、KPI 卡片、实时动态
- **通知系统** — 委托提交/任务分配/报告签发自动通知
- **系统管理** — 检验标准库、客户管理、员工管理
- **操作日志** — 关键业务操作审计追踪

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 数据库初始化

```sql
-- 1. 创建数据库
CREATE DATABASE qims_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 执行初始化脚本（含表结构、索引、外键、种子数据）
-- 文件位置：qims-backend/src/main/resources/sql/db_schema.sql
```

### 后端配置

数据库连接等敏感信息支持通过环境变量覆盖（生产环境必须配置）：

```bash
# 环境变量（可选，有默认值）
export DB_URL=jdbc:mysql://localhost:3306/qims_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=your-strong-secret-key-at-least-32-chars
export CORS_ORIGINS=http://your-frontend-domain:3000
export UPLOAD_DIR=/data/qims/uploads
```

也可直接修改 `application.yml`（开发环境）：

```yaml
spring:
  datasource:
    username: root
    password: 1234
```

### 后端启动

```bash
cd qims-backend
mvn spring-boot:run
```

- 后端地址：http://localhost:8080
- API 文档：http://localhost:8080/doc.html
- 健康检查：http://localhost:8080/actuator/health

### 前端启动

```bash
cd qims-frontend
npm install
npm run dev
```

- 前端地址：http://localhost:3000

## 默认账号

密码统一为：**123456**

| 角色 | 账号 | 说明 |
|------|------|------|
| 管理员 | admin | 系统管理、收样派发、报告审核 |
| 检验员 | inspector | 检测任务录入 |
| 检验员 | inspector2 | 检测任务录入 |
| 客户 | client1 | 绿源生鲜食品有限公司 |
| 客户 | client2 | 香满园肉制品有限公司 |
| 客户 | client3 | 伊康乳业集团有限公司 |

## 数据库设计

### 表结构概览（11 张表）

| 表名 | 说明 | 关键特性 |
|------|------|----------|
| sys_user | 内部用户 | BCrypt 密码、登录锁定、审计字段 |
| sys_client | 客户企业 | 统一社会信用代码、审计字段 |
| sys_operate_log | 操作日志 | 索引优化、IP 记录 |
| sys_notification | 系统通知 | 未读计数、业务关联 |
| sys_config | 系统配置 | 运行时可调参数 |
| std_standard | 检验标准 | 发布/实施日期、软删除 |
| std_inspection_item | 检验项目 | 精度提升 decimal(14,6) |
| biz_delegation | 委托单 | 优先级、期望完成日期、复合索引 |
| biz_sample_task | 盲样任务 | 外键约束、状态索引 |
| biz_inspection_record | 检验记录 | 外键约束、精度提升 |
| biz_report | 检验报告 | 版本号、历史报告关联 |

### 设计特性

- **索引优化** — 15+ 个索引覆盖高频查询字段
- **外键约束** — 所有表间关系强制引用完整性
- **审计字段** — update_time、created_by、updated_by
- **软删除** — deleted_at 字段，数据不物理删除
- **乐观锁预留** — version 字段可扩展

## API 端点

| 控制器 | 路径 | 说明 |
|--------|------|------|
| AuthController | /api/auth | 登录、注册、刷新 Token |
| BizDelegationController | /api/biz-delegation | 委托管理（支持 ?status= 过滤） |
| BizSampleTaskController | /api/biz-sample-task | 盲样任务管理 |
| BizInspectionRecordController | /api/biz-inspection-record | 检验记录（批量提交+自动判定） |
| BizReportController | /api/biz-report | 报告管理 |
| DashboardController | /api/dashboard | 数据看板（SQL 聚合查询） |
| NotificationController | /api/notification | 通知列表、未读计数、标记已读 |
| StdStandardController | /api/std-standard | 检验标准 CRUD |
| StdInspectionItemController | /api/std-inspection-item | 检验项目 CRUD |
| SysUserController | /api/sys-user | 用户管理（支持 ?roleId= 过滤、分页） |
| SysClientController | /api/sys-client | 客户管理 |
| ProfileController | /api/profile | 个人信息、修改密码 |
| FileController | /api/file | 文件上传 |

## 安全特性

- JWT Token 认证，issuer 校验防止跨服务令牌混淆
- 短密钥拒绝（< 32 字节启动失败）
- BCrypt 密码加密（随机盐值）
- @RequireRole 声明式角色权限控制
- 登录失败次数限制
- 环境变量敏感配置（数据库密码、JWT 密钥）
- 全局异常处理器（不泄露内部信息）
- CORS 跨域配置

## License

MIT License