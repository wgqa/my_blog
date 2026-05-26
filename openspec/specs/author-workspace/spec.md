# author-workspace Specification

## Purpose
TBD - created by archiving change add-multi-user-blog. Update Purpose after archive.
## Requirements
### Requirement: 作者必须通过登录进入工作区
系统 SHALL 仅允许已启用的作者账号通过用户名和密码登录后访问作者工作区。

#### Scenario: 已启用作者登录成功
- **WHEN** 作者使用正确的用户名和密码登录
- **THEN** 系统 MUST 返回有效的认证令牌并允许其访问 `/me/*` 路径下的资源

### Requirement: 作者可以维护个人资料
系统 SHALL 允许作者查看并更新自己的基础资料，包括昵称、邮箱、头像地址和个人简介。

#### Scenario: 作者更新个人资料
- **WHEN** 作者提交新的个人资料信息
- **THEN** 系统 MUST 保存这些变更并在后续查询中返回最新资料

### Requirement: 作者可以管理自己的文章
系统 SHALL 允许作者创建、编辑、查看和删除仅属于自己的文章。

#### Scenario: 作者创建文章
- **WHEN** 作者提交新的文章标题、摘要、正文、分类和标签
- **THEN** 系统 MUST 创建一篇归属于该作者的公开文章

#### Scenario: 作者编辑自己的文章
- **WHEN** 作者更新自己已有文章的内容
- **THEN** 系统 MUST 保存更新后的文章内容和元信息

#### Scenario: 作者删除自己的文章
- **WHEN** 作者删除自己已有文章
- **THEN** 系统 MUST 使该文章不再出现在公开列表中

### Requirement: 作者不得操作他人的文章
系统 SHALL 拒绝作者对非本人文章进行编辑或删除操作。

#### Scenario: 作者尝试编辑他人文章
- **WHEN** 作者请求修改不属于自己的文章
- **THEN** 系统 MUST 拒绝该请求并返回无权限结果

### Requirement: 作者写作流程必须支持 Markdown
系统 SHALL 提供 Markdown 编辑能力，允许作者使用 Markdown 编写文章并插入代码块与图片。

#### Scenario: 作者使用 Markdown 编辑文章
- **WHEN** 作者在编辑器中输入 Markdown 内容并保存文章
- **THEN** 系统 MUST 持久化 Markdown 原文并生成用于展示的 HTML 内容

### Requirement: 作者可以上传文章图片
系统 SHALL 提供图片上传接口和编辑器集成，使作者可以将图片插入文章内容。

#### Scenario: 作者上传合法图片
- **WHEN** 作者上传符合格式与大小限制的图片
- **THEN** 系统 MUST 将图片存入对象存储并返回可插入文章的访问地址

