# ReserveHub — Resource Reservation Management System

A full-stack resource booking platform built with **Java, Spring Boot, React, and PostgreSQL**, featuring secure JWT authentication, role-based access control, and concurrent booking protection.

**[Live Demo](https://reservehub-frontend.onrender.com)** | **[Backend API](https://reservehub-backend-wqh4.onrender.com)** | **[GitHub Repository](https://github.com/pranav0213/resource-reservation-system)**

> **Deployment note:** The application uses Render's free hosting services. The backend may take some time to respond after inactivity. The current free PostgreSQL database is scheduled to expire on November 24, 2026, unless upgraded or migrated.

## Overview

ReserveHub is a full-stack web application designed to simplify the management and reservation of shared resources, such as conference rooms and equipment.

Users can browse available resources, reserve them for specific time periods, view their bookings, and cancel reservations. Administrators can manage resources and oversee reservations across users.

The system uses Spring Security and JWT for authentication and authorization, while PostgreSQL and database-level locking help protect against overlapping reservations and concurrent booking conflicts.

## Key Features

### Authentication and Authorization

- User registration and login
- JWT-based stateless authentication
- Role-based access control with USER and ADMIN roles
- Protected REST API endpoints
- Reservation ownership enforcement
- Role-aware frontend controls

### Resource Management

- Browse available resources
- Create, update, and delete resources as an administrator
- Manage resource availability
- Store resource information in PostgreSQL

### Reservation Management

- Book resources for selected time intervals
- Validate reservation start and end times
- Prevent overlapping reservations
- Handle concurrent booking requests using database locking
- View reservations based on user permissions
- Cancel reservations with ownership and authorization checks

### Frontend

- React-based dashboard
- Login and registration interfaces
- Resource and reservation management views
- Role-specific actions and navigation
- Responsive CSS styling
- Backend connectivity status

## Technology Stack

| Category | Technologies |
|---|---|
| Frontend | React, JavaScript, Vite, CSS |
| Backend | Java 25, Spring Boot 4.1.1 |
| Security | Spring Security, JWT |
| Database | PostgreSQL 18 |
| Persistence | Spring Data JPA, Hibernate |
| Testing | JUnit, Mockito, Spring Boot Test |
| Build Tools | Maven, npm |
| Deployment | Render, Docker |
| Development Tools | IntelliJ IDEA, VS Code, Postman, Git, GitHub |

## System Architecture

```text
                React + Vite
                 Frontend
                    |
                    | HTTPS / REST API
                    v
              Spring Boot API
                    |
          Spring Security + JWT
                    |
              REST Controllers
                    |
              Service Layer
                    |
          Spring Data JPA
                    |
                    v
               PostgreSQL
```

The React frontend communicates with Spring Boot through REST APIs. Spring Security authenticates requests and enforces role-based permissions. The service layer handles reservation validation, ownership checks, and booking conflicts. Spring Data JPA and PostgreSQL provide persistent storage and transaction management.

## User Roles

| Capability | USER | ADMIN |
|---|---|---|
| Browse resources | Yes | Yes |
| Create reservations | Yes | Yes |
| View own reservations | Yes | Yes |
| View all reservations | No | Yes |
| Cancel own reservations | Yes | Yes |
| Cancel other users' reservations | No | Yes |
| Create, edit, and delete resources | No | Yes |

Newly registered accounts receive the USER role by default. Administrative access is assigned separately.

## Project Structure

```text
resource-reservation-system/
├── frontend/
│   ├── public/
│   ├── src/
│   ├── package.json
│   └── vite.config.js
├── reservation-system/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
├── .gitignore
└── README.md
```

## Getting Started

### Prerequisites

- Java 25
- Node.js and npm
- PostgreSQL
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/pranav0213/resource-reservation-system.git
cd resource-reservation-system
```

### 2. Configure PostgreSQL

Create a PostgreSQL database named:

```text
reservation_db
```

Configure the backend database connection and JWT signing secret through environment variables.

| Variable | Description |
|---|---|
| `DB_URL` | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Secret used for JWT signing |
| `FRONTEND_URL` | Optional additional frontend origin allowed by CORS |

Example local JDBC URL:

```text
jdbc:postgresql://localhost:5432/reservation_db
```

Never commit real database passwords, signing secrets, or production connection URLs to Git.

### 3. Start the Backend

```powershell
cd reservation-system
.\mvnw.cmd spring-boot:run
```

The backend runs locally at:

```text
http://localhost:8080
```

### 4. Start the Frontend

Open a separate terminal from the repository root:

```powershell
cd frontend
npm install
npm run dev
```

The Vite development server typically runs at:

```text
http://localhost:5173
```

For local development, the frontend can use its configured development API routing. For production builds, set `VITE_API_BASE_URL` to the deployed backend URL.

## Testing

ReserveHub includes automated backend tests for:

- Reservation creation
- Invalid reservation time validation
- Unavailable resources
- Overlapping reservations
- Reservation ownership
- Role-based authorization
- Reservation cancellation
- Concurrent booking prevention

Create a separate PostgreSQL test database named:

```text
reservation_test_db
```

Configure the required test environment variables, then run:

```powershell
cd reservation-system
.\mvnw.cmd clean test
```

**Latest verified local result: 12 tests passing.**

### Production Verification

The following workflows were successfully tested against the deployed application:

| Test Scenario | Result |
|---|---|
| User registration and login | Passed |
| Administrator login | Passed |
| Administrator creates a resource | Passed |
| User views available resources | Passed |
| User creates a reservation | Passed |
| Overlapping booking is rejected | Passed |
| User cancels a reservation | Passed |
| Frontend connects to the deployed backend | Passed |
| Backend connects to PostgreSQL | Passed |

## Security

The application implements:

- JWT authentication
- Spring Security authorization
- USER and ADMIN permissions
- Reservation ownership checks
- Restricted administrative operations
- Input and booking time validation
- Database locking for concurrent reservation requests
- Environment-based secret configuration
- HTTPS for deployed frontend and backend communication

## Deployment

ReserveHub is deployed using Render.

| Component | Deployment |
|---|---|
| Frontend | Render Static Site |
| Backend | Render Docker Web Service |
| Database | Render PostgreSQL |

**Live Application:** https://reservehub-frontend.onrender.com

**Backend Base URL:** https://reservehub-backend-wqh4.onrender.com

The frontend communicates with the deployed Spring Boot API, which connects to PostgreSQL for persistent resource and reservation data.

## Screenshots

Application screenshots will be added to demonstrate:

- Login and registration
- Administrator dashboard
- Resource management
- User reservation workflow
- Double-booking prevention
- Reservation cancellation

## Future Improvements

- Add GitHub Actions continuous integration
- Implement email booking notifications
- Add reservation search and filtering
- Improve frontend automated testing
- Add administrative analytics
- Improve date and timezone handling
- Introduce production database migration tooling

## Project Status

**Deployed and functional.**

The core resource management, authentication, authorization, and reservation workflows are implemented and have passed initial production testing.

Further enhancements and production hardening are planned.

## Author

Developed by **[Pranav Reddy](https://github.com/pranav0213)**.

## Repository

[github.com/pranav0213/resource-reservation-system](https://github.com/pranav0213/resource-reservation-system)