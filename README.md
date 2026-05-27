# 序章

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
├─ docker-compose.yml       # Docker Compose 编排
└─ .env.example             # 环境变量模板
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

项目根目录提供了 `.env.example` 作为部署模板，请先复制为你自己的 `.env` 后再填写真实值：

```bash
cp .env.example .env
```

模板中的关键项：

```env
VITE_API_BASE_URL=/api
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/blog
SPRING_DATASOURCE_USERNAME=blog
SPRING_DATASOURCE_PASSWORD=replace-with-a-strong-db-password
JWT_SECRET=replace-with-a-long-random-jwt-secret-at-least-32-chars
ADMIN_PASSWORD=replace-with-a-strong-admin-password
MINIO_ACCESS_KEY=replace-with-minio-access-key
MINIO_SECRET_KEY=replace-with-minio-secret-key
MINIO_PUBLIC_BASE_URL=https://your-domain.example/blog-assets
CORS_ALLOWED_ORIGINS=https://your-domain.example
```

说明：
- `VITE_API_BASE_URL` 保持 `/api`，前端会通过同源 Nginx 入口访问后端。
- `MINIO_PUBLIC_BASE_URL` 应改成你的正式资源访问地址。
- `CORS_ALLOWED_ORIGINS` 应只填写正式站点域名。
- `.env.example` 只是模板，不要直接拿去上线。

## 本地开发启动

### 1. 准备数据库和对象存储

如果你不使用 Docker Compose，需要自己准备：

- PostgreSQL 数据库：
  - database: `blog`
  - username: `blog`
  - password: 自行配置
- MinIO：
  - endpoint: `http://localhost:9000`
  - access key: 自行配置
  - secret key: 自行配置
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

前端通过 `frontend/src/api/http.ts` 中的 `VITE_API_BASE_URL` 访问后端；本地直跑前端时，可按需将其配置为 `http://localhost:8080/api`，通过 Nginx 入口访问时则保持 `/api`。

### 4. 访问入口

- 公开博客首页：`http://localhost:5173`
- 登录页：`http://localhost:5173/login`
- 作者工作区：`http://localhost:5173/me`
- 管理后台：`http://localhost:5173/admin`

## Docker Compose 部署

项目根目录提供了面向单机部署的 `docker-compose.yml`，包含以下服务：

- `postgres`
- `minio`
- `backend`
- `frontend`
- `nginx`

其中只有 `nginx` 对外暴露端口；前端静态文件由前端构建容器产出后挂载给 Nginx，后端与数据服务都只走 Compose 内网。

### 1. 准备生产环境变量

在项目根目录执行：

```bash
cp .env.example .env
```

然后把 `.env` 中的数据库密码、JWT 密钥、管理员密码、MinIO 凭证、正式域名等全部改成真实值。

### 2. 启动服务

在项目根目录执行：

```bash
docker compose --env-file .env up -d --build
```

也可以通过环境变量切换配置文件：

```bash
APP_ENV_FILE=.env docker compose up -d --build
```

### 3. 对外端口

默认只暴露：

- Nginx：`80`

如果你在服务器上部署，建议只开放 `80/443`，其余服务保持内网访问。

`postgres` 与 `minio` 的容器凭据会直接读取 `.env` 中的同名变量，因此数据库和对象存储的初始账号密码会与后端运行配置保持一致。

### 4. 统一访问入口

Nginx 会负责：

- 直接提供前端静态资源
- 为前端路由刷新回退到 `index.html`
- `/api/` 反向代理到后端
- `/swagger-ui/` 与 `/v3/api-docs/` 代理到后端

因此使用 Docker Compose 启动后，可以优先通过以下地址访问：

- 站点入口：`http://localhost`
- Swagger UI：`http://localhost/swagger-ui/index.html`

### 5. 停止服务

```bash
docker compose down
```

如果你希望同时清理容器卷数据，可以手动删除 `postgres15-data/` 和 `minio-data/`。

## 默认管理员账号

系统启动时会根据后端配置自动初始化管理员账号，账号信息来自你自己的 `.env` 配置。

首次登录后建议尽快修改为你自己的安全密码。

## MinIO 使用说明

系统中的文章封面与编辑器上传图片会写入 MinIO，对象公开访问地址基于以下配置拼接：

- `MINIO_ENDPOINT`
- `MINIO_BUCKET`
- `MINIO_PUBLIC_BASE_URL`

部署到服务器时，请确认：

1. MinIO 中已存在 `blog-assets` bucket。
2. 该 bucket 具备前端可读的访问策略，否则图片无法公开展示。
3. `MINIO_PUBLIC_BASE_URL` 已改成你的正式资源访问地址。

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

### Docker

```bash
docker compose --env-file .env up -d --build
docker compose logs -f nginx backend
docker compose down
```
