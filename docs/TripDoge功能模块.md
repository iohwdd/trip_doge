# TripDoge 功能模块文档

## 项目介绍

**TripDoge** 是一个基于AI的智能对话平台，支持多角色AI对话、文档知识库问答、用户管理等功能。用户可以与不同性格的AI角色进行对话，上传文档构建知识库，实现智能问答。

## 技术栈概览

### 后端技术栈

- **框架**: Spring Boot 3.3.2
- **语言**: Java 21
- **数据库**: MySQL + PostgreSQL
- **缓存**: Redis
- **AI引擎**: LangChain4j 1.5.0 + 阿里云DashScope
- **文档处理**: Apache Tika
- **存储**: MinIO
- **邮件服务**: Spring Boot Mail
- **API文档**: SpringDoc OpenAPI 3.0
- **数据映射**: MapStruct
- **加密**: Spring Security Crypto

### 系统架构图

```text
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端应用      │────│  Spring Boot    │────│   Controller    │
│                 │    │   Web Server    │    │     层          │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                              ┌─────────┼─────────┐
                                              │         │         │
                                              ▼         ▼         ▼
                                    ┌─────────────────┐ │ ┌─────────────────┐
                                    │  UserService    │ │ │  ChatService    │
                                    │  RoleService    │ │ │  EmailService   │
                                    │  ConversationSrv│ │ │     etc...      │
                                    └─────────────────┘ │ └─────────────────┘
                                              │         │         │
        ┌─────────────────────────────────────┼─────────┼─────────┼─────────────────────────┐
        │                                     │         │         │                         │
        ▼                                     ▼         ▼         ▼                         ▼
┌─────────────────┐               ┌─────────────────┐     ┌─────────────────┐   ┌─────────────────┐
│     Redis       │               │     MySQL       │     │  PostgreSQL     │   │     MinIO       │
│                 │               │                 │     │   (向量数据)    │   │   (文件存储)    │
│ • Session存储   │               │ • 用户数据      │     │ • 文档向量      │   │ • 上传文档      │
│ • 缓存          │               │ • 对话历史      │     │ • 嵌入数据      │   │                 │
└─────────────────┘               │ • 角色配置      │     └─────────────────┘   └─────────────────┘
                                  └─────────────────┘
                                              │
                                              ▼
                               ┌─────────────────┐
                               │   LangChain4j   │
                               │     + AI        │
                               │   DashScope     │
                               └─────────────────┘
```

## 功能模块详述

### 1. 用户管理模块

**功能描述**: 提供用户注册、登录、认证等基础用户功能

#### 主要功能

- 用户注册（邮箱验证）
- 用户登录/登出
- 用户信息管理
- Redis会话管理

#### 实现架构

```text
请求 → UserController → UserService/EmailService → MySQL
                  ↓
            UserSessionService → Redis
```

#### 核心组件

- `UserController`: 用户相关接口
- `UserService/UserServiceImpl`: 用户业务逻辑
- `EmailService/EmailServiceImpl`: 邮件发送服务
- `UserSessionService`: Redis会话管理
- `TokenUtils`: Token处理工具

#### 流程说明

```text
注册: 填写信息 → 邮箱验证码 → 验证通过 → 创建用户
登录: 用户认证 → 创建Session → 返回Token
会话: Token验证 → Redis获取用户信息 → 自动续期
```

---

### 2. AI对话模块

**功能描述**: 核心AI对话功能，支持实时流式对话和多角色切换

#### 主要功能

- SSE流式AI对话
- 多角色对话支持
- 对话历史管理
- 会话持续性
- 对话重置功能

#### 实现架构

```text
ChatController → ChatService → AssistantService → LangChain4j → DashScope
       ↓              ↓              ↓
   用户验证    ConversationService   PersistentChatMemoryStore
       ↓              ↓              ↓
   SSE响应         MySQL存储      CustomerChatMemoryProvider
```

#### 核心组件

- `ChatController`: 对话接口控制器
- `ChatService/ChatServiceImpl`: 对话业务逻辑
- `AssistantService`: AI助手服务
- `ConversationService`: 会话管理
- `PersistentChatMemoryStore`: 持久化记忆存储
- `CustomerChatMemoryProvider`: 自定义记忆提供者

#### 流程说明

```text
对话请求 → 用户身份验证 → 获取/创建会话 → 加载历史 →
调用AI模型 → SSE流式响应 → 保存对话记录
```

---

### 3. 角色管理模块

**功能描述**: 管理不同的AI角色，每个角色有不同的性格和专业领域

#### 主要功能

- 角色列表展示
- 用户-角色会话管理
- 角色配置管理

#### 实现架构

```text
RoleController → RoleService → MySQL(roles表)
       ↓              ↓
   会话查询    ConversationService → 创建/获取会话
```

