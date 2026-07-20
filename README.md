# 🏙️ Smart City Navigation Platform

A **DSA-focused Smart City Navigation System** that demonstrates graph algorithms, route optimization, traffic simulation, and location-based services.


        Deployed Link:- https://smart-city-navigation-platform.vercel.app
---

## ✨ Features

- 🔐 JWT Authentication
- 🗺️ Interactive Map (Leaflet)
- 🚗 Route Planning using Dijkstra & A*
- 🌐 BFS & DFS Graph Traversal
- 🔍 Trie-based Location Autocomplete
- 🚦 Live Traffic Simulation
- 📍 Nearby Services Search
- 📊 Analytics Dashboard
- 👨‍💼 Admin Panel
- 📜 Route History

---

# 🛠️ Tech Stack

## Frontend

- React
- TypeScript
- Tailwind CSS
- Leaflet.js
- Axios
- TanStack Query
- React Router

## Backend

- Spring Boot (Java 21)
- Spring Security
- JWT Authentication
- Spring Data JPA
- Lombok

## Database

- MySQL 8

---

# 📂 Project Structure

```text
smart-city-navigation-platform/
│
├── frontend/        React + TypeScript client
├── backend/         Spring Boot REST API
├── database/        Schema & Seed Data
├── docs/            Architecture Documents
└── README.md
```

---

# 🚀 Modules

## Authentication

- User Registration
- Login
- JWT Authentication
- BCrypt Password Hashing
- Role-based Access Control

---

## Interactive Map

- Leaflet Map
- Current Location
- Zoom Controls
- Click to Select Locations
- Latitude & Longitude Display

---

## DSA Algorithms

### Graph

- Adjacency List Representation

### Shortest Path

- Dijkstra Algorithm
- A* Algorithm

### Graph Traversal

- Breadth First Search (BFS)
- Depth First Search (DFS)

### Trie

- Prefix Search
- Autocomplete Suggestions

### Analytics

- HashMap Frequency Counting
- Min Heap Top-K
- Route Statistics

---

## Smart Features

- Nearby Services
- Traffic Simulation
- Route History
- Analytics Dashboard
- Admin Dashboard

---

# 👥 Demo Accounts

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@smartcity.local | Admin@123 |
| Citizen | citizen@smartcity.local | Citizen@123 |

---

# ⚙️ Installation

## 1. Clone Repository

```bash
git clone https://github.com/bipulmondal2003/smart-city-navigation-platform.git

cd smart-city-navigation-platform
```

---

## 2. Database

```bash
mysql -u root -p < database/schema.sql

mysql -u root -p < database/seed_data.sql
```

---

## 3. Backend

Edit

```
backend/src/main/resources/application.properties
```

Update:

```properties
spring.datasource.password=YOUR_PASSWORD
app.jwt.secret=YOUR_SECRET_KEY
```

Run:

```bash
cd backend

mvn spring-boot:run
```

Backend URL

```
http://localhost:8080
```

---

## 4. Frontend

```bash
cd frontend

npm install

npm run dev
```

Frontend URL

```
http://localhost:5173
```

---

# 🌐 API Endpoints

| Method | Endpoint |
|---------|----------|
| POST | /api/auth/register |
| POST | /api/auth/login |
| GET | /api/locations |
| GET | /api/roads |
| GET | /api/route |
| GET | /api/search |
| GET | /api/nearby |
| GET | /api/history |
| GET | /api/analytics |

---

# 📚 DSA Concepts Used

| Data Structure / Algorithm | Purpose |
|---------------------------|---------|
| Graph | Road Network |
| Dijkstra | Shortest Path |
| A* Search | Optimized Route |
| BFS | Reachability |
| DFS | Connectivity |
| Trie | Autocomplete |
| HashMap | Analytics |
| Min Heap | Top-K Statistics |

---

# 📈 Future Improvements

- Live GPS Tracking
- Real-Time Traffic API
- Cab Booking
- Parking Recommendation
- Public Transport Integration
- Weather Integration

---

# 👨‍💻 Author

**Bipul Mondal**

B.Tech Computer Science & Engineering

GitHub: https://github.com/bipulmondal2003

---

# ⭐ If you found this project useful

Please consider giving it a **Star ⭐** on GitHub.