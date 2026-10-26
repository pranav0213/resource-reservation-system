# \# ReserveHub — Resource Reservation Management System

# 

# A full-stack web application for managing resources and reservations with secure authentication, role-based authorization, and double-booking prevention.

# 

# \## Features

# 

# \- User registration and login with JWT authentication

# \- Role-based access control for USER and ADMIN accounts

# \- Resource creation, viewing, updating, and deletion

# \- Reservation creation and cancellation

# \- Reservation ownership and access restrictions

# \- Double-booking prevention using database locking

# \- RESTful API with input validation and error handling

# \- Responsive React frontend

# \- Automated unit and integration tests

# 

# \## Tech Stack

# 

# | Layer | Technologies |

# |---|---|

# | Frontend | React, JavaScript, Vite, CSS |

# | Backend | Java 25, Spring Boot 4.1.1 |

# | Database | PostgreSQL |

# | Security | Spring Security, JWT |

# | Persistence | Spring Data JPA, Hibernate |

# | Testing | JUnit, Mockito, Spring Boot Test |

# | Tools | Git, GitHub, Maven, Postman, IntelliJ IDEA |

# 

# \## Project Structure

# 

# ```text

# resource-reservation-system/

# ├── frontend/             # React frontend

# ├── reservation-system/   # Spring Boot backend

# └── README.md

# ```

# 

# \## User Roles

# 

# \*\*USER\*\*

# \- View available resources

# \- Create reservations

# \- View their own reservations

# \- Cancel their own reservations

# 

# \*\*ADMIN\*\*

# \- Manage resources

# \- View reservations across users

# \- Cancel reservations as an administrator

# 

# \## Running Locally

# 

# \### Prerequisites

# 

# \- Java 25

# \- Node.js and npm

# \- PostgreSQL

# \- Git

# 

# \### Backend Setup

# 

# 1\. Create a PostgreSQL database named `reservation\_db`.

# 2\. Configure the `DB\_PASSWORD` and `JWT\_SECRET` environment variables.

# 3\. Navigate to `reservation-system`.

# 4\. Run the backend using the Maven wrapper.

# 

# On Windows PowerShell:

# 

# ```powershell

# cd reservation-system

# .\\mvnw.cmd spring-boot:run

# ```

# 

# The backend runs at `http://localhost:8080`.

# 

# \### Frontend Setup

# 

# Open another terminal:

# 

# ```powershell

# cd frontend

# npm install

# npm run dev

# ```

# 

# The frontend runs at `http://localhost:5173` by default.

# 

# \## Testing

# 

# Run the backend test suite:

# 

# ```powershell

# cd reservation-system

# .\\mvnw.cmd clean test

# ```

# 

# The project includes unit and integration tests for reservation validation, authorization, ownership, and concurrency.

# 

# \## Security

# 

# \- JWT-based authentication

# \- Role-based authorization

# \- Reservation ownership enforcement

# \- Environment variables for database credentials and JWT secrets

# \- Transactional database locking to prevent overlapping bookings

# 

# \## Future Improvements

# 

# \- Cloud deployment

# \- Automated CI/CD pipeline

# \- Reservation search and filtering

# \- Email notifications

# \- Additional administrative reporting

# 

# \## Project Status

# 

# Core application development completed. Deployment and final production testing are planned.

