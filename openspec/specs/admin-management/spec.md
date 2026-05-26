# admin-management Specification

## Purpose
TBD - created by archiving change add-multi-user-blog. Update Purpose after archive.
## Requirements
### Requirement: 管理员可以管理用户账号
系统 SHALL 允许管理员创建作者账号，并对已有账号执行启用或禁用操作。

#### Scenario: 管理员创建作者账号
- **WHEN** 管理员提交新用户的用户名、密码、昵称和角色信息
- **THEN** 系统 MUST 创建一个新的作者账号

#### Scenario: 管理员禁用作者账号
- **WHEN** 管理员将某个作者账号设置为禁用
- **THEN** 系统 MUST 阻止该账号继续登录系统

### Requirement: 管理员可以管理全站文章
系统 SHALL 允许管理员查看、编辑和删除任意作者发布的文章。

#### Scenario: 管理员删除文章
- **WHEN** 管理员删除某篇文章
- **THEN** 系统 MUST 使该文章不再对游客可见

### Requirement: 管理员可以管理分类与标签
系统 SHALL 允许管理员创建、编辑和删除分类与标签。

#### Scenario: 管理员新增分类
- **WHEN** 管理员提交新的分类名称与标识信息
- **THEN** 系统 MUST 创建该分类并允许其用于文章归类

#### Scenario: 管理员删除被引用的分类
- **WHEN** 管理员尝试删除仍被文章使用的分类
- **THEN** 系统 MUST 拒绝删除并返回说明该分类仍被引用

### Requirement: 管理员可以查看站点基础统计
系统 SHALL 提供管理员仪表盘，展示用户总数、文章总数、分类总数和标签总数。

#### Scenario: 管理员打开仪表盘
- **WHEN** 管理员访问后台首页
- **THEN** 系统 MUST 返回当前用户、文章、分类和标签的统计总数

