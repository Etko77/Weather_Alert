# 🌦️ Weather Alert Service

A secure Spring Boot application for managing weather alerts with automatic geo-tagging.

## Features

- ✅ RESTful API for weather alert CRUD operations
- ✅ JWT-based authentication
- ✅ Role-based authorization (ADMIN, USER)
- ✅ Asynchronous geo-tagging using OpenStreetMap Nominatim
- ✅ Swagger/OpenAPI documentation
- ✅ Comprehensive error handling
- ✅ Unit and Integration tests

## Tech Stack

- **Framework:** Spring Boot 3.2
- **Security:** Spring Security + JWT
- **Database:** MySql
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **Build:** Maven
- **Java:** 17+

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Run the Application

```bash
# Clone the repository
git clone <repository-url>
cd weather-alert-service

# Build
mvn clean install

# Run
mvn spring-boot:run
```

### Access Points

| Service | URL |
|---------|-----|
| API Base | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

## Test Credentials

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN (full CRUD) |
| user1 | user123 | USER (read-only) |
| user2 | user123 | USER (read-only) |

## API Endpoints

### Authentication

```bash
# Login
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}

# Response
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "username": "admin",
  "roles": ["ROLE_ADMIN", "ROLE_USER"]
}
```

### Weather Alerts

```bash
# Create Alert (ADMIN only)
POST /api/alerts
Authorization: Bearer <token>
{
  "description": "Heavy snowfall expected",
  "locationName": "Sofia",
  "severityLevel": "HIGH"
}

# Get All Alerts (ADMIN, USER)
GET /api/alerts

# Get Alert by ID (ADMIN, USER)
GET /api/alerts/{id}

# Update Alert (ADMIN only)
PUT /api/alerts/{id}
{
  "description": "Updated description",
  "severityLevel": "MEDIUM"
}

# Delete Alert (ADMIN only)
DELETE /api/alerts/{id}
```

## Project Structure

```
src/main/java/com/example/weatheralert/
├── config/           # Security, Async, OpenAPI configs
├── controller/       # REST controllers
├── dto/              # Request/Response DTOs
├── entity/           # JPA entities
├── enums/            # Enumerations
├── exception/        # Custom exceptions & handlers
├── repository/       # JPA repositories
├── security/         # JWT components
└── service/          # Business logic
```

## Async Geo-tagging

When an alert is created:
1. Alert is saved with `geoTaggingStatus: PENDING`
2. Response is returned immediately
3. Background thread calls Nominatim API
4. Alert is updated with coordinates (`SUCCESS`) or error (`FAILED`)

```
Client                    Server                    Nominatim
  |                          |                          |
  |-- POST /api/alerts ----->|                          |
  |                          |-- Save (PENDING) ------->|
  |<-- 201 Created ----------|                          |
  |                          |                          |
  |                          |-- [ASYNC] GET --------->|
  |                          |<-- lat, lon -------------|
  |                          |-- Update (SUCCESS) ---->|
```

## Configuration

### application.yml

```yaml
# JWT Settings
jwt:
  secret: your-secret-key
  expiration: 86400000  # 24 hours

# Geocoding API
geocoding:
  api:
    base-url: https://nominatim.openstreetmap.org
    rate-limit-ms: 1000  # Max 1 request/second

# Async Thread Pool
async:
  core-pool-size: 2
  max-pool-size: 5
  queue-capacity: 100
```

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Documentation

- [Technical Document](docs/TECHNICAL_DOCUMENT.md) - Detailed architecture and design decisions
- [Swagger UI](http://localhost:8080/swagger-ui.html) - Interactive API documentation

## License

MIT License
