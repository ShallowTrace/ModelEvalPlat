INSERT INTO users (
    username,
    password,
    email,
    role,
    is_active,
    gender,
    avatar_url,
    department,
    created_at,
    last_login_at
)
VALUES (
           'admin',
           '123456',
           '123@qq.com',
           'admin',
           1,
           'male',
           'https://example.com/avatar.jpg',
           'Administration',
           '2025-08-12 16:24:50',
           '2025-08-12 16:24:52'
       );