-- =====================================================
-- Weather Alert Service - Database Schema
-- Database: MariaDB
-- =====================================================

-- Drop tables if exist (в правилен ред заради foreign keys)
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS alerts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- =====================================================
-- ROLES TABLE
-- =====================================================
CREATE TABLE roles (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(255),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_roles_name ON roles(name);

-- =====================================================
-- USERS TABLE
-- =====================================================
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       enabled BOOLEAN DEFAULT TRUE,
                       account_non_expired BOOLEAN DEFAULT TRUE,
                       account_non_locked BOOLEAN DEFAULT TRUE,
                       credentials_non_expired BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- =====================================================
-- USER_ROLES TABLE (Many-to-Many)
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
) ENGINE=InnoDB;

-- =====================================================
-- ALERTS TABLE
-- =====================================================
CREATE TABLE alerts (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        description VARCHAR(1000) NOT NULL,
                        location_name VARCHAR(255) NOT NULL,
                        latitude DOUBLE,
                        longitude DOUBLE,
                        severity_level ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
                        geo_tagging_status ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING',
                        geo_tagging_error VARCHAR(500),
                        created_by BIGINT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_alerts_created_by
                            FOREIGN KEY (created_by)
                                REFERENCES users(id)
                                ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_alerts_location ON alerts(location_name);
CREATE INDEX idx_alerts_severity ON alerts(severity_level);
CREATE INDEX idx_alerts_created_at ON alerts(created_at);
CREATE INDEX idx_alerts_geo_status ON alerts(geo_tagging_status);

-- =====================================================
-- TRIGGERS за автоматично обновяване на updated_at
-- =====================================================
DELIMITER $$

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$

CREATE TRIGGER update_alerts_updated_at
    BEFORE UPDATE ON alerts
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$

DELIMITER ;

-- =====================================================
-- COMMENTS
-- =====================================================
ALTER TABLE roles COMMENT = 'Роли в системата за authorization';
ALTER TABLE users COMMENT = 'Потребители на системата';
ALTER TABLE user_roles COMMENT = 'Връзка много-към-много между users и roles';
ALTER TABLE alerts COMMENT = 'Weather alerts с geo-tagging информация';
