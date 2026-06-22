# 🐧 Waddler Tourism Platform

A full-stack tourism and hotel booking platform that allows travelers to discover hotels, reserve rooms, book events, manage reservations, submit reviews, and earn loyalty rewards.

The platform provides dedicated dashboards for guests, hotel managers, and administrators, offering a complete end-to-end booking experience.

---

## Features

### Guest Features

* User registration and authentication
* JWT-secured login
* Browse hotels with filtering and search
* View hotel details
* Real-time room availability
* Create and manage reservations
* Event registration
* Restaurant table reservations
* Review and rating system
* Loyalty points and rewards
* User profile management
* Email notifications

### Hotel Manager Features

* Hotel management dashboard
* Room management
* Pricing management
* Event management
* Reservation tracking
* Booking confirmation workflow

### Administrator Features

* User management
* Hotel approval and management
* Reservation oversight
* Support request handling
* Platform-wide administration

---

## Architecture

```text
React Frontend
       │
       ▼
Spring Boot REST API
       │
       ▼
MySQL Database
```

The backend is implemented as a modular monolith with clear module boundaries, making future migration to microservices straightforward.

---

## Technology Stack

### Frontend

* React 18
* Vite
* React Router
* Axios
* Tailwind CSS
* React Query
* ShadCN UI
* JWT Authentication

### Backend

* Java 21
* Spring Boot
* Spring Security
* OAuth2 Resource Server (JWT)
* Spring Data JPA
* Hibernate
* MySQL
* Spring Mail
* Swagger / OpenAPI
* Maven

### Testing

* JUnit 5
* Mockito
* Spring Boot Test

---

## Project Structure

```text
waddler-tourism-platform/
│
├── backend/
│   └── Spring Boot Application
│
├── frontend/
│   └── React + Vite Application
│
└── README.md
```

---

## Security

The application uses:

* JWT-based authentication
* Role-based authorization
* Protected frontend routes
* Spring Security integration

### Supported Roles

* Admin
* Hotel Manager
* User

---

## Core Business Modules

### Authentication Module

* Registration
* Login
* JWT generation
* Authorization

### Hotel Module

* Hotel listings
* Search and filtering
* Hotel details

### Reservation Module

* Room booking
* Reservation lifecycle management
* Availability validation

### Event Module

* Event creation
* Event registration

### Review Module

* Ratings
* Reviews
* User feedback

### Loyalty Module

* Loyalty points
* Reward system

### Notification Module

* Email notifications
* Reservation updates

---

## API Documentation

Swagger UI is available after running the backend:

```text
http://localhost:8080/swagger-ui.html
```

---

## Running the Backend

```bash
cd backend

mvn clean install
mvn spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

---

## Running the Frontend

```bash
cd frontend

npm install
npm run dev
```

Frontend runs on:

```text
http://localhost:3000
```

---

## Team

AbsoluteCinema

### Contributors

* Jiovanni Kitlo
* Salma Abu Odeh
* Zeina Ibrahim


---

## Future Enhancements

* Microservices migration
* Docker and Kubernetes deployment
* Cloud-native infrastructure
* Payment gateway integration
* Advanced recommendation system
* Analytics dashboard
