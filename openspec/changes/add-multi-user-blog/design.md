## Context

当前仓库处于博客系统开发前的准备阶段，已有一份经过确认的设计文档和一份实现计划，但它们仍位于 `docs/` 目录下，尚未纳入标准 OpenSpec change 流程。此次变更的目标是在 OpenSpec 的标准结构内，定义一个可执行的多用户博客 MVP，使后续开发能够按照 `propose -> apply -> archive` 的规范推进。

该系统采用单仓库 Monorepo，并保持前后端分离。前端使用 Vue 3 + Vite + Tailwind CSS，统一承载公开博客、作者工作区和 `/admin` 管理后台；后端使用 Spring Boot，负责 JWT 认证、博客内容 API、管理接口、Markdown 渲染与 HTML 清洗。数据存储采用 PostgreSQL，图片通过 MinIO 对象存储管理，系统整体默认通过 Docker Compose 启动。

约束条件如下：
- 用户已明确不开放公开注册，账号仅由管理员创建。
- 作者发布文章后直接公开，不引入审核流。
- MVP 阶段要求功能完整但不追求额外扩展，不包含评论、点赞、收藏、草稿、多版本历史等能力。
- 前端登录态在 MVP 中保存在 localStorage，以降低联调复杂度。

## Goals / Non-Goals

**Goals:**
- 建立一个前后端分离、可通过 Docker Compose 启动的多用户博客 MVP。
- 支持游客公开浏览文章、分类筛选、搜索与作者主页访问。
- 支持作者登录后进行个人资料管理、文章创建/编辑/删除、Markdown 写作和图片上传。
- 支持管理员进行用户管理、文章管理、分类标签管理和基础统计查看。
- 在后端统一处理 Markdown 到 HTML 的渲染与安全清洗，确保展示一致性与基本安全边界。
- 将当前已确认方案转化为标准 OpenSpec change 的设计依据，为后续 apply 阶段提供明确技术决策。

**Non-Goals:**
- 不实现公开注册、邮箱验证、找回密码等完整身份生命周期流程。
- 不实现评论、点赞、收藏、社交分享或消息通知。
- 不实现复杂审核流、草稿箱、定时发布、文章版本历史。
- 不在本次变更中引入微服务拆分、多前端仓库拆分或云原生高级编排方案。
- 不追求完整 BI、访问趋势报表或复杂埋点分析。

## Decisions

### 1. 使用单仓库 Monorepo 管理前后端与部署配置
- **Decision**: 在同一个仓库内维护 `frontend/`、`backend/`、`docker/` 与根目录部署配置。
- **Rationale**: 当前项目规模属于个人或小团队 MVP，单仓库更便于统一维护文档、环境变量模板和 Docker Compose 编排，也更适合在 OpenSpec 中以一个 change 跟踪完整交付。
- **Alternatives considered**:
  - **双仓库前后端分离**：隔离更彻底，但会增加初始化、联调和部署说明成本，不适合当前阶段。
  - **单体全栈应用**：运行更集中，但与用户已确认的“前后端分离”方向不一致。

### 2. 前端采用单 Vue 应用承载公开站点、作者工作区和管理后台
- **Decision**: 使用一个 Vue 项目，通过不同 layout 和路由守卫区分 `/`、`/me/*` 和 `/admin/*`。
- **Rationale**: 这样既能保证部署简单，也能避免两个前端项目带来的重复配置和样式分裂。对于当前 MVP，使用一个应用足以维持清晰边界。
- **Alternatives considered**:
  - **前台与后台拆成两个独立前端项目**：隔离更强，但工程体积和维护成本更高。
  - **直接引入重型后台 UI 框架**：开发速度可能更快，但不利于保持整体简洁技术风格。

