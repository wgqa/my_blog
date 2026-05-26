# 多用户博客系统

一个前后端分离的多用户博客 MVP，包含公开博客、作者工作区和 `/admin` 管理后台。

- 前端：Vue 3 + Vite + TypeScript + Tailwind CSS + Pinia + Vue Router
- 后端：Spring Boot 3 + Spring Security + Spring Data JPA + Flyway
- 数据库：PostgreSQL
- 对象存储：MinIO
- 鉴权：JWT（MVP 阶段保存在 `localStorage`）
- 部署：Docker Compose + Nginx

## 功能概览

### 公开博客
- 文章列表、文章详情
- 分类浏览
- 关键词搜索
- 作者主页
- Markdown 内容展示与代码高亮

### 作者工作区
- 用户登录
- 个人资料查看与更新
- 我的文章列表
- 新建、编辑、删除自己的文章
- Markdown 编辑器与图片上传

### 管理后台
- 仪表盘统计
- 作者账号创建与启用/禁用
- 全站文章管理
- 分类管理
- 标签管理

## 仓库结构

```text
.
├─ frontend/                # Vue 前端，包含公开站点、作者工作区、/admin
├─ backend/                 # Spring Boot 后端 API
├─ docker/                  # Dockerfile 与 Nginx 配置
├─ docker-compose.yml       # 本地一键启动编排
└─ .env.example             # 示例环境变量
```

## 环境要求

### 本地开发
- Node.js 20+
- npm 10+
- Java 17
- Maven Wrapper（仓库已提供 `backend/mvnw`）
- PostgreSQL 16（如不使用 Docker）
- MinIO（如不使用 Docker）

### Docker 部署
- Docker
- Docker Compose

## 环境变量

项目根目录提供了 `.env.example`，默认内容如下：

```env
VITE_API_BASE_URL=http://localhost:8080/api
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/blog
SPRING_DATASOURCE_USERNAME=blog
SPRING_DATASOURCE_PASSWORD=blog123
JWT_SECRET=change-this-secret-change-this-secret
JWT_EXPIRE_SECONDS=86400
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123456
ADMIN_NICKNAME=管理员
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=blog-assets
MINIO_PUBLIC_BASE_URL=http://localhost:9000/blog-assets
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost
```

建议复制一份作为自己的本地环境配置后再修改默认值。

## 本地开发启动

### 1. 准备数据库和对象存储

如果你不使用 Docker Compose，需要自己准备：

- PostgreSQL 数据库：
  - database: `blog`
  - username: `blog`
  - password: `blog123`
- MinIO：
  - endpoint: `http://localhost:9000`
  - access key: `minioadmin`
  - secret key: `minioadmin`
  - bucket: `blog-assets`

后端启动时会执行 Flyway migration，并按配置自动初始化默认管理员账号。

### 2. 启动后端

在 `backend/` 目录执行：

```bash
./mvnw spring-boot:run
```

后端默认端口：`8080`

可访问：
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`
- Swagger UI：`http://localhost:8080/swagger-ui/index.html`

### 3. 启动前端

在 `frontend/` 目录执行：

```bash
npm install
npm run dev
```

前端默认端口：`5173`

前端通过 `frontend/src/api/http.ts` 中的 `VITE_API_BASE_URL` 访问后端，默认指向：

```text
http://localhost:8080/api
```

### 4. 访问入口

- 公开博客首页：`http://localhost:5173`
- 登录页：`http://localhost:5173/login`
- 作者工作区：`http://localhost:5173/me`
- 管理后台：`http://localhost:5173/admin`

## Docker Compose 启动

项目根目录已经提供 `docker-compose.yml`，包含以下服务：

- `postgres`
- `minio`
- `backend`
- `frontend`
- `nginx`

### 1. 启动服务

在项目根目录执行：

```bash
docker compose up --build
```

### 2. 默认端口

- Nginx：`80`
- Frontend：`5173`
- Backend：`8080`
- PostgreSQL：`5432`
- MinIO API：`9000`
- MinIO Console：`9001`

### 3. 统一访问入口

Nginx 会做反向代理：

- `/` 转发到前端容器
- `/api/` 转发到后端容器
- `/swagger-ui/` 转发到后端 Swagger UI
- `/v3/api-docs/` 转发到后端 OpenAPI 文档

因此使用 Docker Compose 启动后，可以优先通过以下地址访问：

- 站点入口：`http://localhost`
- Swagger UI：`http://localhost/swagger-ui/index.html`

### 4. 停止服务

```bash
docker compose down
```

如果你希望同时清理容器卷数据，可以手动删除 `postgres-data/` 和 `minio-data/`。

## 默认管理员账号

系统启动时会根据后端配置自动初始化管理员账号：

- 用户名：`admin`
- 密码：`admin123456`
- 昵称：`管理员`

对应配置来源：
- `backend/src/main/resources/application.yml`
- `.env.example`

首次登录后建议尽快修改为你自己的安全密码。

## MinIO 使用说明

系统中的文章封面与编辑器上传图片会写入 MinIO，对象公开访问地址基于以下配置拼接：

- `MINIO_ENDPOINT`
- `MINIO_BUCKET`
- `MINIO_PUBLIC_BASE_URL`

默认配置为：

- MinIO API：`http://localhost:9000`
- MinIO Console：`http://localhost:9001`
- Bucket：`blog-assets`
- Public Base URL：`http://localhost:9000/blog-assets`

使用建议：

1. 确保 MinIO 中存在 `blog-assets` bucket。
2. 确保该 bucket 具备前端可读的访问策略，否则图片无法公开展示。
3. 如果你部署在服务器上，应把 `MINIO_PUBLIC_BASE_URL` 改成你的公网访问地址。

## 常用命令

### 前端

```bash
npm install --prefix frontend
npm run dev --prefix frontend
npm run build --prefix frontend
npm test --prefix frontend
```

### 后端

```bash
./mvnw -f backend/pom.xml spring-boot:run
./mvnw -f backend/pom.xml test
```

## 当前验证状态

已经完成的基础验证：

- 后端集成测试通过
- 前端关键测试通过
- 前端生产构建通过
- Docker Compose 容器成功拉起
- 已完成游客浏览、管理员建号、作者发文、文章展示、管理员禁用作者、管理员删除文章的完整联调

说明：
- Docker Compose 文件、Dockerfile 与 Nginx 配置已完成并验证可用。
- fresh Docker 环境默认没有业务分类和标签种子数据，联调时需要先准备分类与标签后再执行作者发文链路。

## 后续建议

如果你接下来要继续推进交付收口，建议按这个顺序：

1. 清理不需要提交的联调产物并检查 `git status`
2. 提交代码并创建 PR
3. 根据需要补充分类/标签初始化方案，优化首次启动体验
