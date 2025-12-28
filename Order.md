# 项目开发顺序 Order.md

目标：按“数据库 -> 后端 -> 前端 -> 联调/测试”的顺序推进，避免返工；每一阶段都能跑通最小闭环（注册/登录 -> 发帖 -> 评论/点赞 -> 管理端审核）。

---

## 0. 推荐开发顺序（最省返工）

1) **数据库表结构**（users / posts / comments / likes）  
2) **后端基础能力**（统一返回、异常、参数校验、分页、CORS、JWT拦截）  
3) **认证模块**（验证码/注册/登录/鉴权/封禁）  
4) **帖子模块**（列表/详情/发帖/搜索/热榜/浏览数/点赞）  
5) **评论模块**（发评论/楼中楼/树形返回/软删除）  
6) **管理端模块**（审核用户/禁言/删帖/删评论/列表）  
7) **前端页面**（登录注册、帖子列表、详情、发帖、我的、后台）  
8) **联调与压测**（401/跨域/Redis降级/一致性）  
9) **上线前检查**（初始化数据、索引、权限、日志与监控）

---

## 1. 技术栈与原则

**前端**：React + Ant Design + Axios + React Router + Vite  
**后端**：Spring Boot + MyBatis-Plus + PostgreSQL + Redis + JWT  
**架构**：前后端分离，后端提供 REST API

**关键约束**
- 所有接口返回统一结构（code/msg/data）。
- JWT 负责鉴权；不使用 session。
- Redis 用于验证码、热点帖子、浏览数缓存；必要时可降级回 DB。

---

## 2. 数据库表设计（核心字段）

### 2.1 用户表 `users`
字段建议：phone / nickname / password / auth_status / role / is_muted / create_time / update_time  
- phone：唯一 + 索引  
- auth_status：pending / approved / rejected  
- role：STUDENT / ADMIN  
- is_muted：是否禁言（true/false）

### 2.2 帖子表 `posts`
字段建议：title / content / category_id / author_id / contact_info / view_count / like_count / hot_score / status / create_time / update_time  
- title：**4~20 字符**（varchar(20)），前端与后端都校验  
- status：normal / deleted  
- hot_score：由浏览数、点赞数等计算（可用 Redis ZSet 做热榜）

分类固定 5 类：
1 二手闲置；2 打听求助；3 恋爱交友；4 校园趣事；5 考试信息

### 2.3 评论表 `comments`
字段建议：post_id / user_id / content / parent_id / like_count / status / create_time / update_time  
- parent_id：楼中楼（为空表示一级评论）  
- status：normal / deleted

### 2.4 点赞表
- posts_likes：post_id / user_id / create_time（联合唯一防重复）  
- comment_likes：comment_id / user_id / create_time（联合唯一防重复）

**索引建议**
- users(phone) unique
- posts(category_id, create_time)
- posts(author_id, create_time)
- comments(post_id, create_time)
- likes(post_id, user_id) unique / likes(comment_id, user_id) unique

---

## 3. JWT 鉴权与权限控制（RBAC）

### 3.1 机制
- 登录成功签发 JWT：载荷包含 userId + role  
- 前端请求带：`Authorization: Bearer <token>`  
- 后端拦截器校验 token，解析 userId 写入 ThreadLocal

### 3.2 关键类与路径
- `HNU-backend/src/main/java/com/hnu/campus/security/JwtUtil.java`：生成/解析 token  
- `HNU-backend/src/main/java/com/hnu/campus/security/AuthInterceptor.java`：拦截鉴权  
- `HNU-backend/src/main/java/com/hnu/campus/security/CurrentUserContext.java`：ThreadLocal 保存当前用户  
- `AdminServiceImpl.ensureAdmin()`：管理员权限校验

**注意**
- 必须在请求结束后清理 ThreadLocal，避免线程复用导致串号。  
- 管理端接口统一走 `ensureAdmin()` 做 RBAC。

---

## 4. 认证模块（验证码/注册/登录）

### 4.1 功能
- 发送验证码（Redis 存储）  
- 注册（默认 pending，待管理员审核）  
- 登录（仅 approved 可登录；禁言用户限制发言）

### 4.2 后端实现（建议接口/方法）
- `AuthServiceImpl.sendVerifyCode()`
  - Redis Key：`verify_code:{phone}`，TTL 5 分钟
- `AuthServiceImpl.register()`
  - 校验验证码
  - BCrypt 加密 password
  - auth_status = pending
- `AuthServiceImpl.login()`
  - 校验密码
  - auth_status 必须为 approved
  - 若 is_muted=true：可登录但限制发帖/评论
  - 返回 JWT

代码路径：
- `HNU-backend/src/main/java/com/hnu/campus/service/impl/AuthServiceImpl.java`

前端页面：
- `HNU-frontend/my-app/src/pages/Register.tsx`
- `HNU-frontend/my-app/src/pages/Login.tsx`

---

## 5. 帖子模块（列表/详情/搜索/热榜/点赞）

### 5.1 功能
- 发帖、删帖（软删除）
- 列表分页 + 分类筛选
- 关键词搜索
- 热榜（hot_score）
- 详情页（浏览数 +1）
- 点赞切换（toggle like）

### 5.2 后端实现（建议方法）
- `PostServiceImpl.createPost()`
- `PostServiceImpl.getPostList()`（分页、分类）
- `PostServiceImpl.searchPosts()`（关键词）
- `PostServiceImpl.getHotPosts()`（Redis ZSet）
- `PostServiceImpl.getPostDetail()`（浏览数累计）
- `PostServiceImpl.toggleLike()`（点赞/取消点赞，更新 like_count 与热度）

