-- =====================================================================
-- Smart City Navigation Platform - MySQL Database Schema
-- =====================================================================
-- Engine: InnoDB (required for FK support)
-- Charset: utf8mb4
-- =====================================================================

CREATE DATABASE IF NOT EXISTS smart_city_navigation
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE smart_city_navigation;

SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
-- 1. ROLES
-- ---------------------------------------------------------------------
CREATE TABLE roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(30) NOT NULL UNIQUE,   -- 'CITIZEN', 'ADMIN'
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. USERS
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role_id         BIGINT NOT NULL,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    INDEX idx_users_email (email)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. REFRESH TOKENS (supports JWT refresh flow)
-- ---------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    token        VARCHAR(500) NOT NULL UNIQUE,
    expires_at   TIMESTAMP NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked      BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    INDEX idx_refresh_token (token)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. LOCATIONS
-- ---------------------------------------------------------------------
CREATE TABLE locations (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    type        ENUM('HOSPITAL','RESTAURANT','AIRPORT','HOTEL','SCHOOL',
                      'BUS_STOP','ATM','GENERIC') NOT NULL DEFAULT 'GENERIC',
    latitude    DECIMAL(10,7) NOT NULL,
    longitude   DECIMAL(10,7) NOT NULL,
    geo_point   POINT NOT NULL SRID 4326,       -- spatial column for nearest-neighbor queries
    address     VARCHAR(255),
    created_by  BIGINT,                          -- admin who added it
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_locations_creator FOREIGN KEY (created_by) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE,

    SPATIAL INDEX idx_locations_geo (geo_point),
    INDEX idx_locations_type (type),
    INDEX idx_locations_name (name)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. ROADS (graph edges between locations)
-- ---------------------------------------------------------------------
CREATE TABLE roads (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_location_id    BIGINT NOT NULL,
    to_location_id      BIGINT NOT NULL,
    base_distance_km    DECIMAL(8,3) NOT NULL,      -- static weight
    base_speed_kmph     DECIMAL(6,2) NOT NULL DEFAULT 40.00,
    current_weight      DECIMAL(8,3) NOT NULL,      -- dynamic weight (mutated by traffic)
    is_bidirectional    BOOLEAN DEFAULT TRUE,
    is_closed           BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_roads_from FOREIGN KEY (from_location_id) REFERENCES locations(id)
        ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_roads_to FOREIGN KEY (to_location_id) REFERENCES locations(id)
        ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT chk_roads_not_self CHECK (from_location_id <> to_location_id),

    INDEX idx_roads_from (from_location_id),
    INDEX idx_roads_to (to_location_id),
    UNIQUE INDEX idx_roads_pair (from_location_id, to_location_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 6. TRAFFIC (dynamic events that reweight roads)
-- ---------------------------------------------------------------------
CREATE TABLE traffic (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    road_id             BIGINT NOT NULL,
    traffic_level       ENUM('LOW','MODERATE','HEAVY','SEVERE') NOT NULL DEFAULT 'LOW',
    congestion_factor   DECIMAL(4,2) NOT NULL DEFAULT 1.00,  -- multiplier applied to current_weight
    current_speed_kmph  DECIMAL(6,2),
    is_road_closed      BOOLEAN DEFAULT FALSE,
    triggered_by        BIGINT,                                -- admin/system user id, nullable
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at          TIMESTAMP NULL,

    CONSTRAINT fk_traffic_road FOREIGN KEY (road_id) REFERENCES roads(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_traffic_user FOREIGN KEY (triggered_by) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE,

    INDEX idx_traffic_road (road_id),
    INDEX idx_traffic_created (created_at)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 7. ROUTE_HISTORY
-- ---------------------------------------------------------------------
CREATE TABLE route_history (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    from_location_id    BIGINT NOT NULL,
    to_location_id      BIGINT NOT NULL,
    algorithm_used      ENUM('DIJKSTRA','ASTAR','BFS') NOT NULL DEFAULT 'DIJKSTRA',
    total_distance_km   DECIMAL(8,3),
    estimated_time_min  DECIMAL(8,2),
    path_json           JSON,                 -- ordered list of location ids in the path
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_history_from FOREIGN KEY (from_location_id) REFERENCES locations(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_history_to FOREIGN KEY (to_location_id) REFERENCES locations(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    INDEX idx_history_user (user_id),
    INDEX idx_history_created (created_at)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 8. FAVORITE_PLACES
-- ---------------------------------------------------------------------
CREATE TABLE favorite_places (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    location_id   BIGINT NOT NULL,
    nickname      VARCHAR(100),           -- e.g. "Home", "Office"
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_favorite_location FOREIGN KEY (location_id) REFERENCES locations(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    UNIQUE INDEX idx_favorite_unique (user_id, location_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 9. ANALYTICS_LOGS (supports Analytics Dashboard module)
-- ---------------------------------------------------------------------
CREATE TABLE analytics_logs (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type    ENUM('ROUTE_REQUEST','NEARBY_SEARCH','TRAFFIC_UPDATE','SEARCH_QUERY') NOT NULL,
    user_id       BIGINT,
    location_id   BIGINT,
    road_id       BIGINT,
    metadata_json JSON,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_analytics_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_analytics_location FOREIGN KEY (location_id) REFERENCES locations(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_analytics_road FOREIGN KEY (road_id) REFERENCES roads(id)
        ON DELETE SET NULL ON UPDATE CASCADE,

    INDEX idx_analytics_type (event_type),
    INDEX idx_analytics_created (created_at)
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- TRIGGERS: keep geo_point in sync with latitude/longitude automatically.
-- The JPA `Location` entity intentionally does NOT map geo_point directly
-- (it's a MySQL-specific spatial type) - these triggers mean the admin
-- panel (which inserts/updates via JPA using only lat/lng) still produces
-- a valid, indexed geo_point without the application layer knowing about it.
-- =====================================================================
DELIMITER $$

CREATE TRIGGER trg_locations_before_insert
BEFORE INSERT ON locations
FOR EACH ROW
BEGIN
    IF NEW.geo_point IS NULL THEN
        SET NEW.geo_point = ST_SRID(POINT(NEW.longitude, NEW.latitude), 4326);
    END IF;
END$$

CREATE TRIGGER trg_locations_before_update
BEFORE UPDATE ON locations
FOR EACH ROW
BEGIN
    IF NEW.latitude <> OLD.latitude OR NEW.longitude <> OLD.longitude THEN
        SET NEW.geo_point = ST_SRID(POINT(NEW.longitude, NEW.latitude), 4326);
    END IF;
END$$

DELIMITER ;

-- =====================================================================
-- SEED DATA: Roles
-- =====================================================================
INSERT INTO roles (name) VALUES ('CITIZEN'), ('ADMIN');
