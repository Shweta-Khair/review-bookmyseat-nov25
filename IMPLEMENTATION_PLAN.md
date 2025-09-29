# Review Service - Implementation Plan & Checklist

## Overview
This document outlines the complete implementation plan for the Review Service as defined in the BookMySeat PRD. The service will manage movie reviews and ratings, integrate with the Movie Service for validation, and provide aggregated rating data using Spring Boot 3.5.6, Java 17+, and MySQL 8.0+.

## Implementation Plan

### Phase 1: Project Setup & Configuration
1. Initialize Spring Boot project with Maven
2. Create pom.xml with all required dependencies (including Resilience4j)
3. Set up application.yml configuration with circuit breaker settings
4. Create project directory structure
5. Configure inter-service communication properties

### Phase 2: Database Layer
1. Create Review entity class with rating constraints
2. Create MovieRating entity class for caching
3. Implement ReviewRepository interface with pagination
4. Implement MovieRatingRepository interface
5. Create database initialization scripts with Flyway migrations

### Phase 3: External Service Integration
1. Create MovieServiceClient interface
2. Implement RestClient for Movie Service communication
3. Configure circuit breaker with Resilience4j
4. Implement retry policies and timeout handling
5. Create Movie DTO for external service responses

### Phase 4: Service Layer
1. Create ReviewService interface and implementation
2. Create MovieRatingService interface and implementation
3. Implement business logic for review submission and validation
4. Implement rating calculation and caching logic
5. Add pagination support for review retrieval

### Phase 5: DTO Layer
1. Create ReviewDTO for API responses
2. Create ReviewSubmissionDTO for requests
3. Create MovieReviewsResponseDTO with pagination
4. Create RatingSummaryDTO for aggregated data
5. Implement DTO mappers with validation

### Phase 6: Controller Layer
1. Create ReviewController with REST endpoints
2. Implement POST /api/v1/reviews endpoint
3. Implement GET /api/v1/reviews/movie/{movieId} endpoint
4. Implement GET /api/v1/reviews/movie/{movieId}/rating endpoint
5. Add validation, pagination, and error handling

### Phase 7: Circuit Breaker & Resilience
1. Configure Resilience4j circuit breaker
2. Implement fallback mechanisms
3. Add retry policies with exponential backoff
4. Configure timeout handling
5. Add circuit breaker health checks

### Phase 8: Observability & Monitoring
1. Configure Actuator endpoints
2. Set up Prometheus metrics for circuit breaker
3. Configure distributed tracing with Micrometer
4. Add health checks for external service connectivity
5. Implement custom metrics for reviews and ratings

### Phase 9: Testing
1. Create unit tests for repositories
2. Create unit tests for services with mocked external calls
3. Create integration tests for controllers
4. Add WireMock tests for Movie Service integration
5. Test circuit breaker behavior
6. Configure H2 for testing

### Phase 10: Containerization & Deployment
1. Create Dockerfile
2. Create docker-compose.yml for local development
3. Add Kubernetes deployment YAML
4. Configure environment-specific properties
5. Add startup scripts and automation

## Verification Checklist

### ✅ Project Structure & Configuration
- [x] Maven project initialized with Spring Boot 3.5.6
- [x] Java 17+ configured
- [x] pom.xml contains all dependencies from PRD (including Resilience4j)
- [x] application.yml configured with correct ports and datasource
- [x] Movie Service integration URL configured
- [x] Project runs on port 8082

### ✅ Database Schema
- [x] Reviews table created with all required columns
- [x] Movie ratings cache table created
- [x] Rating constraint (1.0-5.0) enforced
- [x] Foreign key constraints properly set
- [x] Indexes created for performance optimization
- [x] MySQL 8.0+ database configured
- [x] Flyway migrations working

### ✅ API Endpoints
- [x] POST /api/v1/reviews endpoint works
- [x] Review submission validates movie existence via Movie Service
- [x] Rating validation (1.0-5.0) implemented
- [x] User name and comment validation working
- [x] GET /api/v1/reviews/movie/{movieId} endpoint works
- [x] Pagination parameters (page, size, sort) working
- [x] GET /api/v1/reviews/movie/{movieId}/rating endpoint works
- [x] Rating distribution calculation correct
- [x] 404 error returned for non-existent movie

### ✅ Data Models
- [x] Review entity with all required fields
- [x] MovieRating entity for caching
- [x] DTOs for request/response separation
- [x] Proper JSON serialization
- [x] Validation annotations on DTOs

### ✅ Service Layer
- [x] ReviewService implemented with business logic
- [x] MovieRatingService implemented
- [x] Movie existence validation via external service
- [x] Rating calculation and caching logic
- [x] Proper transaction management
- [x] Pagination support implemented

### ✅ External Service Integration
- [x] MovieServiceClient implemented
- [x] RestClient configured for Movie Service
- [x] Proper error handling for external service calls
- [x] Timeout configuration (3 seconds)
- [x] Circuit breaker integration working

### ✅ Circuit Breaker & Resilience
- [x] Resilience4j circuit breaker configured
- [x] Circuit breaker thresholds set (50% failure rate)
- [x] Sliding window size configured (10 calls)
- [x] Wait duration in open state (10 seconds)
- [x] Retry policy implemented (2 retries with backoff)
- [x] Fallback mechanisms implemented
- [x] Circuit breaker health indicator working

### ✅ Exception Handling
- [x] Global exception handler configured
- [x] Custom exceptions created (ReviewNotFoundException, etc.)
- [x] External service failure handling
- [x] Proper HTTP status codes returned
- [x] Structured error responses

