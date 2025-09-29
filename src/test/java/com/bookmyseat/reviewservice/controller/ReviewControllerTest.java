package com.bookmyseat.reviewservice.controller;

import com.bookmyseat.reviewservice.dto.*;
import com.bookmyseat.reviewservice.exception.MovieNotFoundException;
import com.bookmyseat.reviewservice.exception.ReviewNotFoundException;
import com.bookmyseat.reviewservice.service.MovieRatingService;
import com.bookmyseat.reviewservice.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private MovieRatingService movieRatingService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReviewSubmissionDTO reviewSubmissionDTO;
    private ReviewDTO reviewDTO;
    private MovieReviewsResponseDTO movieReviewsResponseDTO;
    private RatingSummaryDTO ratingSummaryDTO;

    @BeforeEach
    void setUp() {
        reviewSubmissionDTO = new ReviewSubmissionDTO(1L, "John Doe",
                BigDecimal.valueOf(4.5), "Great movie!");

        reviewDTO = new ReviewDTO(1L, 1L, "Inception", "John Doe",
                BigDecimal.valueOf(4.5), "Great movie!", LocalDateTime.now());

        List<ReviewDTO> reviews = Arrays.asList(reviewDTO);
        movieReviewsResponseDTO = new MovieReviewsResponseDTO(reviews,
                BigDecimal.valueOf(4.5), 1L, 0, 1, 10, true, true);

        Map<String, Integer> ratingDistribution = new HashMap<>();
        ratingDistribution.put("5", 2);
        ratingDistribution.put("4", 1);
        ratingSummaryDTO = new RatingSummaryDTO(1L, "Inception",
                BigDecimal.valueOf(4.5), 3, ratingDistribution);
    }

    @Test
    void submitReview_Success() throws Exception {
        when(reviewService.submitReview(any(ReviewSubmissionDTO.class)))
                .thenReturn(reviewDTO);

        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewSubmissionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.movieId").value(1L))
                .andExpect(jsonPath("$.movieTitle").value("Inception"))
                .andExpect(jsonPath("$.userName").value("John Doe"))
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.comment").value("Great movie!"));
    }

    @Test
    void submitReview_InvalidRating() throws Exception {
        ReviewSubmissionDTO invalidReview = new ReviewSubmissionDTO(1L, "John Doe",
                BigDecimal.valueOf(6.0), "Invalid rating");

        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReview)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitReview_MissingMovieId() throws Exception {
        ReviewSubmissionDTO invalidReview = new ReviewSubmissionDTO(null, "John Doe",
                BigDecimal.valueOf(4.5), "Missing movie ID");

        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReview)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitReview_MissingUserName() throws Exception {
        ReviewSubmissionDTO invalidReview = new ReviewSubmissionDTO(1L, null,
                BigDecimal.valueOf(4.5), "Missing user name");

        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReview)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitReview_MovieNotFound() throws Exception {
        when(reviewService.submitReview(any(ReviewSubmissionDTO.class)))
                .thenThrow(new MovieNotFoundException("Movie not found with ID: 999"));

        ReviewSubmissionDTO invalidReview = new ReviewSubmissionDTO(999L, "John Doe",
                BigDecimal.valueOf(4.5), "Nonexistent movie");

        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReview)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie not found with ID: 999"));
    }

    @Test
    void getReviewsForMovie_Success() throws Exception {
        when(reviewService.getReviewsForMovie(eq(1L), any(Pageable.class)))
                .thenReturn(movieReviewsResponseDTO);

        mockMvc.perform(get("/api/v1/reviews/movie/1")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "reviewDate,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews[0].id").value(1L))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.totalReviews").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void getReviewsForMovie_WithDefaultPagination() throws Exception {
        when(reviewService.getReviewsForMovie(eq(1L), any(Pageable.class)))
                .thenReturn(movieReviewsResponseDTO);

        mockMvc.perform(get("/api/v1/reviews/movie/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews").isArray());
    }

    @Test
    void getReviewsForMovie_MovieNotFound() throws Exception {
        when(reviewService.getReviewsForMovie(eq(999L), any(Pageable.class)))
                .thenThrow(new MovieNotFoundException("Movie not found with ID: 999"));

        mockMvc.perform(get("/api/v1/reviews/movie/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie not found with ID: 999"));
    }

    @Test
    void getMovieRatingSummary_Success() throws Exception {
        when(movieRatingService.getMovieRatingSummary(1L))
                .thenReturn(ratingSummaryDTO);

        mockMvc.perform(get("/api/v1/reviews/movie/1/rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieId").value(1L))
                .andExpect(jsonPath("$.movieTitle").value("Inception"))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.totalReviews").value(3))
                .andExpect(jsonPath("$.ratingDistribution.5").value(2))
                .andExpect(jsonPath("$.ratingDistribution.4").value(1));
    }

    @Test
    void getMovieRatingSummary_MovieNotFound() throws Exception {
        when(movieRatingService.getMovieRatingSummary(999L))
                .thenThrow(new MovieNotFoundException("Movie not found with ID: 999"));

        mockMvc.perform(get("/api/v1/reviews/movie/999/rating"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Movie not found with ID: 999"));
    }

    @Test
    void getReviewById_Success() throws Exception {
        when(reviewService.getReviewById(1L)).thenReturn(reviewDTO);

        mockMvc.perform(get("/api/v1/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.movieId").value(1L))
                .andExpect(jsonPath("$.movieTitle").value("Inception"))
                .andExpect(jsonPath("$.userName").value("John Doe"))
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.comment").value("Great movie!"));
    }

    @Test
    void getReviewById_ReviewNotFound() throws Exception {
        when(reviewService.getReviewById(999L))
                .thenThrow(new ReviewNotFoundException("Review not found with ID: 999"));

        mockMvc.perform(get("/api/v1/reviews/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Review not found with ID: 999"));
    }

    @Test
    void hasReviews_True() throws Exception {
        when(reviewService.hasReviews(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/reviews/movie/1/has-reviews"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void hasReviews_False() throws Exception {
        when(reviewService.hasReviews(1L)).thenReturn(false);

        mockMvc.perform(get("/api/v1/reviews/movie/1/has-reviews"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void getReviewCount_Success() throws Exception {
        when(reviewService.getReviewCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/v1/reviews/movie/1/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void submitReview_UserNameTooLong() throws Exception {
        String longUserName = "a".repeat(101);
        ReviewSubmissionDTO invalidReview = new ReviewSubmissionDTO(1L, longUserName,
                BigDecimal.valueOf(4.5), "Valid comment");

        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReview)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitReview_CommentTooLong() throws Exception {
        String longComment = "a".repeat(1001);
        ReviewSubmissionDTO invalidReview = new ReviewSubmissionDTO(1L, "John Doe",
                BigDecimal.valueOf(4.5), longComment);

        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReview)))
                .andExpect(status().isBadRequest());
    }
}