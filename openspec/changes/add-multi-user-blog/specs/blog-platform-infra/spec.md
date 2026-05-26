## ADDED Requirements

### Requirement: 系统必须提供前后端分离的工程骨架
系统 SHALL 以单仓库 Monorepo 方式组织前端、后端和部署配置，并允许开发者在同一仓库中完成运行与维护。

#### Scenario: 开发者查看工程结构
- **WHEN** 开发者初始化或拉取仓库后查看目录
- **THEN** 系统 MUST 提供清晰分离的 `frontend/`、`backend/` 和部署相关目录结构

### Requirement: 系统必须提供 JWT 认证与角色鉴权基础设施
系统 SHALL 提供基于 JWT 的认证机制，并对游客、作者和管理员实施不同访问控制。

#### Scenario: 游客访问作者工作区接口
- **WHEN** 未登录用户请求 `/api/me/*` 资源
- **THEN** 系统 MUST 拒绝访问并返回未认证结果

#### Scenario: 作者访问管理员接口
- **WHEN** 作者请求 `/api/admin/*` 资源
- **THEN** 系统 MUST 拒绝访问并返回无权限结果

### Requirement: 系统必须使用 PostgreSQL 持久化博客核心数据
系统 SHALL 在 PostgreSQL 中持久化用户、文章、分类、标签、文章标签关联和上传资源记录。

#### Scenario: 系统保存新文章
- **WHEN** 作者成功创建新文章
- **THEN** 系统 MUST 在数据库中保存文章正文、作者归属、分类、标签和发布时间等数据

### Requirement: 系统必须通过对象存储管理上传资源
系统 SHALL 使用兼容 S3 的对象存储保存图片资源，并在数据库中保存上传元数据。

#### Scenario: 作者上传图片成功
- **WHEN** 图片上传接口接收到合法图片文件
- **THEN** 系统 MUST 将文件写入对象存储并记录上传者、对象键和访问地址

### Requirement: 系统必须支持通过 Docker Compose 启动完整环境
系统 SHALL 提供 Docker Compose 配置，以启动前端、后端、数据库、对象存储和代理服务。

#### Scenario: 开发者启动本地完整环境
- **WHEN** 开发者执行 Docker Compose 启动命令
- **THEN** 系统 MUST 拉起运行博客所需的全部核心服务

### Requirement: 系统必须提供可复现的初始化说明
系统 SHALL 提供完整 README，说明本地开发、Docker Compose 启动、默认管理员账号和对象存储配置方式。

#### Scenario: 新开发者首次接入项目
- **WHEN** 新开发者按照 README 操作
- **THEN** 系统 MUST 允许其独立完成项目启动与基础验证
