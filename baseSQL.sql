-- ============================================
-- HNU校园交流平台 - PostgreSQL 数据库建表脚本
-- 数据库：PostgreSQL
-- 版本：1.0
-- 创建时间：2024
-- ============================================

-- 启用必要的扩展
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- 用于模糊查询和全文检索优化

-- ============================================
-- 1. 用户表 (users)
-- ============================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(11) NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    student_id VARCHAR(20),
    campus_card_url VARCHAR(500),
    auth_status VARCHAR(20) NOT NULL DEFAULT 'pending',
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    is_muted BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_auth_status CHECK (auth_status IN ('pending', 'approved', 'rejected')),
    CONSTRAINT chk_role CHECK (role IN ('STUDENT', 'ADMIN')),
    CONSTRAINT chk_phone_format CHECK (phone ~ '^1[3-9]\d{9}$')
);

-- 用户表索引
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_student_id ON users(student_id);
CREATE INDEX idx_users_auth_status ON users(auth_status);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_create_time ON users(create_time DESC);

-- 用户表注释
COMMENT ON TABLE users IS '用户表，存储平台用户基本信息';
COMMENT ON COLUMN users.id IS '用户ID，自增主键';
COMMENT ON COLUMN users.phone IS '手机号，唯一索引';
COMMENT ON COLUMN users.nickname IS '昵称';
COMMENT ON COLUMN users.password IS '密码，加密存储（BCrypt/SHA256等）';
COMMENT ON COLUMN users.student_id IS '学号';
COMMENT ON COLUMN users.campus_card_url IS '校园卡照片URL';
COMMENT ON COLUMN users.auth_status IS '认证状态：pending(待审核)/approved(通过)/rejected(拒绝)';
COMMENT ON COLUMN users.role IS '角色：STUDENT(学生)/ADMIN(管理员)';
COMMENT ON COLUMN users.is_muted IS '被禁言状态：false(正常)/true(禁言)';
COMMENT ON COLUMN users.create_time IS '创建时间';
COMMENT ON COLUMN users.update_time IS '更新时间';

-- ============================================
-- 2. 帖子分类字典表 (post_categories)
-- ============================================
CREATE TABLE post_categories (
    id SERIAL PRIMARY KEY,
    category_code VARCHAR(20) NOT NULL UNIQUE,
    category_name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    sort_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 分类表索引
CREATE INDEX idx_post_categories_code ON post_categories(category_code);
CREATE INDEX idx_post_categories_active ON post_categories(is_active, sort_order);

-- 分类表注释
COMMENT ON TABLE post_categories IS '帖子分类字典表，管理帖子分类信息';
COMMENT ON COLUMN post_categories.id IS '分类ID';
COMMENT ON COLUMN post_categories.category_code IS '分类代码：SECOND_HAND(二手闲置)/ASK_HELP(打听求助)/DATING(恋爱交友)/CAMPUS_FUN(校园趣事)/EXAM_INFO(考试信息)';
COMMENT ON COLUMN post_categories.category_name IS '分类名称';
COMMENT ON COLUMN post_categories.description IS '分类描述';
COMMENT ON COLUMN post_categories.sort_order IS '排序顺序';
COMMENT ON COLUMN post_categories.is_active IS '是否启用';
COMMENT ON COLUMN post_categories.create_time IS '创建时间';
COMMENT ON COLUMN post_categories.update_time IS '更新时间';

-- 初始化分类数据
INSERT INTO post_categories (category_code, category_name, description, sort_order) VALUES
('SECOND_HAND', '二手闲置', '二手物品交易', 1),
('ASK_HELP', '打听求助', '校园生活求助信息', 2),
('DATING', '恋爱交友', '交友信息', 3),
('CAMPUS_FUN', '校园趣事', '校园生活分享', 4),
('EXAM_INFO', '考试信息', '考试相关资讯', 5);

-- ============================================
-- 3. 帖子表 (posts)
-- ============================================
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    category_id INT NOT NULL,
    author_id BIGINT NOT NULL,
    contact_info VARCHAR(200),
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    hot_score DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'normal',
    search_vector TSVECTOR,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES post_categories(id),
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT chk_posts_status CHECK (status IN ('normal', 'deleted')),
    CONSTRAINT chk_posts_title_length CHECK (CHAR_LENGTH(title) >= 4 AND CHAR_LENGTH(title) <= 20)
);

