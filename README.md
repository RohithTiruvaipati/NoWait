# ParkPulse

ParkPulse is a real-time theme park analytics platform designed to monitor attraction wait times, detect crowd surges, track ride closures, predict future wait times, and recommend optimal ride times for visitors.

The platform will eventually ingest live attraction data from ThemeParks.wiki and process it using an event-driven architecture powered by Spring Boot, Apache Kafka, and PostgreSQL, presenting interactive analytics via a React frontend.

---

## 🛠️ Planned Tech Stack

- **Java 21**
- **Spring Boot**
- **Apache Kafka**
- **PostgreSQL**
- **React**
- **TypeScript**
- **Tailwind CSS**
- **Docker**

---

## ✨ Planned Features

- Live attraction wait-time monitoring
- Kafka event streaming
- Historical wait-time storage
- Crowd surge detection
- Ride closure monitoring
- Wait-time prediction
- Smart ride recommendations

---

## 📁 Project Structure

```
ParkPulse/
├── backend/                  # Spring Boot application (Java 21 / Maven)
│   ├── src/main/java/com/parkpulse/
│   │   ├── ParkPulseApplication.java   # Application entry point
│   │   ├── controller/       # REST API endpoints (to be implemented)
│   │   ├── service/          # Business logic & analytics (to be implemented)
│   │   ├── kafka/            # Kafka producers & consumers (to be implemented)
│   │   ├── model/            # JPA entities & domain models (to be implemented)
│   │   ├── repository/       # Database access repositories (to be implemented)
│   │   └── dto/              # Data transfer objects (to be implemented)
│   ├── src/main/resources/
│   │   └── application.yml   # Application configuration
│   └── pom.xml               # Backend dependencies
│
├── frontend/                 # Client application (React + Vite + TypeScript + Tailwind CSS)
│   ├── src/
│   │   ├── components/       # Reusable UI components (to be implemented)
│   │   ├── App.tsx           # Main application root component
│   │   ├── main.tsx          # React application entry point
│   │   └── index.css         # Global Tailwind CSS styling
│   ├── package.json          # Frontend dependencies & scripts
│   └── index.html            # Main HTML document
│
├── docker-compose.yml        # Placeholder for Kafka & PostgreSQL services
├── README.md                 # Project documentation
└── .gitignore                # Global Git ignore rules
```

---

## 📌 Current Status

**Phase 10: Predictive Wait-Time Engine & Live Smart Recommendations**

ParkPulse now predicts future attraction wait times across multiple planning horizons and offers real-time crowd advice:
1. **Multi-Horizon Predictive Model (+15, +30, +45 Minutes Ahead)**: Calculates linear regression ($y = mx + b$) over recent historical snapshots from PostgreSQL (past 60-minute lookback window) to project wait times at 15m, 30m, and 45m intervals into the future.
2. **REST Prediction Endpoint**: `GET /api/rides/{id}/prediction` delivers the multi-horizon array (`horizons: [{ minutesAhead: 15, predictedWait, expectedChange }, ...]`), overall 30-min outlook, expected delta (`+15m`, `-10m`), and automated action recommendations (`RIDE_NOW`, `CONSIDER_WAITING`, `STABLE`).
3. **Interactive Visual Dashboard**:
   - Multi-metric projection grid displaying **Current Wait**, **+15 min**, **+30 min**, and **+45 min** side-by-side with individual delta badges.
   - Dynamic Recommendation Banner advising visitors whether to jump in line now or hold off.
   - Smooth AreaChart visualization showing historical snapshots scaled against 0 to accurately reflect trend changes.

### 🔄 Live Data Ingestion & Parsing Flow

1. **API Client (`RestClient`)**: Calls `https://api.themeparks.wiki/v1/entity/75ea578a-adc8-4116-a54d-dccb60765ef9/live`.
2. **Entity Filtering**: The API response contains shows, entertainment, and restaurants; the parser filters items where `entityType == "ATTRACTION"`.
3. **Queue Extraction**: Traverses `queue.STANDBY.waitTime` safely (handling null values when attractions are closed or down).
4. **DTO Mapping**: Converts each attraction into the [`RideResponse`](backend/src/main/java/com/parkpulse/dto/RideResponse.java) record.
5. **REST Controller Endpoints**:
   - `GET /api/rides`: Returns all attractions and their live wait times as a JSON list.
   - `GET /api/rides/{id}`: Looks up a specific attraction by its UUID (returns HTTP 200 with ride JSON, or HTTP 404 if not found).
   - `GET /api/rides/{id}/history`: Returns the chronological wait time observations from PostgreSQL for charting and trend analysis.
   - `GET /api/rides/{id}/prediction`: Delivers the 30-minute forward forecast, expected wait-time change, and visitor recommendation.

---

## 🚀 Getting Started

### Prerequisites

- Java 21 JDK
- Node.js (v18+) and npm
- Maven (or use the included `./mvnw` wrapper in `backend/`)

### Running the Backend

```bash
cd backend
./mvnw spring-boot:run
```

The Spring Boot backend will start up on `http://localhost:8080`.

To run backend tests:
```bash
cd backend
./mvnw test
```

### Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

The Vite development server will start up on `http://localhost:5173` displaying the welcome screen.

