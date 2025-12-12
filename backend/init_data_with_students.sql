-- 学生管理系统 - 完整初始化数据
-- 包含管理员、测试用户和测试学生数据
USE student_manage;

-- ========================================
-- 1. 清理现有数据（可选，谨慎使用）
-- ========================================
-- DELETE FROM users WHERE username != 'admin';
-- DELETE FROM students;

-- ========================================
-- 2. 插入管理员账号（密码：123）
-- ========================================
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

-- ========================================
-- 3. 插入测试学生数据
-- ========================================
-- 计算机1班学生
INSERT INTO
    students (
        student_number,
        name,
        gender,
        age,
        class_name
    )
VALUES (
        '2024001',
        '张三',
        '男',
        20,
        '计算机1班'
    ),
    (
        '2024002',
        '李四',
        '男',
        19,
        '计算机1班'
    ),
    (
        '2024003',
        '王五',
        '女',
        20,
        '计算机1班'
    ),
    (
        '2024004',
        '赵六',
        '男',
        21,
        '计算机1班'
    ),
    (
        '2024005',
        '钱七',
        '女',
        19,
        '计算机1班'
    );

-- 计算机2班学生
INSERT INTO
    students (
        student_number,
        name,
        gender,
        age,
        class_name
    )
VALUES (
        '2024006',
        '孙八',
        '男',
        20,
        '计算机2班'
    ),
    (
        '2024007',
        '周九',
        '女',
        19,
        '计算机2班'
    ),
    (
        '2024008',
        '吴十',
        '男',
        20,
        '计算机2班'
    ),
    (
        '2024009',
        '郑十一',
        '女',
        21,
        '计算机2班'
    ),
    (
        '2024010',
        '王十二',
        '男',
        19,
        '计算机2班'
    );

-- 软件工程1班学生
INSERT INTO
    students (
        student_number,
        name,
        gender,
        age,
        class_name
    )
VALUES (
        '2024011',
        '刘明',
        '男',
        20,
        '软件工程1班'
    ),
    (
        '2024012',
        '陈红',
        '女',
        19,
        '软件工程1班'
    ),
    (
        '2024013',
        '杨刚',
        '男',
        21,
        '软件工程1班'
    ),
    (
        '2024014',
        '黄丽',
        '女',
        20,
        '软件工程1班'
    ),
    (
        '2024015',
        '林强',
        '男',
        19,
        '软件工程1班'
    );

-- 数据科学1班学生
INSERT INTO
    students (
        student_number,
        name,
        gender,
        age,
        class_name
    )
VALUES (
        '2024016',
        '徐静',
        '女',
        20,
        '数据科学1班'
    ),
    (
        '2024017',
        '朱伟',
        '男',
        19,
        '数据科学1班'
    ),
    (
        '2024018',
        '胡敏',
        '女',
        20,
        '数据科学1班'
    ),
    (
        '2024019',
        '郭涛',
        '男',
        21,
        '数据科学1班'
    ),
    (
        '2024020',
        '何娟',
        '女',
        19,
        '数据科学1班'
    );

-- ========================================
-- 4. 为每个学生创建对应的User账号（密码：123）
-- ========================================
-- 计算机1班用户
INSERT INTO
    users (
        username,
        password,
        real_name,
        role,
        student_id,
        status,
        created_at,
        updated_at
    )
SELECT s.student_number, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', s.name, 'USER', s.id, 1, NOW(), NOW()
FROM students s
WHERE
    s.student_number IN (
        '2024001',
        '2024002',
        '2024003',
        '2024004',
        '2024005'
    );

-- 计算机2班用户
INSERT INTO
    users (
        username,
        password,
        real_name,
        role,
        student_id,
        status,
        created_at,
        updated_at
    )
SELECT s.student_number, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', s.name, 'USER', s.id, 1, NOW(), NOW()
FROM students s
WHERE
    s.student_number IN (
        '2024006',
        '2024007',
        '2024008',
        '2024009',
        '2024010'
    );

-- 软件工程1班用户
INSERT INTO
    users (
        username,
        password,
        real_name,
        role,
        student_id,
        status,
        created_at,
        updated_at
    )
SELECT s.student_number, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', s.name, 'USER', s.id, 1, NOW(), NOW()
FROM students s
WHERE
    s.student_number IN (
        '2024011',
        '2024012',
        '2024013',
        '2024014',
        '2024015'
    );

-- 数据科学1班用户
INSERT INTO
    users (
        username,
        password,
        real_name,
        role,
        student_id,
        status,
        created_at,
        updated_at
    )
SELECT s.student_number, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', s.name, 'USER', s.id, 1, NOW(), NOW()
FROM students s
WHERE
    s.student_number IN (
        '2024016',
        '2024017',
        '2024018',
        '2024019',
        '2024020'
    );

-- ========================================
-- 5. 插入一个普通管理员用于测试角色管理（密码：123）
-- ========================================
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
        'manager',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
        '普通管理员',
        'ADMIN',
        1,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE
    password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH';

-- ========================================
-- 6. 查看插入结果
-- ========================================
SELECT '=== 用户统计 ===' AS info;

SELECT role, COUNT(*) as count FROM users GROUP BY role;

SELECT '=== 班级统计 ===' AS info;

SELECT class_name, COUNT(*) as count
FROM students
GROUP BY
    class_name;

SELECT '=== 所有用户 ===' AS info;

SELECT
    id,
    username,
    real_name,
    role,
    student_id
FROM users
ORDER BY id;

SELECT '=== 所有学生 ===' AS info;

SELECT
    id,
    student_number,
    name,
    gender,
    age,
    class_name
FROM students
ORDER BY id;