**Redis 策略**
- 热榜：Redis ZSet（member=postId，score=hot_score）
- 读取优先 Redis，失败/无数据则降级 DB，并可回填 Redis

代码路径：
- `HNU-backend/src/main/java/com/hnu/campus/service/impl/PostServiceImpl.java`

前端页面：
- `src/pages/Posts.tsx`（列表/筛选/分页）
- `src/pages/PostDetail.tsx`（详情/评论区）
- `src/pages/CreatePost.tsx`（发帖）
- `src/pages/MyPosts.tsx`（我的帖子）

---

## 6. 评论模块（楼中楼/树形/删除）

### 6.1 功能
- 发表评论（一级/回复）
- 获取评论树（按 parent_id 组织）
- 删除评论
  - 普通用户：只能删自己的
  - 管理员：可强制删除（软删除+替换内容）

### 6.2 后端实现（建议方法）
- `CommentServiceImpl.createComment()`
- `CommentServiceImpl.getCommentTree()`（返回树形结构 DTO）
- `deleteComment()` / `deleteCommentAsAdmin()`
  - 软删除：status=deleted
  - 管理员删除：content="该评论已被删除"

前端：
- 评论区集成在 `PostDetail.tsx`
- 支持回复某条评论（带 parent_id）

**注意**
- 回复层级建议只做 2 层（一级 + 回复），避免无限递归复杂度。
- 返回 DTO 时带 userInfo（昵称、头像等）避免前端多次请求。

---

## 7. 管理员模块（审核/禁言/内容治理）

### 7.1 功能
- 审核用户：pending -> approved/rejected
- 禁言/解禁：muteUser
- 强制删帖：forceDeletePost（软删除）
- 删评论：deleteComment（软删除并替换内容）
- 查询：待审核用户列表、用户列表、帖子/评论治理列表（可选）

### 7.2 后端方法（建议）
- `AdminServiceImpl.reviewAuth()`
- `AdminServiceImpl.muteUser()`
- `AdminServiceImpl.forceDeletePost()`
- `AdminServiceImpl.deleteComment()`
- `AdminServiceImpl.getPendingUsers()`
- `AdminServiceImpl.getAllUsers()`（或仅 approved）

前端页面：
- `src/pages/AdminPending.tsx`
- 用户管理/内容管理页面（可拆分）
- 危险操作用二次确认（Modal/Alert）

**注意**
- 管理接口务必校验管理员权限（RBAC）。
- 返回 DTO，避免把密码等敏感字段返回前端。

---

## 8. Redis 与异步一致性（浏览数/热榜/验证码）

### 8.1 使用点
- 验证码：Redis String + TTL
- 热榜：Redis ZSet
- 浏览数：Redis INCR（高频）

### 8.2 定时回写（浏览数）
- 详情页浏览数：Redis INCR  
- 定时任务 `ViewCountSyncTask`：每 10 分钟把增量同步到 DB

代码路径：
- `HNU-backend/src/main/java/com/hnu/campus/task/ViewCountSyncTask.java`

**注意**
- Redis 挂了要能降级：直接写 DB 或临时不计数但不影响主流程。

---

## 9. 前端鉴权与请求封装

### 9.1 目标
- 自动携带 token
- 统一处理 401（跳转登录/清理缓存）
- 路由守卫（需要登录才能访问的页面）

### 9.2 实现位置
- AuthContext：`src/store/auth.tsx`（保存 token 与用户信息）
- Axios 封装：`src/api/client.ts`（request/response 拦截器）
- 路由配置：`src/App.tsx`

**注意**
- token 建议存 localStorage（刷新不丢）
- 401 统一处理：清 token + 跳转 Login

---

## 10. Mock 数据（前端先跑起来）

- `src/mock/posts.json`
- `src/mock/comments.json`
- 列表/详情/评论先用 mock 渲染，后续切换真实接口

---

## 11. 分页与查询

- 前端：Ant Design `Pagination`
- 后端：MyBatis-Plus 分页（Page）
- 列表接口返回：records + total + current + size

页面参考：`src/pages/Posts.tsx`

---

## 12. 跨域 CORS

- 开发环境前端：`localhost:5173`
- 后端配置：`CorsConfig` 或 `WebConfig.addCorsMappings`

---

## 13. 初始化 SQL 与种子数据

- `seed_users.sql`：包含 pending/approved、STUDENT/ADMIN、BCrypt 密码
- `seed_posts_utf8.sql`：50 条帖子（title 必须 <= 20）

---

## 14. 常见问题清单（你们会踩的坑）

- CORS 未放行导致前端请求失败  
- Redis 连接异常导致接口 500：需要降级与异常处理  
- JWT 拦截器未放行登录/注册接口导致死循环 401  
- ThreadLocal 未清理导致串用户  
- is_muted=true：允许登录但禁止发帖/评论（业务要明确）  
- title 长度：前端校验 + 后端校验 + 数据库 varchar(20) 三重一致

---

## 15. 交付物检查（最终能验收）

- 能注册/登录（token 正常）
- 能发帖/看列表/看详情（浏览数增长）
- 能评论/回复/树形展示
- 能点赞/取消点赞（计数正确）
- 管理端能审核用户、禁言、删帖删评
- Redis 挂掉系统仍可用（至少核心链路可用）