-- 帖子表索引
CREATE INDEX idx_posts_category_id ON posts(category_id);
CREATE INDEX idx_posts_author_id ON posts(author_id);
CREATE INDEX idx_posts_status ON posts(status);
CREATE INDEX idx_posts_hot_score ON posts(hot_score DESC, create_time DESC);
CREATE INDEX idx_posts_create_time ON posts(create_time DESC);
CREATE INDEX idx_posts_view_count ON posts(view_count DESC);
CREATE INDEX idx_posts_like_count ON posts(like_count DESC);
-- 全文检索索引（GIN索引，支持快速全文搜索）
CREATE INDEX idx_posts_search_vector ON posts USING GIN(search_vector);

-- 帖子表注释
COMMENT ON TABLE posts IS '帖子表，存储用户发布的帖子信息';
COMMENT ON COLUMN posts.id IS '帖子ID，自增主键';
COMMENT ON COLUMN posts.title IS '标题，限制4-20字（应用层校验）';
COMMENT ON COLUMN posts.content IS '帖子内容';
COMMENT ON COLUMN posts.category_id IS '分类ID，外键关联post_categories';
COMMENT ON COLUMN posts.author_id IS '发布者ID，外键关联users';
COMMENT ON COLUMN posts.contact_info IS '联系方式';
COMMENT ON COLUMN posts.view_count IS '浏览量';
COMMENT ON COLUMN posts.like_count IS '点赞数';
COMMENT ON COLUMN posts.hot_score IS '热度值，用于热搜排序（计算公式：浏览量*0.3 + 点赞数*0.7 + 时间衰减因子）';
COMMENT ON COLUMN posts.status IS '状态：normal(正常)/deleted(删除)';
COMMENT ON COLUMN posts.search_vector IS '全文检索向量，用于PostgreSQL全文检索（预留，可后续接入ElasticSearch）';
COMMENT ON COLUMN posts.create_time IS '创建时间';
COMMENT ON COLUMN posts.update_time IS '更新时间';

-- ============================================
-- 4. 评论表 (comments)
-- ============================================
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    parent_id BIGINT,
    like_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'normal',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
    CONSTRAINT chk_comments_status CHECK (status IN ('normal', 'deleted'))
);

-- 评论表索引
CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_parent_id ON comments(parent_id);
CREATE INDEX idx_comments_create_time ON comments(create_time DESC);
CREATE INDEX idx_comments_status ON comments(status);

-- 评论表注释
COMMENT ON TABLE comments IS '评论表，存储帖子的评论和回复信息';
COMMENT ON COLUMN comments.id IS '评论ID，自增主键';
COMMENT ON COLUMN comments.post_id IS '帖子ID，外键关联posts';
COMMENT ON COLUMN comments.user_id IS '评论者ID，外键关联users';
COMMENT ON COLUMN comments.content IS '评论内容';
COMMENT ON COLUMN comments.parent_id IS '父评论ID，用于回复功能，NULL表示直接评论帖子，否则为回复某条评论';
COMMENT ON COLUMN comments.like_count IS '点赞数';
COMMENT ON COLUMN comments.status IS '状态：normal(正常)/deleted(删除)';
COMMENT ON COLUMN comments.create_time IS '创建时间';
COMMENT ON COLUMN comments.update_time IS '更新时间';

-- ============================================
-- 5. 帖子点赞表 (post_likes) - 防止重复点赞
-- ============================================
CREATE TABLE post_likes (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_post_likes UNIQUE (post_id, user_id)
);

-- 点赞表索引
CREATE INDEX idx_post_likes_post_id ON post_likes(post_id);
CREATE INDEX idx_post_likes_user_id ON post_likes(user_id);

-- 点赞表注释
COMMENT ON TABLE post_likes IS '帖子点赞表，记录用户对帖子的点赞，防止重复点赞';
COMMENT ON COLUMN post_likes.id IS '点赞记录ID';
COMMENT ON COLUMN post_likes.post_id IS '帖子ID';
COMMENT ON COLUMN post_likes.user_id IS '用户ID';
COMMENT ON COLUMN post_likes.create_time IS '创建时间';

-- ============================================
-- 6. 评论点赞表 (comment_likes) - 防止重复点赞
-- ============================================
CREATE TABLE comment_likes (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_comment_likes UNIQUE (comment_id, user_id)
);

-- 评论点赞表索引
CREATE INDEX idx_comment_likes_comment_id ON comment_likes(comment_id);
CREATE INDEX idx_comment_likes_user_id ON comment_likes(user_id);

-- 评论点赞表注释
COMMENT ON TABLE comment_likes IS '评论点赞表，记录用户对评论的点赞，防止重复点赞';
COMMENT ON COLUMN comment_likes.id IS '点赞记录ID';
COMMENT ON COLUMN comment_likes.comment_id IS '评论ID';
COMMENT ON COLUMN comment_likes.user_id IS '用户ID';
COMMENT ON COLUMN comment_likes.create_time IS '创建时间';