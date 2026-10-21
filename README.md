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

**Phase 0: Project initialization**

The project currently contains only its initial clean directory structure, build configurations, and minimal entry points. No application functionality, database connections, Kafka topics, or external API integrations have been implemented yet.

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

