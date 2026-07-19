# Smart City Navigation Platform

A DSA-focused city navigation system: graph-based route planning (Dijkstra / A*),
BFS-based reachability, a Trie-powered autocomplete, live traffic simulation with
dynamic edge reweighting, analytics (HashMap frequency counting + heap top-K), and
a role-gated admin panel.

## Tech Stack

**Frontend:** React + TypeScript, Tailwind CSS, Leaflet.js, Axios, TanStack Query, React Router
**Backend:** Spring Boot (Java 21), Spring Security + JWT, Spring Data JPA, Lombok
**Database:** MySQL 8 (spatial indexing + triggers for nearby-search / geo_point sync)

## Project Structure

```
smart-city-navigation-platform/
├── frontend/     React + TypeScript client
├── backend/      Spring Boot API + DSA engine
├── database/     schema.sql, seed_data.sql, ER diagram
├── docs/         architecture notes
└── README.md
```

## All Modules — Complete

- [x] **Auth** — register, login, refresh token, BCrypt password hashing, JWT, role-based access
- [x] **Interactive Map** — Leaflet, current-location, click-to-select, zoom, lat/lng readout
- [x] **DSA Engine** — `Node`/`Edge`/`CityGraph` adjacency list, in `algorithm/`
- [x] **BFS & DFS** — `GraphTraversal.java`; BFS is live via `GET /api/route/reachable`
- [x] **Dijkstra** — min-heap based, `GET /api/route?algorithm=dijkstra`
- [x] **A\*** — haversine heuristic, `GET /api/route?algorithm=astar`
- [x] **Trie** — location-name autocomplete, `GET /api/search?q=`
- [x] **Nearby Services** — haversine-sorted nearest-N, `GET /api/nearby`
- [x] **Traffic Simulation** — `POST /api/traffic`, `/simulate`, `/reset` (admin) — mutates the live graph so routes reroute immediately
- [x] **Route History** — auto-logged per logged-in user, `GET /api/history`
- [x] **Analytics Dashboard** — top routes/places (HashMap + min-heap top-K), averages, top-congested roads
- [x] **Admin Panel** — add/close/delete roads, add/delete locations, user list, dashboard stats — all `ROLE_ADMIN`-gated

## Seeded Demo Accounts

| Role | Email | Password |
|---|---|---|
| Admin | `admin@smartcity.local` | `Admin@123` |
| Citizen | `citizen@smartcity.local` | `Citizen@123` |

(Loaded by `database/seed_data.sql`. New registrations default to CITIZEN.)

## Running Locally

### 1. Database
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed_data.sql
```

### 2. Backend
Edit `backend/src/main/resources/application.properties`:
```properties
spring.datasource.password=YOUR_MYSQL_PASSWORD
app.jwt.secret=SOME_LONG_RANDOM_STRING
```
Then:
```bash
cd backend
mvn spring-boot:run
```
Runs on **http://localhost:8080**.

### 3. Frontend
```bash
cd frontend
npm install
cp .env.example .env
npm run dev
```
Runs on **http://localhost:5173** (proxies `/api` to the backend).

## Verified

- Frontend: `npm run build` (full `tsc` type-check + Vite production build) passes clean with zero errors.
- Backend: **confirmed working on a real machine** — `mvn spring-boot:run` starts cleanly (`Started SmartCityNavApplication`), graph builds from MySQL (`15 nodes, 23 roads loaded`), and login was tested end-to-end via `curl` and the browser.

## Fixes Applied During Testing

| Issue | Cause | Fix |
|---|---|---|
| Login always failed | `app.jwt.secret` was shorter than 256 bits (HMAC-SHA requires it) | Use a real random ≥32-character secret — **see below, this is required before first run** |
| Login errors all said "Invalid email or password" regardless of real cause | Frontend caught every error the same way | Now distinguishes 401 / 403 / network-down / backend message |
| Route Planner sidebar showed "Couldn't load locations" even when the map had data | Form and error banner were both gated on a stale `isError` flag instead of checking actual data | Both now key off whether data is actually present |
| Sessions would silently break after 15 minutes | No automatic token refresh was wired up | `apiClient` now transparently refreshes the access token on a 401 and retries the request; only forces logout if the refresh token itself is invalid |
| Registering with a 6–7 character password always failed with a confusing message | Frontend allowed `minLength=6`, backend requires 8 | Frontend now requires 8, and shows the backend's actual validation message |
| Route Planner, Admin panel, History, and Analytics all broke immediately after logging in | `JwtAuthFilter` reads the user's Role on every request carrying a token (i.e. every request once logged in), but Role was lazy-loaded and the DB session had already closed by the time it was accessed → `LazyInitializationException` on every single authenticated request | `UserRepository.findByEmail` now `JOIN FETCH`es the Role in the same query, so it's never a lazy proxy in the first place |
| The above error was invisible — a blank/broken response instead of a real error | `AuthEntryPoint` built its own bare `ObjectMapper()` to write the error JSON, which lacks the `JavaTimeModule` Spring Boot normally auto-registers, so serializing the error's own `LocalDateTime` timestamp threw a second, unrelated exception | `AuthEntryPoint` now uses Spring's auto-configured `ObjectMapper` bean instead of constructing its own |
| `schema.sql` failed outright with MySQL error 3823 on a fresh database, which cascaded into `seed_data.sql` failing too (roles were never seeded because the script aborted before reaching them) | MySQL disallows a `CHECK` constraint on a column that also has `ON UPDATE CASCADE` in a foreign key — `roads.from_location_id`/`to_location_id` had both | Changed those two FKs to `ON UPDATE RESTRICT` (location IDs are auto-increment and never updated in practice, so this changes nothing functionally) while keeping `ON DELETE CASCADE` and the self-loop `CHECK` |

### JWT secret
A working random secret is already set in `application.properties` (64 characters / 512 bits) — no setup needed to run this locally. For anything beyond local/demo use, generate your own and don't commit it:
```bash
python3 -c "import secrets,string; print(''.join(secrets.choice(string.ascii_letters+string.digits) for _ in range(64)))"
```

## Deploying Online

Three pieces need hosting: the React frontend (static), the Spring Boot backend (a running Java process), and MySQL (a database). The setup below needs **zero extra code changes beyond what's already in this repo** — the backend already reads its DB connection, port, JWT secret, and CORS origins from environment variables with safe local-dev defaults.

**Recommended: Railway (backend + MySQL) + Vercel (frontend).** Railway costs $5/month after a 30-day trial (their free tier was discontinued platform-wide in 2023); Vercel's static hosting is free. This is the path with the least setup friction for a two-service (API + DB) app like this one.

### 1. Push to GitHub
Both `frontend/` and `backend/` can live in one repo — each host is told which subfolder to build from.

### 2. Backend + Database on Railway
1. [railway.app](https://railway.app) → New Project → **Deploy from GitHub repo** → select this repo, set the root directory to `backend/`. Railway auto-detects Maven/Java and builds it.
2. In the same project, **+ New → Database → Add MySQL**.
3. Click your backend service → **Variables** tab → **Add Reference** → link the MySQL service. This auto-injects `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE` — which `application.properties` already reads automatically. No manual entry needed.
4. Optionally add a `JWT_SECRET` variable (a long random string) — see the generation command earlier in this README. Not required to function, but good practice for anything beyond a demo.
5. Load the schema: open the MySQL service's **Data** tab (or connect with any MySQL client using the credentials Railway shows you) and run `database/schema.sql` then `database/seed_data.sql`.
6. Once deployed, Railway gives you a public URL like `https://your-app.up.railway.app`. Test it: `https://your-app.up.railway.app/api/locations` should return JSON.

