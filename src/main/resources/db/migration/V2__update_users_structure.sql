-- Миграция для обновления структуры пользователей
-- Добавляем недостающие поля и изменяем структуру ролей

-- Добавляем поле name в таблицу users
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(100) NOT NULL DEFAULT 'Unknown';

-- Добавляем поле active в таблицу users
ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true;

-- Добавляем поле role напрямую в таблицу users (вместо связующей таблицы)
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Удаляем старую структуру ролей (если она не используется)
-- DROP TABLE IF EXISTS user_roles;
-- DROP TABLE IF EXISTS roles;

-- Обновляем существующие записи, устанавливая роль USER по умолчанию
UPDATE users SET role = 'USER' WHERE role IS NULL OR role = '';

-- Создаем индексы для оптимизации поиска
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
