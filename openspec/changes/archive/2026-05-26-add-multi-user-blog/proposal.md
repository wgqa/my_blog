## Why

当前仓库还没有一个可运行的博客系统，但需求已经明确：需要一个简洁技术风格的多用户博客平台，支持游客浏览、作者发文和管理员统一管理。现在把这套能力正式落为 OpenSpec change，可以让后续开发严格按照 propose → apply → archive 的标准流程推进。

## What Changes

- 新增一个前后端分离的多用户博客 MVP，采用单仓库 Monorepo 管理。
- 新增公开博客阅读能力，包括文章列表、分类筛选、搜索、文章详情和作者主页。
- 新增作者工作区能力，包括登录、个人资料维护、文章创建/编辑/删除、Markdown 写作与图片上传。
- 新增管理员后台能力，包括用户管理、全站文章管理、分类管理、标签管理和基础统计。
- 新增 Spring Boot + PostgreSQL + MinIO + Docker Compose 的运行与部署基础设施。

## Capabilities

### New Capabilities
- `public-blog`: 提供游客可访问的公开博客阅读能力，包括文章列表、分类筛选、搜索、文章详情和作者主页。
- `author-workspace`: 提供作者登录后的个人资料管理、文章管理、Markdown 编辑和图片上传能力。
- `admin-management`: 提供管理员用户管理、全站文章管理、分类标签管理和基础统计能力。
- `blog-platform-infra`: 提供前后端分离工程骨架、认证、安全、数据库、对象存储和 Docker Compose 部署能力。

### Modified Capabilities
- 无

## Impact

- 会新增 `frontend/`、`backend/`、`docker/`、`docker-compose.yml`、`.env.example` 等工程结构。
- 会新增 Spring Boot API、Vue 页面与路由、PostgreSQL 表结构、MinIO 上传链路和 JWT 认证流程。
- 会影响后续实现方式：开发将按 OpenSpec change 中的 tasks 执行，而不是直接基于 `docs/` 下的独立计划文档实施。
