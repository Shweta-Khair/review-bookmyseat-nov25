package com.bookmyseat.reviewservice.client;

import com.bookmyseat.reviewservice.dto.MovieDetailDTO;
import com.bookmyseat.reviewservice.exception.MovieNotFoundException;
import com.bookmyseat.reviewservice.exception.MovieServiceUnavailableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.profiles.active=test"
})
class MovieServiceClientTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private MovieServiceClient movieServiceClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        registry.add("movie-service.base-url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        // Reset circuit breaker before each test
        circuitBreakerRegistry.circuitBreaker("movieService").reset();
    }

    @org.junit.jupiter.api.AfterAll
    static void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    void getMovieById_Success() throws JsonProcessingException {
        // Given
        Long movieId = 1L;
        MovieDetailDTO expectedMovie = new MovieDetailDTO(1L, "Inception", "Mind-bending thriller",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(expectedMovie))));

        // When
        MovieDetailDTO result = movieServiceClient.getMovieById(movieId);

        // Then
        assertNotNull(result);
        assertEquals(expectedMovie.getId(), result.getId());
        assertEquals(expectedMovie.getTitle(), result.getTitle());
        assertEquals(expectedMovie.getDescription(), result.getDescription());
        assertEquals(expectedMovie.getDurationMinutes(), result.getDurationMinutes());
        assertEquals(expectedMovie.getGenre(), result.getGenre());
        assertEquals(expectedMovie.getLanguage(), result.getLanguage());
        assertEquals(expectedMovie.getReleaseDate(), result.getReleaseDate());
    }

    @Test
    void getMovieById_MovieNotFound() {
        // Given
        Long movieId = 999L;

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Movie not found with ID: " + movieId + "\"}")));

        // When & Then
        MovieNotFoundException exception = assertThrows(MovieNotFoundException.class,
                () -> movieServiceClient.getMovieById(movieId));

        assertEquals("Movie not found with ID: " + movieId, exception.getMessage());
    }

    @Test
    void getMovieById_ServiceUnavailable_FallbackCalled() {
        // Given
        Long movieId = 1L;

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Service Unavailable\"}")));

        // When
        MovieDetailDTO result = movieServiceClient.getMovieById(movieId);

        // Then - fallback should return a movie with "Unknown Movie" title
        assertNotNull(result);
        assertEquals(movieId, result.getId());
        assertEquals("Unknown Movie", result.getTitle());
        assertEquals("Movie details temporarily unavailable", result.getDescription());
    }

    @Test
    void getMovieById_Timeout_FallbackCalled() {
        // Given
        Long movieId = 1L;

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"title\":\"Inception\"}")
                        .withFixedDelay(5000))); // Delay longer than timeout

        // When
        MovieDetailDTO result = movieServiceClient.getMovieById(movieId);

        // Then - fallback should be called due to timeout
        assertNotNull(result);
        assertEquals(movieId, result.getId());
        assertEquals("Unknown Movie", result.getTitle());
        assertEquals("Movie details temporarily unavailable", result.getDescription());
    }

    @Test
    void getMovieById_CircuitBreakerOpens() {
        // Given
        Long movieId = 1L;

        // Stub service to always return 500 (failure)
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Internal Server Error\"}")));

        // When - Make enough failed calls to open the circuit breaker
        // Need at least 3 calls (minimumNumberOfCalls) with 50% failure rate
        for (int i = 0; i < 4; i++) {
            MovieDetailDTO result = movieServiceClient.getMovieById(movieId);
            // Should get fallback response
            assertNotNull(result);
            assertEquals("Unknown Movie", result.getTitle());
        }

        // Verify circuit breaker is now open
        assertTrue(circuitBreakerRegistry.circuitBreaker("movieService").getState().toString().contains("OPEN") ||
                   circuitBreakerRegistry.circuitBreaker("movieService").getState().toString().contains("HALF_OPEN"));
    }

    @Test
    void getMovieById_RetryMechanism() {
        // Given
        Long movieId = 1L;
        MovieDetailDTO expectedMovie = new MovieDetailDTO(1L, "Inception", "Mind-bending thriller",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));

        // Reset circuit breaker to ensure clean state
        circuitBreakerRegistry.circuitBreaker("movieService").reset();

        // First call fails, second call succeeds (testing retry)
        // Set up both stubs before making any calls
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .atPriority(1)
                .inScenario("retry-scenario")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Internal Server Error\"}"))
                .willSetStateTo("first-attempt"));

        try {
            wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                    .atPriority(1)
                    .inScenario("retry-scenario")
                    .whenScenarioStateIs("first-attempt")
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(objectMapper.writeValueAsString(expectedMovie))));
        } catch (JsonProcessingException e) {
            fail("Failed to serialize expected movie: " + e.getMessage());
        }

        // When
        MovieDetailDTO result = movieServiceClient.getMovieById(movieId);

        // Then - Should get successful response after retry
        assertNotNull(result);
        assertEquals(expectedMovie.getTitle(), result.getTitle());

        // Verify that retry happened (2 requests should have been made)
        wireMockServer.verify(2, getRequestedFor(urlEqualTo("/api/v1/movies/" + movieId)));
    }

    @Test
    void getMovieById_JsonParsingError_FallbackCalled() {
        // Given
        Long movieId = 1L;

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ invalid json }")));

        // When
        MovieDetailDTO result = movieServiceClient.getMovieById(movieId);

        // Then - fallback should be called due to JSON parsing error
        assertNotNull(result);
        assertEquals(movieId, result.getId());
        assertEquals("Unknown Movie", result.getTitle());
        assertEquals("Movie details temporarily unavailable", result.getDescription());
    }

    @Test
    void getMovieById_ConnectionRefused_FallbackCalled() {
        // Given
        Long movieId = 1L;

        // Stop WireMock to simulate connection refused
        wireMockServer.stop();

        // When
        MovieDetailDTO result = movieServiceClient.getMovieById(movieId);

        // Then - fallback should be called
        assertNotNull(result);
        assertEquals(movieId, result.getId());
        assertEquals("Unknown Movie", result.getTitle());
        assertEquals("Movie details temporarily unavailable", result.getDescription());

        // Restart WireMock for other tests
        wireMockServer.start();
    }

    @Test
    void movieExists_MovieFound_ReturnsTrue() throws JsonProcessingException {
        // Given
        Long movieId = 1L;
        MovieDetailDTO movie = new MovieDetailDTO(1L, "Inception", "Mind-bending thriller",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(movie))));

        // When
        boolean result = movieServiceClient.movieExists(movieId);

        // Then
        assertTrue(result);
    }

    @Test
    void movieExists_MovieNotFound_ReturnsFalse() {
        // Given
        Long movieId = 999L;

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Movie not found\"}")));

        // When
        boolean result = movieServiceClient.movieExists(movieId);

        // Then
        assertFalse(result);
    }

    @Test
    void movieExists_ServiceUnavailable_ThrowsException() {
        // Given
        Long movieId = 1L;

        wireMockServer.stubFor(get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Service Unavailable\"}")));

        // When & Then
        assertThrows(MovieServiceUnavailableException.class, () -> {
            movieServiceClient.movieExists(movieId);
        });
    }
}