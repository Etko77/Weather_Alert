-- =====================================================
-- Weather Alert Service - Database Schema
-- Database: PostgreSQL (може да се адаптира за MySQL/H2)
-- =====================================================

-- Drop tables if exist (в правилен ред заради foreign keys)
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS alerts CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- =====================================================
-- ROLES TABLE
-- Съхранява ролите в системата (ADMIN, USER)
-- =====================================================
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index за бързо търсене по име
CREATE INDEX idx_roles_name ON roles(name);

-- =====================================================
-- USERS TABLE
-- Съхранява потребителите в системата
-- =====================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes за users
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- =====================================================
-- USER_ROLES TABLE (Many-to-Many relationship)
-- Свързва потребители с роли
-- =====================================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role 
        FOREIGN KEY (role_id) 
        REFERENCES roles(id) 
        ON DELETE CASCADE
);

-- =====================================================
-- ALERTS TABLE
-- Съхранява weather alerts
-- =====================================================
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(1000) NOT NULL,
    location_name VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    severity_level VARCHAR(20) NOT NULL,
    geo_tagging_status VARCHAR(20) DEFAULT 'PENDING',
    geo_tagging_error VARCHAR(500),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_severity_level 
        CHECK (severity_level IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT chk_geo_tagging_status 
        CHECK (geo_tagging_status IN ('PENDING', 'SUCCESS', 'FAILED')),
    CONSTRAINT fk_alerts_created_by 
        FOREIGN KEY (created_by) 
        REFERENCES users(id) 
        ON DELETE SET NULL
);

-- Indexes за alerts
CREATE INDEX idx_alerts_location ON alerts(location_name);
CREATE INDEX idx_alerts_severity ON alerts(severity_level);
CREATE INDEX idx_alerts_created_at ON alerts(created_at DESC);
CREATE INDEX idx_alerts_geo_status ON alerts(geo_tagging_status);

-- =====================================================
-- ФУНКЦИЯ ЗА АВТОМАТИЧНО ОБНОВЯВАНЕ НА updated_at
-- (PostgreSQL специфично)
-- =====================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers за автоматично обновяване на updated_at
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_alerts_updated_at
    BEFORE UPDATE ON alerts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- COMMENTS за документация
-- =====================================================
COMMENT ON TABLE roles IS 'Роли в системата за authorization';
COMMENT ON TABLE users IS 'Потребители на системата';
COMMENT ON TABLE user_roles IS 'Връзка много-към-много между users и roles';
COMMENT ON TABLE alerts IS 'Weather alerts с geo-tagging информация';

COMMENT ON COLUMN alerts.geo_tagging_status IS 'Статус на geo-tagging: PENDING, SUCCESS, FAILED';
COMMENT ON COLUMN alerts.latitude IS 'Географска ширина, попълва се асинхронно от Nominatim API';
COMMENT ON COLUMN alerts.longitude IS 'Географска дължина, попълва се асинхронно от Nominatim API';
