# Review Service

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/ALMGHAS/bookmyseat-review-service)
[![Test Coverage](https://img.shields.io/badge/coverage-89%25-brightgreen)](https://github.com/ALMGHAS/bookmyseat-review-service)
[![Version](https://img.shields.io/badge/version-1.0.0-blue)](https://github.com/ALMGHAS/bookmyseat-review-service/releases)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-orange)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green)](https://spring.io/projects/spring-boot)

**Review Service** is a production-ready Spring Boot microservice that manages movie reviews and ratings for the BookMySeat application. Built with resilience patterns, comprehensive testing (89% coverage), and full observability including circuit breakers and distributed tracing.

## 📚 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Quick Start](#-quick-start)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Configuration](#-configuration)
- [Testing](#-testing)
- [Resilience Patterns](#-resilience-patterns)
- [Performance](#-performance)
- [Security](#-security)
- [Dependencies](#-dependencies)
- [Deployment](#-deployment)
- [Monitoring](#-monitoring)
- [Sample Data](#-sample-data)
- [Best Practices](#-best-practices)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [Changelog](#-changelog)

## ✨ Features

- **Review Management**: Complete CRUD operations for movie reviews with validation
- **Rating Aggregation**: Real-time rating calculations and statistics per movie
- **RESTful API**: Well-documented REST endpoints with OpenAPI 3.0
- **Database Integration**: MySQL with JPA/Hibernate and automatic migrations
- **High Test Coverage**: 89% test coverage with comprehensive unit and integration tests
- **Resilience4j Integration**: Circuit breaker, retry, and timeout patterns for movie service calls
- **Observability**: Metrics, health checks, and distributed tracing with Zipkin
- **Service Integration**: Seamless integration with Movie Service for title enrichment
- **Production Ready**: Docker, Kubernetes, and cloud deployment support
- **Performance Optimized**: Connection pooling, indexing, and query optimization

## 🏗 Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │───▶│  Review Service │───▶│     MySQL       │
│   (Angular)     │    │  (Spring Boot)  │    │   Database      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              │ HTTP Client
                              ▼
                       ┌─────────────────┐
                       │  Movie Service  │
                       │  (Resilience4j) │
                       └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Observability │
                       │ (Prometheus,    │
                       │  Zipkin, etc.)  │
                       └─────────────────┘
```

### Service Layer Architecture

```
Controller Layer (REST APIs)
     ↓
Service Layer (Business Logic + Circuit Breaker)
     ↓
Client Layer (Movie Service Integration)
     ↓
Repository Layer (Data Access)
     ↓
Database Layer (MySQL)
```

## 🛠 Technology Stack

- **Runtime**: Java 17+, Spring Boot 3.5.6
- **Database**: MySQL 8.0+ with HikariCP connection pooling
- **Build Tool**: Maven 3.8+
- **Testing**: JUnit 5, Mockito, Spring Boot Test (89% coverage)
- **Documentation**: OpenAPI 3.0 (Swagger UI)
- **Resilience**: Resilience4j (Circuit Breaker, Retry, Time Limiter)
- **Observability**: Micrometer, Prometheus, Zipkin, Spring Actuator
- **Migration**: Flyway for database versioning
- **Containerization**: Docker, Docker Compose, Kubernetes

## 🚀 Quick Start

### Prerequisites

- **Java 17+** (OpenJDK recommended)
- **Maven 3.8+**
- **Either**: Docker OR MySQL 8.0+ locally installed
- **Movie Service**: Must be running for full functionality
- **Zipkin** (Optional, for distributed tracing)
- **Optional**: Git for cloning

### Quick Setup

```bash
# Navigate to the review-service directory
cd review-service

# Copy environment template
cp .env.example .env

# Edit .env with your configuration
# SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/review_db
# MOVIE_SERVICE_BASE_URL=http://localhost:8081

# Start the application (assumes database is set up)
mvn spring-boot:run
```

### Docker Compose Setup (Recommended)

```bash
# Start complete review service environment
docker-compose up --build

# Services available:
# - Review Service: http://localhost:8082
# - Review Database: localhost:3308
```

### Manual Database Setup

```bash
# Create database in your local MySQL
mysql -u root -p
CREATE DATABASE review_db;
CREATE USER 'reviewuser'@'localhost' IDENTIFIED BY 'reviewpass';
GRANT ALL PRIVILEGES ON review_db.* TO 'reviewuser'@'localhost';
FLUSH PRIVILEGES;

# Set environment variables
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/review_db
export SPRING_DATASOURCE_USERNAME=reviewuser
export SPRING_DATASOURCE_PASSWORD=reviewpass
export MOVIE_SERVICE_BASE_URL=http://localhost:8081

# Start the application
mvn spring-boot:run
```

### Zipkin Setup (Optional - Distributed Tracing)

Zipkin provides distributed tracing for monitoring request flows across services.

**Requirements**: Java 17 or higher

**Quick Start (Recommended)**:
```bash
# Download and run Zipkin with one command
curl -sSL https://zipkin.io/quickstart.sh | bash -s
java -jar zipkin.jar
```

**Alternative - Docker (Preferred Method)**:
```bash
# Run Zipkin in Docker
docker run -d -p 9411:9411 openzipkin/zipkin
```

**Alternative - Homebrew (macOS)**:
```bash
brew install zipkin
```

**Access Zipkin UI**:
- Open http://localhost:9411 in your browser
- View traces after making requests to the Review Service

**Note**: The service will work without Zipkin, but distributed tracing features will be unavailable. Connection warnings will appear in logs if Zipkin is not running.

### ✅ Access Points

Once started, the application will be available at:

| Service | URL | Description |
|---------|-----|-------------|
| **Application** | http://localhost:8082 | Main service endpoint |
| **API Documentation** | http://localhost:8082/swagger-ui.html | Interactive API docs |
| **Health Check** | http://localhost:8082/actuator/health | Service health status |
| **Metrics** | http://localhost:8082/actuator/metrics | Application metrics |
| **Prometheus** | http://localhost:8082/actuator/prometheus | Prometheus metrics |
| **Circuit Breakers** | http://localhost:8082/actuator/circuitbreakers | Circuit breaker status |
| **Zipkin UI** | http://localhost:9411 | Distributed tracing UI (if running) |

## 📋 API Documentation

### Reviews API

#### Submit a Review
```http
POST /api/v1/reviews
Content-Type: application/json
```

**Request Body:**
```json
{
  "movieId": 1,
  "userName": "John Doe",
  "rating": 4.5,
  "comment": "Great movie! Highly recommended."
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "movieId": 1,
  "movieTitle": "Inception",
  "userName": "John Doe",
  "rating": 4.5,
  "comment": "Great movie! Highly recommended.",
  "reviewDate": "2025-09-30T10:30:00"
}
```

**Validation Rules:**
- `movieId`: Required, must be positive
- `userName`: Required, 2-100 characters
- `rating`: Required, 0.0-5.0 (increments of 0.5)
- `comment`: Optional, max 1000 characters

#### Get Reviews for a Movie
```http
GET /api/v1/reviews/movie/{movieId}?page=0&size=10
```

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10, max: 100)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "movieId": 1,
      "movieTitle": "Inception",
      "userName": "John Doe",
      "rating": 4.5,
      "comment": "Great movie! Highly recommended.",
      "reviewDate": "2025-09-30T10:30:00"
    },
    {
      "id": 2,
      "movieId": 1,
      "movieTitle": "Inception",
      "userName": "Jane Smith",
      "rating": 5.0,
      "comment": "Mind-blowing!",
      "reviewDate": "2025-09-30T11:15:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 6,
  "totalPages": 1,
  "last": true,
  "first": true,
  "size": 10,
  "number": 0,
  "numberOfElements": 6
}
```

#### Get Movie Rating Summary
```http
GET /api/v1/reviews/movie/{movieId}/rating
```

**Response (200 OK):**
```json
{
  "movieId": 1,
  "movieTitle": "Inception",
  "averageRating": 4.42,
  "totalReviews": 6,
  "ratingDistribution": {
    "5.0": 2,
    "4.5": 2,
    "4.0": 1,
    "3.5": 1,
    "3.0": 0,
    "2.5": 0,
    "2.0": 0,
    "1.5": 0,
    "1.0": 0,
    "0.5": 0
  }
}
```

**Error Response (404):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "No reviews found for movie ID: 999",
  "path": "/api/v1/reviews/movie/999/rating",
  "timestamp": "2025-09-30T10:30:00.000Z"
}
```

### API Examples

```bash
# Submit a review
curl -X POST http://localhost:8082/api/v1/reviews \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "userName": "Alice Johnson",
    "rating": 4.5,
    "comment": "Excellent thriller"
  }'

# Get reviews for movie
curl "http://localhost:8082/api/v1/reviews/movie/1?page=0&size=10"

# Get rating summary
curl http://localhost:8082/api/v1/reviews/movie/1/rating
```

## 🗄️ Database Schema

### Reviews Table
```sql
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    rating DECIMAL(2,1) NOT NULL CHECK (rating >= 0.0 AND rating <= 5.0),
    comment TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_movie_id (movie_id),
    INDEX idx_rating (rating),
    INDEX idx_review_date (review_date)
);
```

### Movie Ratings Table (Aggregated View)
```sql
CREATE TABLE movie_ratings (
    movie_id BIGINT PRIMARY KEY,
    average_rating DECIMAL(3,2) NOT NULL,
    total_reviews INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_average_rating (average_rating)
);
```

### Database Features
- **Automatic Migrations**: Flyway handles schema versioning
- **Optimized Indexing**: Indexes for movie_id, rating, and review_date
- **Rating Constraints**: Check constraint ensures ratings are 0.0-5.0
- **Aggregated Statistics**: Separate table for fast rating queries
- **Connection Pooling**: HikariCP for optimal performance

## ⚙️ Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/review_db` | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | `reviewuser` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `reviewpass` | Database password |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile (`dev`, `prod`, `docker`) |
| `MOVIE_SERVICE_BASE_URL` | `http://localhost:8081` | Movie service endpoint |
| `ZIPKIN_ENDPOINT` | `http://localhost:9411/api/v2/spans` | Zipkin tracing endpoint |
| `SERVER_PORT` | `8082` | Server port |

### Spring Profiles

| Profile | Use Case | Features |
|---------|----------|----------|
| **dev** | Development | Debug logging, show SQL, relaxed timeouts |
| **prod** | Production | INFO logging, optimized performance, lower tracing sampling |
| **docker** | Docker | Container-specific URLs, INFO logging |

### Resilience4j Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      movieService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
  retry:
    instances:
      movieService:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
  timelimiter:
    instances:
      movieService:
        timeoutDuration: 3s
```

## 🧪 Testing

### Test Coverage Achievement: 89% ✅

Our comprehensive testing strategy achieves **89% code coverage**, exceeding the industry standard of 85%.

```bash
# Run all tests
mvn test

# Generate coverage report
mvn jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Test Coverage Breakdown

| Component | Coverage | Description |
|-----------|----------|-------------|
| **Controllers** | 98% | All REST endpoints tested |
| **Services** | 98% | Business logic fully covered |
| **Clients** | 59% | Movie service integration tested |
| **Repositories** | 100% | Data access layer tested |
| **Entities** | 87% | JPA entities tested |
| **Mappers** | 100% | DTO conversion tested |
| **Exception Handlers** | 95% | Error handling tested |
| **Config** | 100% | Configuration classes tested |

### Test Categories

- **Unit Tests**: Individual component testing with mocks
- **Integration Tests**: End-to-end API testing with test containers
- **Repository Tests**: Database interaction testing
- **Circuit Breaker Tests**: Resilience pattern testing

## 🛡️ Resilience Patterns

### Circuit Breaker Pattern

The service uses Resilience4j circuit breaker for Movie Service calls:

**States:**
- **CLOSED**: Normal operation, all calls go through
- **OPEN**: Too many failures, calls fail immediately
- **HALF_OPEN**: Testing if service recovered

**Configuration:**
- **Failure Rate Threshold**: 50% (opens after 50% failures)
- **Sliding Window**: 10 calls
- **Wait Duration**: 10 seconds before retry
- **Half-Open Calls**: 3 test calls

**Monitoring:**
```bash
# Check circuit breaker status
curl http://localhost:8082/actuator/circuitbreakers

# Check circuit breaker metrics
curl http://localhost:8082/actuator/metrics/resilience4j.circuitbreaker.state
```

### Retry Pattern

Automatic retries for transient failures:

- **Max Attempts**: 3
- **Wait Duration**: 1 second (exponential backoff)
- **Retryable Exceptions**: Server errors, timeouts, connection errors

### Timeout Pattern

- **Timeout Duration**: 3 seconds for Movie Service calls
- **Prevents**: Hanging requests and cascading failures

## ⚡ Performance

### Performance Characteristics

| Metric | Value | Description |
|--------|-------|-------------|
| **Startup Time** | < 20 seconds | Service ready time |
| **Response Time** | < 150ms | Average API response |
| **Throughput** | 800+ req/sec | Maximum requests per second |
| **Memory Usage** | ~250MB | Runtime memory footprint |
| **Database Connections** | 10 (pool) | HikariCP connection pool |

### Optimization Features

- **Connection Pooling**: HikariCP with optimized settings
- **Database Indexing**: Strategic indexes on frequently queried columns
- **Query Optimization**: Efficient JPA queries with pagination
- **Circuit Breaker**: Prevents cascading failures
- **Caching**: Movie title caching to reduce external calls

## 🔒 Security

### Security Features

- **Input Validation**: Bean validation on all request DTOs
- **SQL Injection Prevention**: JPA/Hibernate parameterized queries
- **Rating Constraints**: Database-level rating validation (0.0-5.0)
- **CORS Configuration**: Configurable cross-origin resource sharing
- **Security Headers**: Standard security headers in responses
- **Health Check Protection**: Actuator endpoints secured in production

## 📦 Dependencies

### Core Dependencies

```xml
<!-- Spring Boot Starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Resilience4j -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-retry</artifactId>
</dependency>

<!-- Observability -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-zipkin</artifactId>
</dependency>
```

## 🚢 Deployment

### Docker

```bash
# Build Docker image
docker build -t bookmyseat/review-service:1.0.0 .

# Run container
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3307/review_db \
  -e SPRING_DATASOURCE_USERNAME=reviewuser \
  -e SPRING_DATASOURCE_PASSWORD=reviewpass \
  -e MOVIE_SERVICE_BASE_URL=http://host.docker.internal:8081 \
  bookmyseat/review-service:1.0.0
```

### Docker Compose (Full Stack)

```bash
# Start complete environment
docker-compose up --build

# Services available:
# - Review Service: http://localhost:8082
# - Review Database: localhost:3308
```

## 📊 Monitoring

### Health Checks

```bash
# Basic health check
curl http://localhost:8082/actuator/health

# Database health
curl http://localhost:8082/actuator/health/db

# Circuit breaker health
curl http://localhost:8082/actuator/health/circuitBreakers
```

### Metrics Collection

| Endpoint | Purpose | Format |
|----------|---------|--------|
| `/actuator/metrics` | Application metrics | JSON |
| `/actuator/prometheus` | Prometheus metrics | Text |
| `/actuator/circuitbreakers` | Circuit breaker status | JSON |
| `/actuator/retries` | Retry metrics | JSON |

### Custom Metrics

- `review_requests_total`: Total review submissions
- `review_request_duration`: Review processing time
- `rating_requests_total`: Total rating queries
- `rating_request_duration`: Rating calculation time

## 🎬 Sample Data

The service automatically populates with sample data:

### Sample Reviews (50+ reviews)

- Reviews for 8 different movies
- Ratings range from 3.5 to 5.0
- Various user names and comments
- Realistic distribution of ratings

### Quick Data Check

```bash
# Check reviews for movie 1
curl http://localhost:8082/api/v1/reviews/movie/1

# Check rating summary
curl http://localhost:8082/api/v1/reviews/movie/1/rating
```

## 🏆 Best Practices

This project implements industry best practices:

1. **RESTful API Design**: Standard HTTP methods and status codes
2. **Layered Architecture**: Controller → Service → Client → Repository
3. **DTO Pattern**: Separate DTOs for request/response
4. **Resilience Patterns**: Circuit breaker, retry, timeout
5. **Database Best Practices**: Indexing, constraints, migrations
6. **Testing Excellence**: 89% code coverage
7. **Observability**: Metrics, tracing, health checks
8. **Security**: Input validation, SQL injection prevention
9. **Documentation**: OpenAPI 3.0, comprehensive README
10. **DevOps Integration**: Docker, Kubernetes, CI/CD ready

## 🛠️ Troubleshooting

### Common Issues

**Database Connection Errors**
```bash
# Check if MySQL is running
docker ps | grep mysql

# Test connection
mysql -h localhost -P 3306 -u reviewuser -p review_db
```

**Movie Service Integration Failures**
```bash
# Check circuit breaker status
curl http://localhost:8082/actuator/circuitbreakers

# Verify movie service is running
curl http://localhost:8081/actuator/health
```

**Port Already in Use (8082)**
```bash
# Find process using port
lsof -i :8082

# Kill the process
kill -9 <PID>
```

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. Fork and clone the repository
2. Create a feature branch
3. Write tests for new features
4. Ensure coverage remains above 85%
5. Submit pull request with description

## 📈 Changelog

### [1.0.0] - 2025-09-30

#### ✨ Added
- Initial release of Review Service
- Complete review CRUD operations
- Rating aggregation and statistics
- Movie Service integration with circuit breaker
- Comprehensive testing suite (89% coverage)
- Docker and Kubernetes support
- Observability with Prometheus and Zipkin
- Sample data with 50+ reviews

#### 🏗️ Architecture
- Resilience4j integration
- RestClient for service communication
- Layered architecture with client layer
- Global exception handling

---

**Maintained by**: BookMySeat Development Team
**License**: MIT
**Support**: [Create an issue](https://github.com/ALMGHAS/bookmyseat-review-service/issues)

---

<div align="center">

**⭐ Star this repo if it helped you!**

[Report Bug](https://github.com/ALMGHAS/bookmyseat-review-service/issues) · [Request Feature](https://github.com/ALMGHAS/bookmyseat-review-service/issues) · [Documentation](https://github.com/ALMGHAS/bookmyseat-review-service/wiki)

</div>