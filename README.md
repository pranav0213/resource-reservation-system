
# ReserveHub — Resource Reservation Management System

A full-stack web application for managing resources and reservations with secure authentication, role-based authorization, and double-booking prevention.

## Overview

ReserveHub is a full-stack resource reservation management system built with Spring Boot, React, and PostgreSQL. It allows users to browse resources, create reservations, and manage their bookings while providing administrators with tools to manage resources and reservations.

The application uses JWT authentication, role-based access control, reservation ownership checks, and database locking to help prevent overlapping reservations.

## Features

### Authentication and Authorization
- User registration and login
- JWT-based authentication
- Role-based access control (USER and ADMIN)
- Protected API endpoints
- Reservation ownership enforcement

### Resource Management
- View resources
- Create resources
- Update resource information
- Delete resources
- Manage resource availability

### Reservation Management
- Create reservations for available resources
- View reservations based on user permissions
- Cancel reservations
- Prevent overlapping reservations
- Validate reservation dates and times
- Use database locking to handle concurrent booking requests

### Frontend
- React-based user interface
- Login and registration pages
- Resource and reservation views
- Role-aware navigation and controls
- Responsive styling

## Tech Stack

| Category | Technologies |
|---|---|
| Frontend | React, JavaScript, Vite, CSS |
| Backend | Java 25, Spring Boot 4.1.1 |
| Database | PostgreSQL |
| Security | Spring Security, JWT |
| Data Access | Spring Data JPA, Hibernate |
| Testing | JUnit, Mockito, Spring Boot Test |
| Build Tools | Maven, npm |
| Development Tools | IntelliJ IDEA, Postman, Git, GitHub |

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
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
├── .gitignore
└── README.md
```

## System Architecture

```text
React Frontend
      |
      | HTTP / REST API
      v
Spring Boot Backend
      |
      +-- Spring Security + JWT
      |
      +-- Controllers
      |
      +-- Services
      |
      +-- Repositories
      |
      v
PostgreSQL Database
```

The frontend communicates with the backend through REST APIs. Spring Security handles authentication and authorization. The service layer applies business rules, while Spring Data JPA manages database access.

## User Roles

### USER
- Browse resources
- Create reservations
- View their own reservations
- Cancel their own reservations

### ADMIN
- Manage resources
- View reservations across users
- Cancel reservations as an administrator

## Running the Project Locally

### Prerequisites

Install the following software:

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

The backend uses the following environment variables:

| Variable | Description |
|---|---|
| DB_PASSWORD | PostgreSQL database password |
| JWT_SECRET | Secret key used for JWT signing |

Keep actual passwords and signing secrets out of Git.

### 3. Start the Backend

Navigate to the backend directory:

```powershell
cd reservation-system
```

Configure the required environment variables in your terminal or IDE.

Start Spring Boot on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

### 4. Start the Frontend

Open another terminal from the repository root:

```powershell
cd frontend
npm install
npm run dev
```

The Vite development server typically runs at:

```text
http://localhost:5173
```

Make sure the backend and frontend are both running.

## Testing

The backend includes automated tests covering:

- Valid reservation creation
- Invalid reservation times
- Unavailable resources
- Overlapping reservations
- Reservation ownership
- USER and ADMIN authorization
- Reservation cancellation
- Concurrent booking prevention

To run the tests, create a PostgreSQL test database named `reservation_test_db`, configure `DB_PASSWORD`, and run:

```powershell
cd reservation-system
.\mvnw.cmd clean test
```

The latest verified local test run completed successfully with 12 tests passing.

## Security

ReserveHub implements several security measures:

- JWT-based authentication
- Spring Security authorization
- Role-based access control
- User-specific reservation access
- Restricted reservation cancellation
- Input validation
- Database locking for reservation concurrency
- Environment variables for sensitive configuration

Production deployments should use strong secrets, HTTPS, restricted database access, and appropriate production database migration settings.

## Future Improvements

- Deploy the frontend, backend, and database to cloud services
- Add continuous integration and deployment
- Add reservation search and filtering
- Implement email notifications
- Add administrative reporting
- Improve automated frontend testing

## Project Status

Core application development and automated backend testing are complete.

Cloud deployment and final production testing are planned.

## Author

Developed by [pranav0213](https://github.com/pranav0213).

## Repository

[ReserveHub — Resource Reservation Management System](https://github.com/pranav0213/resource-reservation-system)
  
