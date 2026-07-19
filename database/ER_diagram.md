# Smart City Navigation Platform — ER Diagram

```mermaid
erDiagram
    ROLES ||--o{ USERS : "has"
    USERS ||--o{ REFRESH_TOKENS : "owns"
    USERS ||--o{ LOCATIONS : "creates (admin)"
    USERS ||--o{ ROUTE_HISTORY : "requests"
    USERS ||--o{ FAVORITE_PLACES : "saves"
    USERS ||--o{ TRAFFIC : "triggers (admin)"
    USERS ||--o{ ANALYTICS_LOGS : "generates"

    LOCATIONS ||--o{ ROADS : "from_location"
    LOCATIONS ||--o{ ROADS : "to_location"
    LOCATIONS ||--o{ ROUTE_HISTORY : "from_location"
    LOCATIONS ||--o{ ROUTE_HISTORY : "to_location"
    LOCATIONS ||--o{ FAVORITE_PLACES : "favorited"
    LOCATIONS ||--o{ ANALYTICS_LOGS : "referenced"

    ROADS ||--o{ TRAFFIC : "has events"
    ROADS ||--o{ ANALYTICS_LOGS : "referenced"

    ROLES {
        bigint id PK
        varchar name
    }
    USERS {
        bigint id PK
        varchar name
        varchar email
        varchar password_hash
        bigint role_id FK
        boolean is_active
    }
    REFRESH_TOKENS {
        bigint id PK
        bigint user_id FK
        varchar token
        timestamp expires_at
        boolean revoked
    }
    LOCATIONS {
        bigint id PK
        varchar name
        enum type
        decimal latitude
        decimal longitude
        point geo_point
        bigint created_by FK
    }
    ROADS {
        bigint id PK
        bigint from_location_id FK
        bigint to_location_id FK
        decimal base_distance_km
        decimal current_weight
        boolean is_closed
    }
    TRAFFIC {
        bigint id PK
        bigint road_id FK
        enum traffic_level
        decimal congestion_factor
        bigint triggered_by FK
    }
    ROUTE_HISTORY {
        bigint id PK
        bigint user_id FK
        bigint from_location_id FK
        bigint to_location_id FK
        enum algorithm_used
        decimal total_distance_km
        json path_json
    }
    FAVORITE_PLACES {
        bigint id PK
        bigint user_id FK
        bigint location_id FK
        varchar nickname
    }
    ANALYTICS_LOGS {
        bigint id PK
        enum event_type
        bigint user_id FK
        bigint location_id FK
        bigint road_id FK
        json metadata_json
    }
```

## Relationship Summary

| Relationship | Type | Notes |
|---|---|---|
| Role → Users | 1:N | Each user has exactly one role (CITIZEN/ADMIN) |
| User → RefreshTokens | 1:N | A user can have multiple active/expired refresh tokens |
| User → Locations | 1:N (nullable) | Tracks which admin created a location |
| Location → Roads (from) | 1:N | A location can be the source of many roads |
| Location → Roads (to) | 1:N | A location can be the destination of many roads |
| Road → Traffic | 1:N | A road can have a history of traffic events over time |
| User → RouteHistory | 1:N | A user's past route requests |
| User → FavoritePlaces | 1:N | Unique per (user, location) pair |
| User/Location/Road → AnalyticsLogs | 1:N each (nullable) | Central event log for the analytics dashboard |

## Design Notes

- **`geo_point` (SPATIAL, SRID 4326)** on `locations` enables `ST_Distance_Sphere()` queries for the Nearby Services module — MySQL's native equivalent of MongoDB's `2dsphere` index.
- **`roads.current_weight`** is the field the DSA engine reads when building the in-memory graph. `base_distance_km` never changes; `current_weight` is what Traffic Simulation mutates.
- **`route_history.path_json`** stores the ordered node list so history can be replayed on the map without recomputing the route.
- **Unique index on `roads(from_location_id, to_location_id)`** prevents duplicate edges between the same two nodes.
