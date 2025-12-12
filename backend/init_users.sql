-- 学生管理系统 - 初始化用户数据
-- 使用数据库
USE student_manage;

-- 3. 插入管理员账号（密码：123）
-- 注意：如果已存在会报错，可以忽略或先删除
INSERT INTO
    users (
        username,
        password,
        real_name,
        role,
        status,
        created_at,
        updated_at
    )
VALUES (
        'admin',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
        '系统管理员',
        'ADMIN',
        1,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE
    password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH';

-- 4. 插入一个普通用户用于测试（密码：123）
INSERT INTO
    users (
        username,
        password,
        real_name,
        role,
        status,
        created_at,
        updated_at
    )
VALUES (
        'testuser',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
        '测试用户',
        'USER',
        1,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE
    password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH';

-- 5. 查看插入结果
SELECT * FROM users;