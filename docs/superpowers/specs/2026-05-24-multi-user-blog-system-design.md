# 多用户博客系统设计

日期：2026-05-24  
主题：multi-user-blog-system

## 1. 目标与范围

本项目目标是构建一个简洁技术风格的多用户博客系统，整体视觉参考 johng.cn 的“干净、克制、强调阅读体验”的设计取向，但不直接复制其实现。

本次设计面向 MVP，支持三类角色：游客、作者、管理员。

### 包含范围
- 游客可浏览公开文章、分类筛选、搜索、查看文章详情、查看作者主页
- 作者可登录、管理自己的文章、使用 Markdown 编辑器写作、上传图片、设置分类与标签、维护个人主页
- 管理员可管理用户、文章、分类、标签，并查看基础统计数据
- 系统采用前后端分离架构，但在单仓库 Monorepo 中统一管理
- 系统默认面向 Docker Compose 部署
- 图片采用对象存储方案

### 不包含范围
- 用户公开注册
- 评论、点赞、收藏、社交分享
- 复杂审核流
- 草稿箱、多版本历史、定时发布
- 多租户与团队协作能力

**Why:** 当前目标是优先交付一个可运行、可维护、简洁可用的多用户博客平台，避免功能蔓延。  
**How to apply:** 后续实现应严格围绕博客发布、浏览和后台管理展开，不额外引入未确认能力。

## 2. 总体架构

采用单仓库 Monorepo + 前后端分离结构：

```text
project-root/
├─ frontend/           # Vue 3 + Vite + Tailwind CSS
├─ backend/            # Spring Boot
├─ docker/             # Nginx、部署相关配置
├─ docker-compose.yml  # 服务编排
├─ .env.example        # 环境变量模板
└─ README.md
```

### 前端职责
- 提供前台博客阅读体验
- 提供登录页、作者工作区、管理员后台
- 集成 Markdown 编辑器
- 处理图片上传交互
- 展示文章渲染结果和高亮样式

### 后端职责
- 提供认证与 JWT 签发
- 提供用户、文章、分类、标签、上传、统计 API
- 承担权限校验
- 统一完成 Markdown 到 HTML 的渲染与清洗
- 负责 PostgreSQL 持久化与对象存储接入

### 部署职责
Docker Compose 统一编排以下服务：
- frontend
- backend
- postgres
- minio
- nginx

## 3. 技术选型

### 前端
- Vue 3
- Vite
- TypeScript
- Tailwind CSS
- Vue Router
- Pinia
- Axios
- Markdown 编辑器：MdEditorV3 或同类成熟方案

### 后端
- Spring Boot 3
- Spring Web
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Lombok
- Bean Validation
- MinIO Java SDK
- OpenAPI / Swagger

### 存储
- PostgreSQL：业务数据
- MinIO：图片对象存储，本地与部署环境保持一致

## 4. 角色与权限模型

### 游客
- 访问文章列表
- 分类筛选
- 搜索公开文章
- 查看文章详情
- 查看作者主页

### 作者（AUTHOR）
- 登录系统
- 管理自己的文章
- 发布、编辑、删除自己的文章
- 上传图片
- 更新个人资料

### 管理员（ADMIN）
- 创建作者账号
- 启用/禁用用户
- 管理所有文章
- 管理分类与标签
- 查看站点基础统计

### 账户状态
- ENABLED
- DISABLED

规则：DISABLED 用户在登录阶段直接被拒绝。

## 5. 内容发布策略

### 账号创建
仅管理员创建账号，不开放公开注册。

### 发布流程
作者发布后直接公开，不经过审核。

### 图片策略
图片上传通过后端接口进入对象存储，并在数据库中保留上传记录。

### 登录态存储
MVP 阶段前端将 JWT 保存于 localStorage，以换取更低的联调复杂度。

## 6. 后端模块设计

建议以业务模块为核心组织代码：

```text
backend/
├─ src/main/java/.../
│  ├─ common/
│  ├─ config/
│  ├─ security/
│  ├─ auth/
│  ├─ user/
│  ├─ post/
│  ├─ category/
│  ├─ tag/
│  ├─ upload/
│  ├─ stats/
│  └─ BlogApplication.java
```

### 模块职责
- `auth`：登录、JWT 签发、当前用户识别
- `user`：用户资料、后台用户管理
- `post`：文章 CRUD、公开列表、详情、搜索
- `category`：分类管理与公开分类查询
- `tag`：标签管理
- `upload`：图片上传、上传记录保存
- `stats`：管理员基础统计数据
- `security`：鉴权、权限注解、JWT 过滤器、安全配置
- `common`：统一响应结构、异常体系、工具类

## 7. 前端模块设计

```text
frontend/
├─ src/
│  ├─ api/
│  ├─ assets/
│  ├─ components/
│  ├─ layouts/
│  ├─ router/
│  ├─ stores/
│  ├─ utils/
│  ├─ views/
│  │  ├─ public/
│  │  ├─ user/
│  │  └─ admin/
│  ├─ App.vue
│  └─ main.ts
```

### 布局设计
- `PublicLayout`：前台博客阅读布局
- `UserLayout`：作者工作区布局
- `AdminLayout`：后台管理布局

### 页面设计
#### 公共前台
- `/` 首页
- `/posts/:slug` 文章详情
- `/categories` 分类列表
- `/categories/:slug` 分类文章列表
- `/search` 搜索结果
- `/authors/:username` 作者主页

#### 认证
- `/login`

#### 作者工作区
- `/me`
- `/me/posts`
- `/me/posts/create`
- `/me/posts/:id/edit`

#### 管理后台
- `/admin`
- `/admin/users`
- `/admin/posts`
- `/admin/categories`
- `/admin/tags`

