#  E-Commerce Platform with AI Assistant

> A modern, full-stack e-commerce solution powered by an intelligent AI shopping assistant that understands your database and helps customers find exactly what they need.

---

##  Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot |
| Frontend | Angular 18 |
| Database | MySQL |
| AI Layer | AI API (LLM Integration) |

---

## Project Structure

```
E-COMMERCE/
├── backend/demo/                  # Spring Boot Application
│   └── src/main/java/com/ecommerce/
│       ├── config/                # Security, CORS, AI Config
│       ├── controller/            # REST API Controllers
│       ├── service/               # Business Logic (Interface + Impl)
│       │   └── impl/
│       ├── repository/            # Data Access Layer (JPA)
│       ├── model/                 # JPA Entities
│       ├── dto/                   # Data Transfer Objects
│       │   ├── request/
│       │   └── response/
│       ├── exception/             # Global Exception Handling
│       ├── security/              # JWT Auth (Filter, Util)
│       └── mapper/                # Entity ↔ DTO Mappers
│
└── frontend/                      # Angular 18 Application
    └── src/app/
        ├── core/                  # Guards, Interceptors, Core Services
        ├── shared/                # Reusable Components, Models, Pipes
        ├── features/              # Feature Modules (Lazy Loaded)
        │   ├── auth/
        │   ├── products/
        │   ├── cart/
        │   ├── orders/
        │   ├── profile/
        │   └── ai-chat/           # AI Chat Widget
        ├── services/              # HTTP Services
        └── store/                 # NgRx State Management
```

---

## 🌐 Ports

| Service | URL |
|---------|-----|
| Backend API | `http://localhost:3000` |
| Frontend App | `http://localhost:3001` |


---

##  Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8+
- Maven

### Run Backend
```bash
cd backend/demo
./mvnw spring-boot:run
# Runs on http://localhost:3000
```

### Run Frontend
```bash
cd frontend
npm install
ng serve
# Runs on http://localhost:3001
```

---