### 3. Frontend on Vercel
1. [vercel.com](https://vercel.com) → New Project → import the same repo → set root directory to `frontend/` (framework preset: Vite).
2. Add an environment variable: `VITE_API_BASE_URL` = `https://your-app.up.railway.app/api` (the Railway URL from step 2.6, plus `/api`).
3. Deploy. Vercel gives you a URL like `https://your-app.vercel.app`.

### 4. Connect them
Back in Railway, add a `CORS_ALLOWED_ORIGINS` variable on the backend service set to your Vercel URL (e.g. `https://your-app.vercel.app`) — otherwise the browser will block requests with a CORS error. Redeploy the backend for it to take effect.

### Free alternative (with a tradeoff)
Render offers a genuinely free web-service tier for the backend, but **only manages PostgreSQL for free, not MySQL** — so you'd still need a separate MySQL host (e.g. a free-tier MySQL from Aiven or Clever Cloud) and to wire the same environment variables Railway would have auto-injected. The tradeoff: Render's free tier spins down after 15 minutes of inactivity, so the first request after idle time takes 30–60 seconds. Fine for a viva if you open the URL a few minutes before presenting; not something to rely on for a live cold demo.

### After deploying
- Re-run the same functional tests from Section 5.5 of the project report against the live URLs.
- The seeded demo accounts (`admin@smartcity.local` / `Admin@123`) work identically once `seed_data.sql` is loaded on the cloud database.

## API Reference

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register`, `/login`, `/refresh` | Public |
| GET | `/api/locations` | Public |
| POST/DELETE | `/api/locations` | Admin |
| GET | `/api/roads` | Public |
| POST/PATCH/DELETE | `/api/roads/**` | Admin |
| GET | `/api/route?from=&to=&algorithm=dijkstra\|astar` | Public (logs history if logged in) |
| GET | `/api/route/reachable?from=&maxHops=` | Public (BFS) |
| GET | `/api/nearby?lat=&lng=&type=&limit=` | Public |
| GET | `/api/search?q=&limit=` | Public |
| GET | `/api/traffic/current` | Public |
| POST | `/api/traffic`, `/simulate`, `/reset` | Admin |
| GET | `/api/history` | Authenticated |
| GET | `/api/analytics` | Public |
| GET | `/api/admin/users`, `/dashboard` | Admin |

## DSA Concepts Demonstrated

| Concept | Where |
|---|---|
| Graph (adjacency list) | `CityGraph.java` |
| Dijkstra (min-heap) | `Dijkstra.java` |
| A* (haversine heuristic) | `AStar.java` |
| BFS (hop-limited) | `GraphTraversal.bfsWithinHops` |
| DFS (recursive, connectivity) | `GraphTraversal.dfs` |
| Trie (prefix tree) | `Trie.java`, `TrieNode.java` |
| Min-heap top-K | `AnalyticsService` (top routes/places/congested roads) |
| HashMap frequency counting | `AnalyticsService` |
#   s m a r t - c i t y - n a v i g a t i o n - p l a t f o r m  
 