#### 核心组件

- `RoleController`: 角色接口控制器
- `RoleService/RoleServiceImpl`: 角色业务逻辑
- `ConversationService/ConversationServiceImpl`: 会话管理服务

#### 流程说明

```text
获取角色列表 → 用户选择角色 → 检查现有会话 →
创建/加载会话 → 返回会话信息
```

---

### 4. 文档管理模块

**功能描述**: 处理文档上传、解析、向量化，构建知识库支持RAG问答

#### 主要功能

- 多格式文档上传（PDF、Word、TXT等）
- Apache Tika文档解析
- 文档向量化处理
- MinIO文件存储
- 文档列表查询与管理
- 文档下载与删除
- 向量数据级联删除

#### 实现架构

```text
DocController → MinIO存储 + Apache Tika解析
       ↓              ↓
   文件上传     EmbeddingStoreIngestor → PostgreSQL向量存储
       ↓              ↓
   向量化处理    LangChain4j文档处理 + VectorDataService
```

#### 核心组件

- `DocController`: 文档接口控制器
- `FileUploadUtils`: 文件上传工具类
- `EmbeddingStoreIngestor`: 向量化处理器
- `ApacheTikaDocumentParser`: 文档解析器
- `VectorDataService`: 向量数据管理服务

#### API接口详情

**1. 文档上传解析 (POST /api/doc/parse)**
- 功能：上传文档文件并进行解析，提取内容并向量化存储
- 参数：file (文件), roleId (角色ID), docName (文档名称)
- 返回：文档ID、解析状态、向量数量等信息

**2. 查询文档列表 (POST /api/doc/list)**
- 功能：分页查询用户的文档列表
- 参数：roleId, pageNum, pageSize, docName (可选)
- 返回：分页文档列表，包含文档基本信息

**3. 下载文档 (POST /api/doc/download)**
- 功能：下载指定的文档文件
- 参数：docId (文档ID)
- 返回：文件流下载

**4. 删除文档 (POST /api/doc/delete)**
- 功能：删除指定文档及其关联的向量数据
- 参数：docId (文档ID)
- 返回：删除结果，包含删除的向量数量

#### 流程说明

```text
文档上传 → MinIO存储 → Apache Tika解析 →
文档分块 → 向量化 → PostgreSQL存储 → RAG检索

文档删除 → 删除数据库记录 → 删除MinIO文件 →
删除关联向量数据 → 返回删除结果
```

### 数据库架构

```text
MySQL (主数据库)
├── users (用户信息)
├── roles (AI角色配置)
├── conversations (用户-角色会话)
└── chat_history (对话记录)

PostgreSQL (向量数据库)
├── 文档向量存储
└── 嵌入数据

Redis (缓存和会话)
├── 用户会话信息
└── 系统缓存数据
```

## API接口概览

### 用户管理 (`UserController`)

```text
POST /api/user/register     # 用户注册
POST /api/user/login        # 用户登录
POST /api/user/logout       # 用户登出
POST /api/user/info         # 获取用户信息
POST /api/user/sendEmail    # 发送验证码
```

### AI对话 (`ChatController`)

```text
POST /api/chat/{roleId}           # SSE流式对话
POST /api/chat/{roleId}/reset     # 重置对话
POST /api/chat/{roleId}/history   # 获取历史记录
```

### 角色管理 (`RoleController`)

```text
POST /api/roles/list        # 获取角色列表
```

### 文档管理 (`DocController`)

```text
POST /api/doc/parse         # 文档上传解析
POST /api/doc/list          # 查询文档列表
POST /api/doc/download      # 下载文档
POST /api/doc/delete        # 删除文档
```

## 部署架构

### 应用配置

```text
Spring Boot Application (Port: 7979)
├── MySQL 数据库连接
├── PostgreSQL 向量数据库
├── Redis 缓存和会话存储
├── MinIO 对象存储
├── DashScope AI服务
└── SMTP 邮件服务
```

### 环境配置

```text
开发环境:
- Spring Boot内置Tomcat
- MySQL单机部署
- Redis单机部署
- MinIO单机部署

生产环境:
- 容器化部署
- 数据库集群
- Redis集群
- 负载均衡
```

## 技术特性

### 核心技术栈

- **Web框架**: Spring Boot 3.3.2 + Spring Web
- **数据访问**: MyBatis + HikariCP连接池
- **缓存**: Redis + Spring Session
- **AI能力**: LangChain4j + DashScope
- **文档处理**: Apache Tika
- **对象存储**: MinIO
- **API文档**: SpringDoc OpenAPI

---

**项目维护**: TripDoge开发团队
**文档版本**: v1.0
**更新时间**: 2025年9月28日