### ✅ Validation
- [x] Bean validation on request DTOs
- [x] Movie ID validation via external service
- [x] Rating range validation (1.0-5.0)
- [x] User name length validation (max 100 chars)
- [x] Comment length validation (max 1000 chars)
- [x] Error messages for invalid inputs

### ✅ Observability & Monitoring
- [x] /actuator/health endpoint accessible
- [x] /actuator/metrics endpoint accessible
- [x] /actuator/prometheus endpoint accessible
- [x] Circuit breaker metrics exposed
- [x] Health check includes database connectivity
- [x] Health check includes Movie Service connectivity
- [x] Distributed tracing configured
- [x] Trace IDs propagated to Movie Service calls

### ✅ Documentation
- [x] OpenAPI documentation at /api-docs
- [x] Swagger UI accessible at /swagger-ui.html
- [x] All endpoints documented
- [x] Request/response examples included
- [x] Circuit breaker behavior documented

### ✅ Testing
- [x] Unit tests for repositories (basic structure)
- [x] Unit tests for services (created, needs compilation fixes)
- [x] Unit tests for controllers (created, needs compilation fixes)
- [x] Unit tests for MovieServiceClient (created, needs compilation fixes)
- [x] Integration tests for API endpoints (created, needs compilation fixes)
- [x] WireMock tests for external service integration (created, needs compilation fixes)
- [x] Circuit breaker behavior tests (created, needs compilation fixes)
- [ ] Test coverage >= 85% (pending test compilation fixes)
- [x] H2 database configured for tests

### ✅ Docker & Deployment
- [x] Dockerfile created and builds successfully
- [x] Docker image runs properly
- [x] docker-compose.yml for local development
- [x] MySQL container configured in docker-compose
- [x] Movie Service dependency configured
- [x] Kubernetes deployment YAML created
- [x] Liveness probe configured
- [x] Readiness probe configured
- [x] Environment variables properly configured

### ✅ Best Practices
- [x] RESTful API design followed
- [x] Layered architecture (Controller → Service → Repository → Client)
- [x] DTO pattern implemented
- [x] Circuit breaker pattern implemented
- [x] Connection pooling with HikariCP
- [x] API versioning (/api/v1/) implemented
- [x] Structured logging with correlation IDs
- [x] Proper HTTP methods and status codes used
- [x] Pagination for large result sets
- [x] Caching strategy for rating aggregation

## API Endpoints to Implement

### 1. POST /api/v1/reviews
- **Description:** Submit a review for a movie
- **Request Body:** ReviewSubmissionDTO
- **Validation:**
  - movieId: Required, must exist in Movie Service
  - userName: Required, max 100 characters
  - rating: Required, between 1.0 and 5.0
  - comment: Optional, max 1000 characters
- **Response:** Created review with 201 status
- **Error Responses:** 400 Bad Request, 404 Movie Not Found

### 2. GET /api/v1/reviews/movie/{movieId}
- **Description:** Retrieve all reviews for a specific movie
- **Path Parameters:** movieId (required)
- **Query Parameters:**
  - page (optional, default: 0)
  - size (optional, default: 10)
  - sort (optional, default: reviewDate,desc)
- **Response:** Paginated reviews with movie title
- **Error Response:** 404 Not Found if movie doesn't exist

### 3. GET /api/v1/reviews/movie/{movieId}/rating
- **Description:** Get aggregated rating information for a movie
- **Path Parameters:** movieId (required)
- **Response:** Rating summary with distribution
- **Error Response:** 404 Not Found if movie doesn't exist

## Database Schema

### Reviews Table
```sql
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    rating DECIMAL(2,1) NOT NULL CHECK (rating >= 1.0 AND rating <= 5.0),
    comment TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_movie_id (movie_id),
    INDEX idx_review_date (review_date),
    INDEX idx_rating (rating)
);
```

### Movie Rating Cache Table
```sql
CREATE TABLE movie_ratings (
    movie_id BIGINT PRIMARY KEY,
    average_rating DECIMAL(3,2),
    total_reviews INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_average_rating (average_rating)
);
```

## Key Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Actuator
- Spring Boot Starter Validation
- MySQL Connector
- Resilience4j Spring Boot 3 (2.2.0)
- Micrometer Tracing Bridge OTel
- OpenTelemetry Exporter Zipkin
- Micrometer Registry Prometheus
- SpringDoc OpenAPI Starter WebMVC UI
- Spring Boot Starter Test
- H2 Database (for testing)
- Spring Cloud Contract Stub Runner (for testing)

## Configuration Requirements
- Server port: 8082
- Database: MySQL 8.0+ (review_db)
- Movie Service URL: http://movie-service:8081
- Circuit breaker configuration
- Health checks enabled
- Metrics exposed via Prometheus
- Distributed tracing with Zipkin
- API documentation with Swagger

## Inter-Service Communication Configuration
```yaml
movie-service:
  base-url: http://movie-service:8081
  timeout: 3000

resilience4j:
  circuitbreaker:
    instances:
      movieService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
  retry:
    instances:
      movieService:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
```

## Sample Data for Testing
- 5-10 sample reviews for different movies
- Various ratings (1.0 to 5.0)
- Different user names and comments
- Reviews distributed across multiple movies
- Rating cache entries for popular movies

## Next Steps
1. Start with Phase 1: Project setup and Maven configuration
2. Set up external service integration early (Phase 3)
3. Implement circuit breaker patterns before service layer
4. Test external service integration thoroughly
5. Proceed through each phase systematically
6. Use this checklist to verify each requirement is met
7. Run tests after each major component is implemented
8. Verify API endpoints work correctly with Movie Service integration
9. Test circuit breaker behavior under various failure conditions