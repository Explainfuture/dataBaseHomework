# HNU校园交流平台 - 后端项目

## 项目简介
基于SpringBoot 3.x + PostgreSQL + MyBatisPlus + Redis的校园交流平台后端服务。

## 技术栈
- Java 17+
- SpringBoot 3.2.0
- MyBatisPlus 3.5.5
- PostgreSQL
- Redis
- Lombok
- SpringDoc OpenAPI (Swagger 3)

## 项目结构
```
HNU-backend/
├── src/
│   ├── main/
│   │   ├── java/com/hnu/campus/
│   │   │   ├── controller/      # 控制器层
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── entity/          # 实体类
│   │   │   ├── enums/           # 枚举类
│   │   │   └── HnuCampusPlatformApplication.java
│   │   └── resources/
│   │       └── application.yml  # 配置文件
│   └── test/
└── pom.xml
```

## 数据库配置
- 数据库：PostgreSQL
- 数据库名：hnu_campus
- 用户名：postgres
- 密码：1
- 端口：5432

## API文档
启动项目后，访问 Swagger UI：
- http://localhost:8080/swagger-ui.html
- http://localhost:8080/api-docs

## 核心接口

### 认证模块 (AuthController)
- `POST /api/v1/auth/register` - 用户注册（包含手机验证码校验）
- `POST /api/v1/auth/login` - 用户登录
- `POST /api/v1/auth/send-verify-code` - 发送验证码

### 用户模块 (UserController)
- `GET /api/v1/users/me` - 获取个人信息
- `PUT /api/v1/users/me` - 修改个人信息
- `GET /api/v1/users/me/posts` - 查看我的发帖

### 帖子模块 (PostController)
- `POST /api/v1/posts` - 发布帖子
- `DELETE /api/v1/posts/{id}` - 删除帖子
- `GET /api/v1/posts` - 获取帖子列表（支持分页和分类筛选）
- `GET /api/v1/posts/search` - 搜索帖子（模糊搜索）
- `GET /api/v1/posts/hot` - 获取热搜帖子（前10条）
- `GET /api/v1/posts/{id}` - 获取帖子详情（包含评论）
- `POST /api/v1/posts/{id}/like` - 点赞/取消点赞帖子

### 评论模块 (CommentController)
- `POST /api/v1/comments` - 发布评论
- `POST /api/v1/comments/{id}/like` - 点赞/取消点赞评论
- `DELETE /api/v1/comments/{id}` - 删除评论

### 管理员模块 (AdminController)
- `POST /api/v1/admin/auth/review` - 审核注册信息
- `DELETE /api/v1/admin/posts/{id}` - 强制删除帖子
- `POST /api/v1/admin/users/mute` - 禁言用户
- `GET /api/v1/admin/users/pending` - 获取待审核用户列表

## Redis使用说明

### 验证码存储
- Key: `verify_code:{phone}`
- Value: 验证码
- 过期时间: 5分钟

### 帖子浏览量缓存
- Key: `post:view:{postId}`
- Value: 浏览量计数
- 同步策略: 定时任务每10分钟同步到数据库

### 热搜帖子缓存
- Key: `hot:posts` (ZSET)
- Score: 热度值 (hot_score)
- 过期时间: 1小时

## 开发说明

1. 所有Controller中的TODO标记需要实现具体业务逻辑
2. JWT Token认证需要实现拦截器或过滤器
3. 管理员权限校验需要实现AOP或拦截器
4. 验证码发送需要接入短信服务（可选）

## 运行项目

1. 确保PostgreSQL和Redis已启动
2. 执行数据库脚本 `baseSQL.sql` 创建表结构
3. 运行 `HnuCampusPlatformApplication.main()` 启动项目
4. 访问 http://localhost:8080/swagger-ui.html 查看API文档