## 8. 数据模型

### users
- id
- username
- password_hash
- nickname
- email
- avatar_url
- bio
- role
- status
- created_at
- updated_at

### posts
- id
- title
- slug
- summary
- content_markdown
- content_html
- cover_image_url
- author_id
- category_id
- status
- published_at
- created_at
- updated_at

### categories
- id
- name
- slug
- description
- created_at
- updated_at

### tags
- id
- name
- slug
- created_at
- updated_at

### post_tags
- post_id
- tag_id

### uploads
- id
- file_name
- original_name
- content_type
- file_size
- storage_key
- url
- uploader_id
- created_at

### 关系
```text
users 1 --- n posts
categories 1 --- n posts
posts n --- n tags   (through post_tags)
users 1 --- n uploads
```

## 9. 接口设计

### 公开接口 `/api/public/*`
- `GET /api/public/posts`
- `GET /api/public/posts/{slug}`
- `GET /api/public/categories`
- `GET /api/public/categories/{slug}/posts`
- `GET /api/public/authors/{username}`
- `GET /api/public/authors/{username}/posts`

### 认证接口 `/api/auth/*`
- `POST /api/auth/login`

### 作者接口 `/api/me/*`
- `GET /api/me/profile`
- `PUT /api/me/profile`
- `GET /api/me/posts`
- `POST /api/me/posts`
- `PUT /api/me/posts/{id}`
- `DELETE /api/me/posts/{id}`
- `POST /api/me/uploads/images`

### 管理员接口 `/api/admin/*`
- `GET /api/admin/users`
- `POST /api/admin/users`
- `PUT /api/admin/users/{id}`
- `PATCH /api/admin/users/{id}/status`
- `GET /api/admin/posts`
- `PUT /api/admin/posts/{id}`
- `DELETE /api/admin/posts/{id}`
- `GET /api/admin/categories`
- `POST /api/admin/categories`
- `PUT /api/admin/categories/{id}`
- `DELETE /api/admin/categories/{id}`
- `GET /api/admin/tags`
- `POST /api/admin/tags`
- `PUT /api/admin/tags/{id}`
- `DELETE /api/admin/tags/{id}`
- `GET /api/admin/stats`

## 10. Markdown 渲染策略

系统同时保存：
- `content_markdown`
- `content_html`

### 设计原则
- 编辑时以前端 Markdown 编辑器为主
- 保存时由后端统一把 Markdown 渲染为 HTML
- 渲染后执行 HTML 清洗，避免 XSS
- 展示时前端直接消费 HTML 并提供样式容器

**Why:** 这样兼顾可编辑性、展示性能与渲染一致性。  
**How to apply:** 不要把 Markdown 渲染职责分散到前后端两边；统一由后端负责生成展示用 HTML。

## 11. UI / UX 设计原则

### 风格方向
参考 johng.cn 的整体气质，但不复制具体视觉实现：
- 简洁
- 留白充足
- 内容优先
- 层级清晰
- 技术博客阅读友好

### 前台重点
- 首页文章列表清爽
- 文章详情排版舒适
- 代码块高亮清晰
- 表格和图片自适应
- 导航简洁，不堆砌功能

### 后台重点
- 结构直接
- 操作路径短
- 不追求重型后台框架风格

## 12. 安全与边界控制

MVP 必须包含：
- BCrypt 密码加密
- JWT 鉴权
- 后端角色与资源归属校验
- 参数校验
- HTML 清洗防止 XSS
- 上传文件类型与大小限制
- CORS 白名单

### 权限边界
- 游客仅访问公开接口
- 作者只能管理自己的文章
- 管理员可管理全站资源
- 前端路由守卫只负责体验，安全必须以后端为准

## 13. 统计能力

管理员仪表盘提供基础统计：
- 用户总数
- 文章总数
- 分类总数
- 标签总数

本阶段不加入复杂趋势分析、访问日志报表或行为埋点。

## 14. 部署设计

默认采用 Docker Compose：
- `frontend`
- `backend`
- `postgres`
- `minio`
- `nginx`

### 环境变量
前端：
- `VITE_API_BASE_URL`

后端：
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRE_SECONDS`
- `MINIO_ENDPOINT`
- `MINIO_ACCESS_KEY`
- `MINIO_SECRET_KEY`
- `MINIO_BUCKET`
- `CORS_ALLOWED_ORIGINS`

## 15. 测试策略

### 后端
- service 层单元测试
- controller 层接口测试
- 权限边界测试

### 前端
- 核心页面渲染测试
- 路由守卫测试
- 关键交互流程测试

### 手工验证重点
- 登录流程
- 发文与编辑流程
- 图片上传
- 管理员管理文章与用户
- 文章详情 Markdown 展示效果

## 16. 分阶段交付建议

用户已确认不必一次性交付，因此建议按阶段推进：

### 阶段 1：基础骨架
- Monorepo 初始化
- 前端基础布局
- Spring Boot 基础工程
- PostgreSQL / MinIO / Docker Compose
- 登录认证与基础权限框架

### 阶段 2：博客核心功能
- 公开文章列表
- 文章详情
- 分类筛选
- 搜索
- 作者文章 CRUD
- Markdown 编辑与图片上传

### 阶段 3：后台管理
- 管理员仪表盘
- 用户管理
- 全站文章管理
- 分类/标签管理

## 17. 成功标准

当满足以下条件时，MVP 视为达成：
- 游客可顺畅浏览和搜索公开文章
- 作者可完成登录、发文、编辑、删除自己的文章
- 管理员可完成用户与文章管理
- Markdown 内容可稳定渲染并具备代码高亮与图片适配
- 系统可通过 Docker Compose 启动
- README 足以指导他人独立运行项目
