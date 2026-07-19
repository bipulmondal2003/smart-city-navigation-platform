-- =========================================================
-- Smart City Navigation Platform - Seed Data
-- Run AFTER schema.sql. This is a small FICTIONAL grid city
-- (not real map data), by design, per the project's DSA focus.
-- =========================================================

USE smart_city_navigation;

-- ---------------------------------------------------------
-- Users (password for BOTH accounts below is: Admin@123 / Citizen@123)
-- Hashes generated with BCrypt (10 rounds) - safe to use for local dev only.
-- ---------------------------------------------------------
INSERT INTO users (name, email, password_hash, role_id, is_active) VALUES
('City Admin', 'admin@smartcity.local',
 '$2b$10$T8C3ZueyftLUf0mDfrcTguyehg0rXIrUA0O0OyK.gxtkJPA/s.Diy',
 (SELECT id FROM roles WHERE name = 'ADMIN'), TRUE),
('Demo Citizen', 'citizen@smartcity.local',
 '$2b$10$0h6FsliXjDAptPc8qu/Fj.K05iIKMphbVA66CCFuSMrFI1/zV15P.',
 (SELECT id FROM roles WHERE name = 'CITIZEN'), TRUE);

-- ---------------------------------------------------------
-- Locations (id will auto-increment 1..15 in insertion order)
-- geo_point is derived from latitude/longitude via ST_GeomFromText
-- ---------------------------------------------------------
INSERT INTO locations (name, type, latitude, longitude, geo_point, address) VALUES
('City General Hospital',    'HOSPITAL',   26.7271, 88.3953, ST_SRID(POINT(88.3953, 26.7271), 4326), 'Central District'),
('Green Valley School',      'SCHOOL',     26.7301, 88.3990, ST_SRID(POINT(88.3990, 26.7301), 4326), 'North Sector'),
('Riverside Restaurant',     'RESTAURANT', 26.7245, 88.3920, ST_SRID(POINT(88.3920, 26.7245), 4326), 'Riverside Ave'),
('Grand Central Hotel',      'HOTEL',      26.7288, 88.3975, ST_SRID(POINT(88.3975, 26.7288), 4326), 'Downtown'),
('Metro International Airport','AIRPORT',  26.7180, 88.3850, ST_SRID(POINT(88.3850, 26.7180), 4326), 'Airport Road'),
('Central Bus Stop',         'BUS_STOP',   26.7260, 88.3945, ST_SRID(POINT(88.3945, 26.7260), 4326), 'Main Street'),
('Northside ATM',            'ATM',        26.7315, 88.3960, ST_SRID(POINT(88.3960, 26.7315), 4326), 'North Sector'),
('Lakeview Hospital',        'HOSPITAL',   26.7330, 88.4010, ST_SRID(POINT(88.4010, 26.7330), 4326), 'Lakeview'),
('Sunrise High School',      'SCHOOL',     26.7205, 88.3900, ST_SRID(POINT(88.3900, 26.7205), 4326), 'West Sector'),
('Spice Route Restaurant',   'RESTAURANT', 26.7250, 88.4000, ST_SRID(POINT(88.4000, 26.7250), 4326), 'East Market'),
('Harborview Hotel',         'HOTEL',      26.7195, 88.3960, ST_SRID(POINT(88.3960, 26.7195), 4326), 'South Sector'),
('East Side Bus Stop',       'BUS_STOP',   26.7275, 88.4005, ST_SRID(POINT(88.4005, 26.7275), 4326), 'East Sector'),
('Downtown ATM',             'ATM',        26.7290, 88.3945, ST_SRID(POINT(88.3945, 26.7290), 4326), 'Downtown'),
('West Park',                'GENERIC',    26.7220, 88.3930, ST_SRID(POINT(88.3930, 26.7220), 4326), 'West Sector'),
('Tech Park Plaza',          'GENERIC',    26.7255, 88.3975, ST_SRID(POINT(88.3975, 26.7255), 4326), 'Central District');

-- ---------------------------------------------------------
-- Roads (bidirectional grid connecting the above locations)
-- distances are approximate straight-line km, rounded
-- ---------------------------------------------------------
INSERT INTO roads (from_location_id, to_location_id, base_distance_km, base_speed_kmph, current_weight, is_bidirectional, is_closed) VALUES
(1, 6, 0.8, 40, 0.8, TRUE, FALSE),
(1, 4, 1.2, 40, 1.2, TRUE, FALSE),
(1, 3, 1.0, 35, 1.0, TRUE, FALSE),
(6, 13, 0.9, 40, 0.9, TRUE, FALSE),
(6, 5, 2.5, 50, 2.5, TRUE, FALSE),
(4, 13, 0.6, 30, 0.6, TRUE, FALSE),
(4, 15, 0.7, 35, 0.7, TRUE, FALSE),
(4, 2, 1.5, 40, 1.5, TRUE, FALSE),
(2, 7, 0.9, 35, 0.9, TRUE, FALSE),
(2, 8, 1.8, 45, 1.8, TRUE, FALSE),
(15, 10, 1.1, 35, 1.1, TRUE, FALSE),
(15, 12, 1.4, 35, 1.4, TRUE, FALSE),
(10, 12, 0.8, 30, 0.8, TRUE, FALSE),
(10, 8, 1.6, 40, 1.6, TRUE, FALSE),
(3, 14, 1.0, 35, 1.0, TRUE, FALSE),
(3, 9, 1.7, 40, 1.7, TRUE, FALSE),
(14, 9, 0.9, 30, 0.9, TRUE, FALSE),
(14, 11, 1.3, 35, 1.3, TRUE, FALSE),
(9, 11, 1.5, 40, 1.5, TRUE, FALSE),
(11, 5, 1.9, 45, 1.9, TRUE, FALSE),
(5, 9, 2.1, 45, 2.1, TRUE, FALSE),
(13, 7, 1.2, 35, 1.2, TRUE, FALSE),
(12, 8, 1.0, 30, 0.9, TRUE, FALSE);