### 3. 后端采用 Spring Boot + Spring Security + JPA
- **Decision**: 使用 Spring Boot 作为 API 框架，Spring Security 做认证鉴权，JPA 管理持久层，Flyway 管理表结构迁移。
- **Rationale**: 这组技术与用户已有的 Java/Spring 经验匹配，同时足够支撑多角色权限、后台管理和标准 CRUD 场景。
- **Alternatives considered**:
  - **Node.js/NestJS**：也能满足需求，但与用户偏好的 Java 技术栈不一致。
  - **MyBatis 替代 JPA**：灵活性更高，但对当前以标准实体关系和 CRUD 为主的场景，JPA 更快落地。

### 4. 认证采用 JWT，前端在 MVP 中将 token 存储于 localStorage
- **Decision**: 登录成功后由后端签发 JWT，前端通过 `Authorization: Bearer <token>` 携带，并在 MVP 中将 token 保存到 localStorage。
- **Rationale**: 前后端分离场景下 JWT 更适合解耦部署；localStorage 虽然安全性不如 HttpOnly Cookie，但配置简单，适合当前先完成可运行 MVP 的目标。
- **Alternatives considered**:
  - **HttpOnly Cookie**：安全性更高，但跨域、代理与本地联调复杂度更高。
  - **Session 方案**：更依赖后端会话状态，与前后端分离的部署方式不够契合。

### 5. 后端统一保存 `content_markdown` 与 `content_html`
- **Decision**: 保存文章时同时持久化 Markdown 原文和渲染后的 HTML，展示时前端直接消费 HTML。
- **Rationale**: 这样能兼顾可编辑性和展示性能，同时保证文章详情页在不同客户端展示一致。后端统一渲染还能集中处理 HTML 清洗，降低 XSS 风险。
- **Alternatives considered**:
  - **只保存 Markdown，展示时实时渲染**：实现更简单，但会增加读取时计算成本，也会使展示策略分散。
  - **前端渲染 Markdown**：降低后端职责，但不利于统一样式和安全清洗策略。

### 6. 图片上传采用 MinIO 对象存储，并记录上传元数据
- **Decision**: 图片通过后端上传到 MinIO，并在数据库中记录 `uploads` 元数据。
- **Rationale**: 这能保证本地开发与部署环境一致，也为后续切换到兼容 S3 的云对象存储保留空间。
- **Alternatives considered**:
  - **本地文件系统存储**：开发更快，但不利于后续部署与静态资源管理。
  - **仅支持外链图片**：实现更轻，但不符合用户对上传能力的需求。

### 7. Docker Compose 作为默认启动与部署方式
- **Decision**: 通过 Docker Compose 编排 frontend、backend、postgres、minio、nginx 五类服务。
- **Rationale**: 这样可以为开发、测试、演示和部署提供统一入口，降低环境差异。
- **Alternatives considered**:
  - **只提供本地命令启动**：上手快，但环境一致性差。
  - **直接设计 Kubernetes 部署**：超出 MVP 范围，增加不必要复杂度。

## Risks / Trade-offs

- **[JWT 保存在 localStorage 存在 XSS 风险]** → 通过后端统一 HTML 清洗、限制上传类型、控制前端 `v-html` 使用范围来降低风险；后续如需要更强安全性，可迁移到 HttpOnly Cookie。
- **[单 Vue 项目同时承载前台与后台，路由和状态可能逐渐复杂]** → 通过 layout 分区、按业务域拆分 `views/`、`api/`、`stores/` 控制复杂度。
- **[JPA 在复杂查询场景下可能产生额外调优成本]** → 当前 MVP 以标准 CRUD 和简单检索为主，先用 JPA 快速落地；若后续查询复杂度上升，可对热点接口改为自定义查询。
- **[对象存储、数据库与应用多服务联调提高了本地运行门槛]** → 使用 Docker Compose 统一编排，并在 README 中明确默认账号、端口和启动顺序。
- **[不开放公开注册减少了用户自助能力]** → 这是有意限制，用于降低审核、滥用和身份生命周期复杂度，符合当前多作者博客的 MVP 目标。
