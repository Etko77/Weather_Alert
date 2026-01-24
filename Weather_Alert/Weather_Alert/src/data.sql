-- =====================================================
-- Weather Alert Service - Initial Data
-- Този файл съдържа INSERT statements за начални данни
-- =====================================================

-- =====================================================
-- ROLES - Начални роли
-- =====================================================
INSERT INTO roles (name, description) VALUES 
    ('ROLE_ADMIN', 'Administrator with full access to all CRUD operations'),
    ('ROLE_USER', 'Regular user with read-only access');

-- =====================================================
-- USERS - Тестови потребители
-- Паролите са криптирани с BCrypt
-- Plain passwords:
--   admin -> admin123
--   user1 -> user123
--   user2 -> user123
-- =====================================================

-- Admin потребител
INSERT INTO users (username, email, password, enabled) VALUES 
    ('admin', 'admin@weatheralert.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqjT1K1Vv1J2WfG1qwQ1sV1r1s1t1u1', TRUE);

-- Regular потребители
INSERT INTO users (username, email, password, enabled) VALUES 
    ('user1', 'user1@weatheralert.com', '$2a$10$EqKcp1xnXnMRzr5ZLOIKIe.8J8N8N8N8N8N8N8N8N8N8N8N8N8N8', TRUE),
    ('user2', 'user2@weatheralert.com', '$2a$10$EqKcp1xnXnMRzr5ZLOIKIe.8J8N8N8N8N8N8N8N8N8N8N8N8N8N8', TRUE);

-- Тестов потребител със заключен акаунт
INSERT INTO users (username, email, password, enabled, account_non_locked) VALUES 
    ('locked_user', 'locked@weatheralert.com', '$2a$10$EqKcp1xnXnMRzr5ZLOIKIe.8J8N8N8N8N8N8N8N8N8N8N8N8N8N8', TRUE, FALSE);

-- Деактивиран потребител
INSERT INTO users (username, email, password, enabled) VALUES 
    ('disabled_user', 'disabled@weatheralert.com', '$2a$10$EqKcp1xnXnMRzr5ZLOIKIe.8J8N8N8N8N8N8N8N8N8N8N8N8N8N8', FALSE);

-- =====================================================
-- USER_ROLES - Присвояване на роли
-- =====================================================

-- Admin получава ROLE_ADMIN
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- Admin получава и ROLE_USER (опционално, за пълен достъп)
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_USER';

-- Regular users получават ROLE_USER
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'user1' AND r.name = 'ROLE_USER';

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'user2' AND r.name = 'ROLE_USER';

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'locked_user' AND r.name = 'ROLE_USER';

INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'disabled_user' AND r.name = 'ROLE_USER';

-- =====================================================
-- ALERTS - Примерни weather alerts
-- =====================================================

-- Alerts с успешно geo-tagging (координатите са предварително попълнени за демонстрация)
INSERT INTO alerts (description, location_name, latitude, longitude, severity_level, geo_tagging_status, created_by) 
SELECT 
    'Heavy snowfall expected. Road conditions may be hazardous.',
    'Sofia',
    42.6977,
    23.3219,
    'HIGH',
    'SUCCESS',
    u.id
FROM users u WHERE u.username = 'admin';

INSERT INTO alerts (description, location_name, latitude, longitude, severity_level, geo_tagging_status, created_by) 
SELECT 
    'Strong winds expected in the afternoon. Secure loose objects.',
    'Berlin Central Park',
    52.5200,
    13.4050,
    'MEDIUM',
    'SUCCESS',
    u.id
FROM users u WHERE u.username = 'admin';

INSERT INTO alerts (description, location_name, latitude, longitude, severity_level, geo_tagging_status, created_by) 
SELECT 
    'Light rain expected throughout the day. Carry an umbrella.',
    'Plovdiv',
    42.1354,
    24.7453,
    'LOW',
    'SUCCESS',
    u.id
FROM users u WHERE u.username = 'user1';

INSERT INTO alerts (description, location_name, latitude, longitude, severity_level, geo_tagging_status, created_by) 
SELECT 
    'Heat wave warning. Stay hydrated and avoid outdoor activities.',
    'Varna',
    43.2141,
    27.9147,
    'HIGH',
    'SUCCESS',
    u.id
FROM users u WHERE u.username = 'user1';

-- Alert с pending geo-tagging (симулира нов alert)
INSERT INTO alerts (description, location_name, severity_level, geo_tagging_status, created_by) 
SELECT 
    'Fog warning for early morning hours. Drive carefully.',
    'Burgas',
    'MEDIUM',
    'PENDING',
    u.id
FROM users u WHERE u.username = 'admin';

-- Alert с failed geo-tagging (невалидна локация)
INSERT INTO alerts (description, location_name, severity_level, geo_tagging_status, geo_tagging_error, created_by) 
SELECT 
    'Thunderstorm expected with possible hail.',
    'NonExistent Location XYZ',
    'HIGH',
    'FAILED',
    'No coordinates found for the specified location',
    u.id
FROM users u WHERE u.username = 'user2';

-- Още няколко alerts за разнообразие
INSERT INTO alerts (description, location_name, latitude, longitude, severity_level, geo_tagging_status, created_by) 
SELECT 
    'Flash flood warning in low-lying areas.',
    'Stara Zagora',
    42.4258,
    25.6345,
    'HIGH',
    'SUCCESS',
    u.id
FROM users u WHERE u.username = 'admin';

INSERT INTO alerts (description, location_name, latitude, longitude, severity_level, geo_tagging_status, created_by) 
SELECT 
    'Air quality alert due to high pollen count.',
    'Ruse',
    43.8356,
    25.9657,
    'LOW',
    'SUCCESS',
    u.id
FROM users u WHERE u.username = 'user1';

INSERT INTO alerts (description, location_name, latitude, longitude, severity_level, geo_tagging_status, created_by) 
SELECT 
    'Ice storm warning. Avoid unnecessary travel.',
    'Munich',
    48.1351,
    11.5820,
    'HIGH',
    'SUCCESS',
    u.id
FROM users u WHERE u.username = 'admin';

INSERT INTO alerts (description, location_name, latitude, longitude, severity_level, geo_tagging_status, created_by) 
SELECT 
    'UV index extremely high. Use sunscreen and protective clothing.',
    'Athens',
    37.9838,
    23.7275,
    'MEDIUM',
    'SUCCESS',
    u.id
FROM users u WHERE u.username = 'user2';

-- =====================================================
-- VERIFICATION QUERIES (опционални, за проверка)
-- Разкоментирай за да провериш данните
-- =====================================================

-- SELECT 'Roles count:' as info, COUNT(*) as count FROM roles;
-- SELECT 'Users count:' as info, COUNT(*) as count FROM users;
-- SELECT 'User-Roles count:' as info, COUNT(*) as count FROM user_roles;
-- SELECT 'Alerts count:' as info, COUNT(*) as count FROM alerts;

-- SELECT u.username, array_agg(r.name) as roles
-- FROM users u
-- JOIN user_roles ur ON u.id = ur.user_id
-- JOIN roles r ON r.id = ur.role_id
-- GROUP BY u.username;

-- SELECT location_name, severity_level, geo_tagging_status, latitude, longitude
-- FROM alerts
-- ORDER BY created_at DESC;
