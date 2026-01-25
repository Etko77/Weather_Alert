-- =====================================================
-- Weather Alert Service - Initial Data
-- Database: MariaDB
-- =====================================================

-- =====================================================
-- ROLES - Начални роли
-- =====================================================
INSERT INTO roles (name, description) VALUES
                                          ('ROLE_ADMIN', 'Administrator with full access to all CRUD operations'),
                                          ('ROLE_USER', 'Regular user with read-only access');

-- =====================================================
-- USERS - Тестови потребители (BCrypt passwords)
-- =====================================================

-- Admin
INSERT INTO users (username, email, password, enabled) VALUES
    ('admin', 'admin@weatheralert.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqjT1K1Vv1J2WfG1qwQ1sV1r1s1t1u1', TRUE);

-- Regular users
INSERT INTO users (username, email, password, enabled) VALUES
                                                           ('user1', 'user1@weatheralert.com',
                                                            '$2a$10$EqKcp1xnXnMRzr5ZLOIKIe.8J8N8N8N8N8N8N8N8N8N8N8N8N8', TRUE),
                                                           ('user2', 'user2@weatheralert.com',
                                                            '$2a$10$EqKcp1xnXnMRzr5ZLOIKIe.8J8N8N8N8N8N8N8N8N8N8N8N8N8', TRUE);

-- Locked user
INSERT INTO users (username, email, password, enabled, account_non_locked) VALUES
    ('locked_user', 'locked@weatheralert.com',
     '$2a$10$EqKcp1xnXnMRzr5ZLOIKIe.8J8N8N8N8N8N8N8N8N8N8N8N8N8', TRUE, FALSE);

-- Disabled user
INSERT INTO users (username, email, password, enabled) VALUES
    ('disabled_user', 'disabled@weatheralert.com',
     '$2a$10$EqKcp1xnXnMRzr5ZLOIKIe.8J8N8N8N8N8N8N8N8N8N8N8N8N8', FALSE);

-- =====================================================
-- USER_ROLES
-- =====================================================

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_USER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'user1' AND r.name = 'ROLE_USER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'user2' AND r.name = 'ROLE_USER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'locked_user' AND r.name = 'ROLE_USER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'disabled_user' AND r.name = 'ROLE_USER';

-- =====================================================
-- ALERTS
-- =====================================================

INSERT INTO alerts (description, location_name, latitude, longitude,
                    severity_level, geo_tagging_status, created_by)
SELECT
    'Heavy snowfall expected. Road conditions may be hazardous.',
    'Sofia', 42.6977, 23.3219,
    'HIGH', 'SUCCESS', u.id
FROM users u WHERE u.username = 'admin';

INSERT INTO alerts (description, location_name, latitude, longitude,
                    severity_level, geo_tagging_status, created_by)
SELECT
    'Strong winds expected in the afternoon. Secure loose objects.',
    'Berlin Central Park', 52.5200, 13.4050,
    'MEDIUM', 'SUCCESS', u.id
FROM users u WHERE u.username = 'admin';

INSERT INTO alerts (description, location_name, latitude, longitude,
                    severity_level, geo_tagging_status, created_by)
SELECT
    'Light rain expected throughout the day. Carry an umbrella.',
    'Plovdiv', 42.1354, 24.7453,
    'LOW', 'SUCCESS', u.id
FROM users u WHERE u.username = 'user1';

INSERT INTO alerts (description, location_name, latitude, longitude,
                    severity_level, geo_tagging_status, created_by)
SELECT
    'Heat wave warning. Stay hydrated and avoid outdoor activities.',
    'Varna', 43.2141, 27.9147,
    'HIGH', 'SUCCESS', u.id
FROM users u WHERE u.username = 'user1';

INSERT INTO alerts (description, location_name,
                    severity_level, geo_tagging_status, created_by)
SELECT
    'Fog warning for early morning hours. Drive carefully.',
    'Burgas', 'MEDIUM', 'PENDING', u.id
FROM users u WHERE u.username = 'admin';

INSERT INTO alerts (description, location_name,
                    severity_level, geo_tagging_status, geo_tagging_error, created_by)
SELECT
    'Thunderstorm expected with possible hail.',
    'NonExistent Location XYZ',
    'HIGH', 'FAILED',
    'No coordinates found for the specified location',
    u.id
FROM users u WHERE u.username = 'user2';

INSERT INTO alerts (description, location_name, latitude, longitude,
                    severity_level, geo_tagging_status, created_by)
SELECT
    'Flash flood warning in low-lying areas.',
    'Stara Zagora', 42.4258, 25.6345,
    'HIGH', 'SUCCESS', u.id
FROM users u WHERE u.username = 'admin';

INSERT INTO alerts (description, location_name, latitude, longitude,
                    severity_level, geo_tagging_status, created_by)
SELECT
    'Air quality alert due to high pollen count.',
    'Ruse', 43.8356, 25.9657,
    'LOW', 'SUCCESS', u.id
FROM users u WHERE u.username = 'user1';

INSERT INTO alerts (description, location_name, latitude, longitude,
                    severity_level, geo_tagging_status, created_by)
SELECT
    'Ice storm warning. Avoid unnecessary travel.',
    'Munich', 48.1351, 11.5820,
    'HIGH', 'SUCCESS', u.id
FROM users u WHERE u.username = 'admin';

INSERT INTO alerts (description, location_name, latitude, longitude,
                    severity_level, geo_tagging_status, created_by)
SELECT
    'UV index extremely high. Use sunscreen and protective clothing.',
    'Athens', 37.9838, 23.7275,
    'MEDIUM', 'SUCCESS', u.id
FROM users u WHERE u.username = 'user2';

-- =====================================================
-- VERIFICATION (MariaDB compatible)
-- =====================================================

-- SELECT COUNT(*) FROM roles;
-- SELECT COUNT(*) FROM users;
-- SELECT COUNT(*) FROM user_roles;
-- SELECT COUNT(*) FROM alerts;

-- SELECT u.username, GROUP_CONCAT(r.name SEPARATOR ', ') AS roles
-- FROM users u
-- JOIN user_roles ur ON u.id = ur.user_id
-- JOIN roles r ON r.id = ur.role_id
-- GROUP BY u.username;
