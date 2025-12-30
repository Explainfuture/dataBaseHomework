-- ============================================
-- 用户信息视图（不包含敏感信息）
-- 仅展示：用户ID、手机号、昵称、学号、校园卡、注册时间
-- ============================================
CREATE OR REPLACE VIEW users_info AS
SELECT
    id,
    phone,
    nickname,
    student_id,
    campus_card_url,
    create_time
FROM users;
