-- Seed 30 pending student users with bcrypt password generated in DB
-- Requires pgcrypto extension for crypt/gen_salt
CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (phone, nickname, password, student_id, campus_card_url, auth_status, role, is_muted)
VALUES
('18800000001', 'user01', crypt('123456', gen_salt('bf')), '2021001001', NULL, 'pending', 'STUDENT', FALSE),
('18800000002', 'user02', crypt('123456', gen_salt('bf')), '2021001002', NULL, 'pending', 'STUDENT', FALSE),
('18800000003', 'user03', crypt('123456', gen_salt('bf')), '2021001003', NULL, 'pending', 'STUDENT', FALSE),
('18800000004', 'user04', crypt('123456', gen_salt('bf')), '2021001004', NULL, 'pending', 'STUDENT', FALSE),
('18800000005', 'user05', crypt('123456', gen_salt('bf')), '2021001005', NULL, 'pending', 'STUDENT', FALSE),
('18800000006', 'user06', crypt('123456', gen_salt('bf')), '2021001006', NULL, 'pending', 'STUDENT', FALSE),
('18800000007', 'user07', crypt('123456', gen_salt('bf')), '2021001007', NULL, 'pending', 'STUDENT', FALSE),
('18800000008', 'user08', crypt('123456', gen_salt('bf')), '2021001008', NULL, 'pending', 'STUDENT', FALSE),
('18800000009', 'user09', crypt('123456', gen_salt('bf')), '2021001009', NULL, 'pending', 'STUDENT', FALSE),
('18800000010', 'user10', crypt('123456', gen_salt('bf')), '2021001010', NULL, 'pending', 'STUDENT', FALSE),
('18800000011', 'user11', crypt('123456', gen_salt('bf')), '2021001011', NULL, 'pending', 'STUDENT', FALSE),
('18800000012', 'user12', crypt('123456', gen_salt('bf')), '2021001012', NULL, 'pending', 'STUDENT', FALSE),
('18800000013', 'user13', crypt('123456', gen_salt('bf')), '2021001013', NULL, 'pending', 'STUDENT', FALSE),
('18800000014', 'user14', crypt('123456', gen_salt('bf')), '2021001014', NULL, 'pending', 'STUDENT', FALSE),
('18800000015', 'user15', crypt('123456', gen_salt('bf')), '2021001015', NULL, 'pending', 'STUDENT', FALSE),
('18800000016', 'user16', crypt('123456', gen_salt('bf')), '2021001016', NULL, 'pending', 'STUDENT', FALSE),
('18800000017', 'user17', crypt('123456', gen_salt('bf')), '2021001017', NULL, 'pending', 'STUDENT', FALSE),
('18800000018', 'user18', crypt('123456', gen_salt('bf')), '2021001018', NULL, 'pending', 'STUDENT', FALSE),
('18800000019', 'user19', crypt('123456', gen_salt('bf')), '2021001019', NULL, 'pending', 'STUDENT', FALSE),
('18800000020', 'user20', crypt('123456', gen_salt('bf')), '2021001020', NULL, 'pending', 'STUDENT', FALSE),
('18800000021', 'user21', crypt('123456', gen_salt('bf')), '2021001021', NULL, 'pending', 'STUDENT', FALSE),
('18800000022', 'user22', crypt('123456', gen_salt('bf')), '2021001022', NULL, 'pending', 'STUDENT', FALSE),
('18800000023', 'user23', crypt('123456', gen_salt('bf')), '2021001023', NULL, 'pending', 'STUDENT', FALSE),
('18800000024', 'user24', crypt('123456', gen_salt('bf')), '2021001024', NULL, 'pending', 'STUDENT', FALSE),
('18800000025', 'user25', crypt('123456', gen_salt('bf')), '2021001025', NULL, 'pending', 'STUDENT', FALSE),
('18800000026', 'user26', crypt('123456', gen_salt('bf')), '2021001026', NULL, 'pending', 'STUDENT', FALSE),
('18800000027', 'user27', crypt('123456', gen_salt('bf')), '2021001027', NULL, 'pending', 'STUDENT', FALSE),
('18800000028', 'user28', crypt('123456', gen_salt('bf')), '2021001028', NULL, 'pending', 'STUDENT', FALSE),
('18800000029', 'user29', crypt('123456', gen_salt('bf')), '2021001029', NULL, 'pending', 'STUDENT', FALSE),
('18800000030', 'user30', crypt('123456', gen_salt('bf')), '2021001030', NULL, 'pending', 'STUDENT', FALSE);
