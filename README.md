# HNU 校园交流平台（前后端分离）

这是一个基于 Spring Boot + PostgreSQL + Redis 的后端服务，以及基于 React + Ant Design 的前端应用。项目包含用户注册/登录、帖子发布与浏览、评论互动、管理员审核与禁言等功能，适用于校园信息交流场景。

## 目录结构

- `HNU-backend/`：后端（Spring Boot + MyBatis Plus + Redis + JWT）
- `HNU-frontend/`：前端（React + Ant Design + Vite）
- `baseSQL.sql`：数据库建表脚本
- `seed_users.sql`：批量用户数据脚本
- `seed_posts.sql`：批量帖子数据脚本

## 环境要求

- JDK 17
- PostgreSQL 12+
- Redis 6+
- Node.js 18+

## 数据库初始化

1) 创建数据库（示例名：`hnu_campus`），并执行建表脚本：

```sql
-- baseSQL.sql
```

2) 批量插入用户（默认密码 `123456`，状态 `pending`，角色 `STUDENT`）：

```sql
-- seed_users.sql
```

3) 批量插入帖子：

```sql
-- seed_posts.sql
```

## 后端启动

进入 `HNU-backend`，修改数据库和 Redis 连接（`src/main/resources/application.yml`），再启动：

```bash
mvn spring-boot:run
```

后端服务默认地址：`http://localhost:8080`

## 前端启动

进入 `HNU-frontend/my-app`：

```bash
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

## 功能列表

- 用户注册/登录（验证码 + 审核）
- JWT 鉴权与管理员权限控制
- 帖子发布、列表、搜索、详情
- 热搜榜（Redis 缓存）
- 评论发布、点赞、删除（逻辑删除展示）
- 管理员审核注册、禁言/解禁用户、强制删除帖子、删除评论
- 个人中心、我的帖子、发帖入口

## 常见问题

- 如果提示 CORS：确认后端已开启跨域并已重启
- 如果帖子详情加载失败：确保 Redis 可用或使用降级逻辑
- 标题长度限制：`posts.title` 仅允许 4-20 字

## 许可证

Apache 2.0
