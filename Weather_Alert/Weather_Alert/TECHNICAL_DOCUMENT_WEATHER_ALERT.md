# Weather Alert Service - Technical Document

## 1. Overview

The Weather Alert Service is a secure Spring Boot application that manages user-submitted weather alerts and enriches them with geolocation data fetched asynchronously from an external API.

### Key Features
- RESTful API for CRUD operations on weather alerts
- JWT-based authentication and authorization
- Role-based access control (ADMIN, USER)
- Asynchronous geo-tagging using OpenStreetMap Nominatim API
- Swagger/OpenAPI documentation

---

## 2. External Geocoding API

### Chosen Service: OpenStreetMap Nominatim

**Why Nominatim?**
- **Free and Open Source**: No API key required, no usage costs
- **Reliable**: Backed by OpenStreetMap community data
- **Good Coverage**: Worldwide geographic data
- **Simple API**: Easy to integrate with REST calls

**API Endpoint:**
```
GET https://nominatim.openstreetmap.org/search?q={location}&format=json&limit=1
```

**Example Response:**
```json
[
  {
    "lat": "42.6977082",
    "lon": "23.3218675",
    "display_name": "Sofia, Sofia City Province, Bulgaria"
  }
]
```

**Rate Limiting:**
- Nominatim requires maximum 1 request per second
- Implemented via `Thread.sleep()` in GeocodingService
- User-Agent header is mandatory

**Error Handling:**
- Empty response: Mark alert as FAILED with appropriate message
- Network errors: Catch exception, log, mark alert as FAILED
- Invalid JSON: Log error, mark alert as FAILED

---

## 3. Authentication & Authorization

### Authentication: JWT (JSON Web Tokens)

**Flow:**
```
1. Client sends POST /api/auth/login with credentials
2. Server validates against database (BCrypt password)
3. Server generates JWT token (HS256 algorithm)
4. Client stores token and sends with each request
5. JwtAuthenticationFilter validates token
6. If valid, user is authenticated for the request
```

**JWT Token Structure:**
- Header: Algorithm (HS256) and type (JWT)
- Payload: Username (subject), issued time, expiration
- Signature: HMAC-SHA256 with secret key

**Key Components:**
- `JwtTokenProvider`: Generates and validates tokens
- `JwtAuthenticationFilter`: Intercepts requests, validates tokens
- `CustomUserDetailsService`: Loads user from database

### Authorization: Role-Based Access Control (RBAC)

**Roles:**
| Role | Description | Permissions |
|------|-------------|-------------|
| ROLE_ADMIN | Administrator | Full CRUD access |
| ROLE_USER | Regular user | Read-only access (GET only) |

**Endpoint Security:**
```java
// In SecurityConfig:
.requestMatchers(HttpMethod.GET, "/api/alerts/**").hasAnyRole("ADMIN", "USER")
.requestMatchers(HttpMethod.POST, "/api/alerts/**").hasRole("ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/alerts/**").hasRole("ADMIN")
.requestMatchers(HttpMethod.DELETE, "/api/alerts/**").hasRole("ADMIN")
```

**Method-Level Security:**
```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<AlertResponse> createAlert(...) { }
```

---

## 4. Asynchronous Enrichment

### How It Works

**Goal:** The POST /api/alerts endpoint must return immediately without waiting for geo-tagging.

**Implementation:**

1. **Alert Creation (Synchronous):**
   ```java
   Alert savedAlert = alertRepository.save(alert);  // Save with PENDING status
   ```

2. **Trigger Async Geo-tagging:**
   ```java
   geocodingService.enrichAlertWithCoordinates(savedAlert.getId());  // Non-blocking
   ```

3. **Return Response Immediately:**
   ```java
   return alertMapper.toResponse(savedAlert);  // Client gets response
   ```

4. **Background Processing (Async):**
   ```java
   @Async("taskExecutor")
   public void enrichAlertWithCoordinates(Long alertId) {
       // This runs in a separate thread
       // Calls Nominatim API
       // Updates alert with coordinates
   }
   ```

**Thread Pool Configuration:**
```yaml
async:
  core-pool-size: 2    # Minimum threads
  max-pool-size: 5     # Maximum threads
  queue-capacity: 100  # Queue size before new threads
```

**Important Notes:**
- `@Async` methods must be called from a different class (proxy limitation)
- The method is transactional to ensure database consistency
- Errors are caught and logged, alert status is updated to FAILED

---

## 5. Architectural Decisions & Trade-offs

### Layered Architecture

```
┌─────────────────────────────────────────┐
│           Controller Layer              │  ← HTTP handling, validation
├─────────────────────────────────────────┤
│            Service Layer                │  ← Business logic
├─────────────────────────────────────────┤
│           Repository Layer              │  ← Data access
├─────────────────────────────────────────┤
│              Database                   │  ← H2/PostgreSQL
└─────────────────────────────────────────┘
```

**Benefits:**
- Clear separation of concerns
- Each layer is testable independently
- Easy to swap implementations (e.g., change database)

### Dependency Injection Pattern

**Choice: Constructor Injection with final fields**

```java
@Service
@RequiredArgsConstructor
public class AlertService {
    private final AlertRepository alertRepository;      // Injected
    private final GeocodingService geocodingService;   // Injected
    private final AlertMapper alertMapper;             // Injected
}
```

**Rationale:**
- Immutability (final fields)
- Clear dependencies (visible in constructor)
- Fail-fast (startup fails if dependency missing)
- Testability (easy to provide mocks)

### Database Choice

**Development:** H2 (in-memory)
- Fast startup
- No external setup
- Automatic schema creation

**Production:** PostgreSQL (recommended)
- Robust and scalable
- Full ACID compliance
- Better for concurrent access

### Error Handling Strategy

**Centralized via @ControllerAdvice:**
- Consistent error response format
- Single place to handle all exceptions
- Easy to add new exception handlers

**Error Response Format:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Alert not found with id: 999",
  "path": "/api/alerts/999"
}
```

### Trade-offs

| Decision | Benefit | Trade-off |
|----------|---------|-----------|
| JWT over Sessions | Stateless, scalable | Token can't be invalidated (until expiry) |
| Async Geo-tagging | Fast response times | Client doesn't get coordinates immediately |
| H2 for dev | Simple setup | Different from production DB |
| Nominatim API | Free, no API key | Rate limited (1 req/sec) |
| BCrypt | Secure password hashing | Slower than MD5/SHA |

---

## 6. API Summary

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| POST | /api/auth/login | User login | No | - |
| POST | /api/alerts | Create alert | Yes | ADMIN |
| GET | /api/alerts | List all alerts | Yes | ADMIN, USER |
| GET | /api/alerts/{id} | Get alert by ID | Yes | ADMIN, USER |
| PUT | /api/alerts/{id} | Update alert | Yes | ADMIN |
| DELETE | /api/alerts/{id} | Delete alert | Yes | ADMIN |

---

## 7. Running the Application

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Access
# API: http://localhost:8080/api/alerts
# Swagger: http://localhost:8080/swagger-ui.html
# H2 Console: http://localhost:8080/h2-console
```

**Test Credentials:**
- Admin: `admin` / `admin123`
- User: `user1` / `user123`
