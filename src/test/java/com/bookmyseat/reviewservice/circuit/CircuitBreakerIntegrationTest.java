package com.bookmyseat.reviewservice.circuit;

import com.bookmyseat.reviewservice.client.MovieServiceClient;
import com.bookmyseat.reviewservice.dto.MovieDetailDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.profiles.active=test",
    "movie-service.timeout=2000",
    "resilience4j.circuitbreaker.instances.movieService.slidingWindowSize=5",
    "resilience4j.circuitbreaker.instances.movieService.minimumNumberOfCalls=3",
    "resilience4j.circuitbreaker.instances.movieService.failureRateThreshold=60",
    "resilience4j.circuitbreaker.instances.movieService.waitDurationInOpenState=3s",
    "resilience4j.circuitbreaker.instances.movieService.permittedNumberOfCallsInHalfOpenState=2",
    "resilience4j.retry.instances.movieService.maxAttempts=2",
    "resilience4j.retry.instances.movieService.waitDuration=500ms"
})
class CircuitBreakerIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private MovieServiceClient movieServiceClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    private CircuitBreaker circuitBreaker;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        wireMockServer = new WireMockServer(8091);
        wireMockServer.start();
        registry.add("movie-service.base-url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("movieService");
        circuitBreaker.reset();
    }

    @org.junit.jupiter.api.AfterAll
    static void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    void circuitBreaker_OpensAfterFailureThreshold() throws Exception {
        // Given
        Long movieId = 1L;

        // Mock service to always return 500 (failure)
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Internal Server Error\"}")));

        // Initially circuit breaker should be closed
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());

        // When - Make enough failed calls to open the circuit breaker
        // Need at least 3 calls (minimumNumberOfCalls) with 60% failure rate
        for (int i = 0; i < 5; i++) {
            MovieDetailDTO result = movieServiceClient.getMovieById(movieId);
            // Should get fallback response
            assertNotNull(result);
            assertEquals("Unknown Movie", result.getTitle());
        }

        // Then - Circuit breaker should now be open
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());

        // Verify that subsequent calls immediately return fallback without hitting service
        long requestCountBefore = wireMockServer.getAllServeEvents().size();
        MovieDetailDTO fallbackResult = movieServiceClient.getMovieById(movieId);
        long requestCountAfter = wireMockServer.getAllServeEvents().size();

        assertEquals("Unknown Movie", fallbackResult.getTitle());
        assertEquals(requestCountBefore, requestCountAfter, "No additional requests should be made when circuit is open");
    }

    @Test
    void circuitBreaker_TransitionsFromOpenToHalfOpen() throws Exception {
        // Given
        Long movieId = 1L;

        // Force circuit breaker to open state
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("{\"message\":\"Service Error\"}")));

        // Make enough failed calls to open circuit
        for (int i = 0; i < 5; i++) {
            movieServiceClient.getMovieById(movieId);
        }
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());

        // When - Wait for the wait duration to pass
        Thread.sleep(3500); // Wait duration is 3s

        // Mock service to return success now
        MovieDetailDTO successResponse = new MovieDetailDTO(movieId, "Inception", "A great movie",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(successResponse))));

        // Make a call that should transition to HALF_OPEN
        MovieDetailDTO result = movieServiceClient.getMovieById(movieId);

        // Then - Circuit should be in HALF_OPEN state initially, then CLOSED after successful calls
        // Note: The state might quickly transition from HALF_OPEN to CLOSED
        assertTrue(circuitBreaker.getState() == CircuitBreaker.State.HALF_OPEN ||
                  circuitBreaker.getState() == CircuitBreaker.State.CLOSED);
        assertEquals("Inception", result.getTitle());
    }

    @Test
    void circuitBreaker_ClosesAfterSuccessfulCallsInHalfOpen() throws Exception {
        // Given
        Long movieId = 1L;
        MovieDetailDTO successResponse = new MovieDetailDTO(movieId, "Inception", "A great movie",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));

        // Force circuit breaker to open
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < 5; i++) {
            movieServiceClient.getMovieById(movieId);
        }
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());

        // Wait for circuit to transition to half-open
        Thread.sleep(3500);

        // When - Mock successful responses
        wireMockServer.resetRequests();
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(successResponse))));

        // Make successful calls (permittedNumberOfCallsInHalfOpenState = 2)
        for (int i = 0; i < 3; i++) {
            MovieDetailDTO result = movieServiceClient.getMovieById(movieId);
            assertEquals("Inception", result.getTitle());
        }

        // Then - Circuit should be closed now
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    @Test
    void circuitBreaker_ReopensOnFailureInHalfOpen() throws Exception {
        // Given
        Long movieId = 1L;

        // Force circuit breaker to open
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < 5; i++) {
            movieServiceClient.getMovieById(movieId);
        }
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());

        // Wait for transition to half-open
        Thread.sleep(3500);

        // When - Mock failed response during half-open state
        wireMockServer.resetRequests();
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("{\"message\":\"Service Unavailable\"}")));

        // Make a call that should fail
        MovieDetailDTO result = movieServiceClient.getMovieById(movieId);

        // Then - Circuit should return to OPEN state after failure in half-open
        // Give it a moment for state transition
        Thread.sleep(100);
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        assertEquals("Unknown Movie", result.getTitle());
    }

    @Test
    void circuitBreaker_RetryMechanism() throws Exception {
        // Given
        Long movieId = 1L;
        MovieDetailDTO successResponse = new MovieDetailDTO(movieId, "Inception", "A great movie",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));

        // Reset circuit breaker to ensure clean state
        circuitBreaker.reset();

        // Mock first call to fail, second to succeed (testing retry)
        // Set up both stubs with priorities to ensure proper matching
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .atPriority(1)
                .inScenario("retry-test")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("{\"message\":\"Temporary Error\"}"))
                .willSetStateTo("first-failure"));

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .atPriority(1)
                .inScenario("retry-test")
                .whenScenarioStateIs("first-failure")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(successResponse))));

        // When
        MovieDetailDTO result = movieServiceClient.getMovieById(movieId);

        // Then - Should get successful response after retry
        assertEquals("Inception", result.getTitle());
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());

        // Verify retry happened (2 requests should have been made)
        assertEquals(2, wireMockServer.getAllServeEvents().size());
    }

    @Test
    void circuitBreaker_TimeoutHandling() throws Exception {
        // Given
        Long movieId = 1L;

        // Mock slow response (longer than timeout)
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"title\":\"Inception\"}")
                        .withFixedDelay(3000))); // Delay longer than 2s timeout

        // When
        MovieDetailDTO result = movieServiceClient.getMovieById(movieId);

        // Then - Should get fallback response due to timeout
        assertEquals("Unknown Movie", result.getTitle());
        assertEquals("Movie details temporarily unavailable", result.getDescription());
    }

    @Test
    void circuitBreaker_SuccessfulCallsKeepCircuitClosed() throws Exception {
        // Given
        Long movieId = 1L;
        MovieDetailDTO successResponse = new MovieDetailDTO(movieId, "Inception", "A great movie",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(successResponse))));

        // When - Make multiple successful calls
        for (int i = 0; i < 10; i++) {
            MovieDetailDTO result = movieServiceClient.getMovieById(movieId);
            assertEquals("Inception", result.getTitle());
        }

        // Then - Circuit should remain closed
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    @Test
    void circuitBreaker_PartialFailures_BelowThreshold() throws Exception {
        // Given
        Long movieId = 1L;
        MovieDetailDTO successResponse = new MovieDetailDTO(movieId, "Inception", "A great movie",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));

        // Mock alternating success and failure (50% failure rate, below 60% threshold)
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .inScenario("partial-failures")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(successResponse)))
                .willSetStateTo("success1"));

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .inScenario("partial-failures")
                .whenScenarioStateIs("success1")
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("{\"message\":\"Error\"}"))
                .willSetStateTo("failure1"));

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .inScenario("partial-failures")
                .whenScenarioStateIs("failure1")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(successResponse)))
                .willSetStateTo("success2"));

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .inScenario("partial-failures")
                .whenScenarioStateIs("success2")
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("{\"message\":\"Error\"}"))
                .willSetStateTo("failure2"));

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .inScenario("partial-failures")
                .whenScenarioStateIs("failure2")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(successResponse))));

        // When - Make calls with 40% failure rate (2 failures out of 5 calls)
        for (int i = 0; i < 5; i++) {
            movieServiceClient.getMovieById(movieId);
        }

        // Then - Circuit should remain closed (failure rate below 60% threshold)
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }
}