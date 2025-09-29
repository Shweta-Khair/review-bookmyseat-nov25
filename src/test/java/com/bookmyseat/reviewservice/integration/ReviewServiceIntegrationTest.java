package com.bookmyseat.reviewservice.integration;

import com.bookmyseat.reviewservice.dto.MovieDetailDTO;
import com.bookmyseat.reviewservice.dto.ReviewSubmissionDTO;
import com.bookmyseat.reviewservice.entity.Review;
import com.bookmyseat.reviewservice.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.profiles.active=test"
    })
@AutoConfigureWebMvc
@Transactional
class ReviewServiceIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        wireMockServer = new WireMockServer(8090);
        wireMockServer.start();
        registry.add("movie-service.base-url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        wireMockServer.resetAll();
        reviewRepository.deleteAll();
        // Reset circuit breaker to ensure clean state for each test
        circuitBreakerRegistry.circuitBreaker("movieService").reset();
    }

    @org.junit.jupiter.api.AfterAll
    static void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @Test
    void submitReview_EndToEnd_Success() throws Exception {
        // Given
        Long movieId = 1L;
        MovieDetailDTO movieDetail = new MovieDetailDTO(movieId, "Inception", "Mind-bending thriller",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));

        wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(movieDetail))));

        ReviewSubmissionDTO reviewSubmission = new ReviewSubmissionDTO(movieId, "John Doe",
                BigDecimal.valueOf(4.5), "Excellent movie!");

        // When & Then
        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewSubmission)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movieId").value(movieId))
                .andExpect(jsonPath("$.movieTitle").value("Inception"))
                .andExpect(jsonPath("$.userName").value("John Doe"))
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.comment").value("Excellent movie!"))
                .andExpect(jsonPath("$.reviewDate").exists());

        // Verify data is persisted
        List<Review> savedReviews = reviewRepository.findAll();
        assertEquals(1, savedReviews.size());
        assertEquals(movieId, savedReviews.get(0).getMovieId());
        assertEquals("John Doe", savedReviews.get(0).getUserName());
        assertEquals(BigDecimal.valueOf(4.5), savedReviews.get(0).getRating());
    }

    @Test
    void getReviewsForMovie_EndToEnd_Success() throws Exception {
        // Given
        Long movieId = 1L;
        MovieDetailDTO movieDetail = new MovieDetailDTO(movieId, "Inception", "Mind-bending thriller",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));

        wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(movieDetail))));

        // Create test reviews
        Review review1 = new Review(movieId, "Alice", BigDecimal.valueOf(5.0), "Amazing!");
        Review review2 = new Review(movieId, "Bob", BigDecimal.valueOf(4.0), "Good movie");
        reviewRepository.saveAll(List.of(review1, review2));

        // When & Then
        mockMvc.perform(get("/api/v1/reviews/movie/" + movieId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews", hasSize(2)))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.totalReviews").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void getMovieRatingSummary_EndToEnd_Success() throws Exception {
        // Given
        Long movieId = 1L;
        MovieDetailDTO movieDetail = new MovieDetailDTO(movieId, "Inception", "Mind-bending thriller",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));

        wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(movieDetail))));

        // Create test reviews with different ratings
        Review review1 = new Review(movieId, "Alice", BigDecimal.valueOf(5.0), "Amazing!");
        Review review2 = new Review(movieId, "Bob", BigDecimal.valueOf(4.0), "Good movie");
        Review review3 = new Review(movieId, "Charlie", BigDecimal.valueOf(5.0), "Excellent!");
        reviewRepository.saveAll(List.of(review1, review2, review3));

        // When & Then
        mockMvc.perform(get("/api/v1/reviews/movie/" + movieId + "/rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieId").value(movieId))
                .andExpect(jsonPath("$.movieTitle").value("Inception"))
                .andExpect(jsonPath("$.totalReviews").value(3))
                .andExpect(jsonPath("$.ratingDistribution.5").value(2))
                .andExpect(jsonPath("$.ratingDistribution.4").value(1))
                .andExpect(jsonPath("$.averageRating").value(closeTo(4.67, 0.1)));
    }

    @Test
    void submitReview_MovieNotFound_Returns404() throws Exception {
        // Given
        Long movieId = 999L;

        wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Movie not found with ID: " + movieId + "\"}")));

        ReviewSubmissionDTO reviewSubmission = new ReviewSubmissionDTO(movieId, "John Doe",
                BigDecimal.valueOf(4.5), "Review for non-existent movie");

        // When & Then
        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewSubmission)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie not found with ID: " + movieId));

        // Verify no review was saved
        List<Review> savedReviews = reviewRepository.findAll();
        assertEquals(0, savedReviews.size());
    }

    @Test
    void submitReview_InvalidRating_Returns400() throws Exception {
        // Given
        ReviewSubmissionDTO invalidReview = new ReviewSubmissionDTO(1L, "John Doe",
                BigDecimal.valueOf(6.0), "Invalid rating");

        // When & Then
        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReview)))
                .andExpect(status().isBadRequest());

        // Verify no review was saved
        List<Review> savedReviews = reviewRepository.findAll();
        assertEquals(0, savedReviews.size());
    }

    @Test
    void getReviewsForMovie_MovieServiceDown_FallbackBehavior() throws Exception {
        // Given
        Long movieId = 1L;

        // Movie service is down (500 error)
        wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Internal Server Error\"}")));

        // Create test reviews
        Review review1 = new Review(movieId, "Alice", BigDecimal.valueOf(5.0), "Amazing!");
        reviewRepository.save(review1);

        // When & Then - Should still return reviews but with fallback movie title
        mockMvc.perform(get("/api/v1/reviews/movie/" + movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews", hasSize(1)))
                .andExpect(jsonPath("$.reviews[0].movieTitle").value("Unknown Movie"));
    }

    @Test
    void getReviewById_Success() throws Exception {
        // Given
        Long movieId = 1L;
        MovieDetailDTO movieDetail = new MovieDetailDTO(movieId, "Inception", "Mind-bending thriller",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));

        wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/v1/movies/" + movieId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(movieDetail))));

        Review savedReview = reviewRepository.save(
                new Review(movieId, "John Doe", BigDecimal.valueOf(4.5), "Great movie!"));

        // When & Then
        mockMvc.perform(get("/api/v1/reviews/" + savedReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedReview.getId()))
                .andExpect(jsonPath("$.movieId").value(movieId))
                .andExpect(jsonPath("$.movieTitle").value("Inception"))
                .andExpect(jsonPath("$.userName").value("John Doe"))
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.comment").value("Great movie!"));
    }

    @Test
    void getReviewById_NotFound_Returns404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/reviews/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Review not found with ID: 999"));
    }

    @Test
    void hasReviews_WithReviews_ReturnsTrue() throws Exception {
        // Given
        Long movieId = 1L;
        reviewRepository.save(new Review(movieId, "John Doe", BigDecimal.valueOf(4.5), "Great!"));

        // When & Then
        mockMvc.perform(get("/api/v1/reviews/movie/" + movieId + "/has-reviews"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void hasReviews_WithoutReviews_ReturnsFalse() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/reviews/movie/999/has-reviews"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void getReviewCount_Success() throws Exception {
        // Given
        Long movieId = 1L;
        reviewRepository.save(new Review(movieId, "Alice", BigDecimal.valueOf(5.0), "Amazing!"));
        reviewRepository.save(new Review(movieId, "Bob", BigDecimal.valueOf(4.0), "Good!"));
        reviewRepository.save(new Review(movieId, "Charlie", BigDecimal.valueOf(3.0), "OK"));

        // When & Then
        mockMvc.perform(get("/api/v1/reviews/movie/" + movieId + "/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }
}