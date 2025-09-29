package com.bookmyseat.reviewservice.service;

import com.bookmyseat.reviewservice.client.MovieServiceClient;
import com.bookmyseat.reviewservice.dto.MovieDetailDTO;
import com.bookmyseat.reviewservice.dto.RatingSummaryDTO;
import com.bookmyseat.reviewservice.entity.MovieRating;
import com.bookmyseat.reviewservice.entity.Review;
import com.bookmyseat.reviewservice.repository.MovieRatingRepository;
import com.bookmyseat.reviewservice.repository.ReviewRepository;
import com.bookmyseat.reviewservice.service.impl.MovieRatingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieRatingServiceImplTest {

    @Mock
    private MovieRatingRepository movieRatingRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MovieServiceClient movieServiceClient;

    @InjectMocks
    private MovieRatingServiceImpl movieRatingService;

    private MovieDetailDTO movieDetailDTO;
    private MovieRating movieRating;
    private Review review1, review2, review3;

    @BeforeEach
    void setUp() {
        movieDetailDTO = new MovieDetailDTO(1L, "Inception", "Mind-bending thriller",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));

        movieRating = new MovieRating(1L);
        movieRating.setAverageRating(BigDecimal.valueOf(4.5));
        movieRating.setTotalReviews(3);
        movieRating.setRating5Count(2);
        movieRating.setRating4Count(1);

        review1 = new Review(1L, "User1", BigDecimal.valueOf(5.0), "Excellent!");
        review1.setId(1L);
        review1.setReviewDate(LocalDateTime.now().minusDays(3));

        review2 = new Review(1L, "User2", BigDecimal.valueOf(4.0), "Good movie");
        review2.setId(2L);
        review2.setReviewDate(LocalDateTime.now().minusDays(2));

        review3 = new Review(1L, "User3", BigDecimal.valueOf(5.0), "Amazing!");
        review3.setId(3L);
        review3.setReviewDate(LocalDateTime.now().minusDays(1));
    }

    @Test
    void getMovieRatingSummary_WithCachedRating() {
        // Given
        Long movieId = 1L;
        when(movieServiceClient.getMovieById(movieId)).thenReturn(movieDetailDTO);
        when(movieRatingRepository.findByMovieId(movieId)).thenReturn(Optional.of(movieRating));

        // When
        RatingSummaryDTO result = movieRatingService.getMovieRatingSummary(movieId);

        // Then
        assertNotNull(result);
        assertEquals(movieId, result.getMovieId());
        assertEquals("Inception", result.getMovieTitle());
        assertEquals(BigDecimal.valueOf(4.5), result.getAverageRating());
        assertEquals(3, result.getTotalReviews());
        assertEquals(2, result.getRatingDistribution().get("5"));
        assertEquals(1, result.getRatingDistribution().get("4"));
        assertEquals(0, result.getRatingDistribution().get("3"));

        verify(movieRatingRepository, never()).save(any());
    }

    @Test
    void getMovieRatingSummary_WithoutCachedRating_WithReviews() {
        // Given
        Long movieId = 1L;
        List<Review> reviews = Arrays.asList(review1, review2, review3);

        when(movieServiceClient.getMovieById(movieId)).thenReturn(movieDetailDTO);
        when(movieRatingRepository.findByMovieId(movieId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(movieRating));
        when(reviewRepository.findByMovieIdOrderByReviewDateDesc(movieId)).thenReturn(reviews);

        // When
        RatingSummaryDTO result = movieRatingService.getMovieRatingSummary(movieId);

        // Then
        assertNotNull(result);
        assertEquals(movieId, result.getMovieId());
        assertEquals("Inception", result.getMovieTitle());

        verify(movieRatingRepository).save(any(MovieRating.class));
    }

    @Test
    void getMovieRatingSummary_NoReviews() {
        // Given
        Long movieId = 1L;
        when(movieServiceClient.getMovieById(movieId)).thenReturn(movieDetailDTO);
        when(movieRatingRepository.findByMovieId(movieId)).thenReturn(Optional.empty());
        when(reviewRepository.findByMovieIdOrderByReviewDateDesc(movieId)).thenReturn(Arrays.asList());

        // When
        RatingSummaryDTO result = movieRatingService.getMovieRatingSummary(movieId);

        // Then
        assertNotNull(result);
        assertEquals(movieId, result.getMovieId());
        assertEquals("Inception", result.getMovieTitle());
        assertEquals(BigDecimal.ZERO, result.getAverageRating());
        assertEquals(0, result.getTotalReviews());
        assertTrue(result.getRatingDistribution().isEmpty());

        verify(movieRatingRepository).deleteByMovieId(movieId);
    }

    @Test
    void updateMovieRating_ExistingRating() {
        // Given
        Review newReview = new Review(1L, "User4", BigDecimal.valueOf(3.0), "OK movie");
        List<Review> allReviews = Arrays.asList(review1, review2, review3, newReview);

        when(movieRatingRepository.findByMovieId(1L)).thenReturn(Optional.of(movieRating));
        when(reviewRepository.findByMovieIdOrderByReviewDateDesc(1L)).thenReturn(allReviews);

        // When
        movieRatingService.updateMovieRating(newReview);

        // Then
        verify(movieRatingRepository).save(any(MovieRating.class));
    }

    @Test
    void updateMovieRating_NewRating() {
        // Given
        Review newReview = new Review(1L, "User1", BigDecimal.valueOf(4.5), "Great!");

        when(movieRatingRepository.findByMovieId(1L)).thenReturn(Optional.empty());

        // When
        movieRatingService.updateMovieRating(newReview);

        // Then
        verify(movieRatingRepository).save(any(MovieRating.class));
    }

    @Test
    void recalculateMovieRating_WithReviews() {
        // Given
        Long movieId = 1L;
        List<Review> reviews = Arrays.asList(review1, review2, review3);
        MovieRating existingRating = new MovieRating(movieId);

        when(reviewRepository.findByMovieIdOrderByReviewDateDesc(movieId)).thenReturn(reviews);
        when(movieRatingRepository.findByMovieId(movieId)).thenReturn(Optional.of(existingRating));

        // When
        movieRatingService.recalculateMovieRating(movieId);

        // Then
        verify(movieRatingRepository).save(any(MovieRating.class));
    }

    @Test
    void recalculateMovieRating_NoReviews() {
        // Given
        Long movieId = 1L;
        when(reviewRepository.findByMovieIdOrderByReviewDateDesc(movieId)).thenReturn(Arrays.asList());

        // When
        movieRatingService.recalculateMovieRating(movieId);

        // Then
        verify(movieRatingRepository).deleteByMovieId(movieId);
        verify(movieRatingRepository, never()).save(any());
    }

    @Test
    void initializeMovieRating_NotExists() {
        // Given
        Long movieId = 1L;
        when(movieRatingRepository.existsByMovieId(movieId)).thenReturn(false);

        // When
        movieRatingService.initializeMovieRating(movieId);

        // Then
        verify(movieRatingRepository).save(any(MovieRating.class));
    }

    @Test
    void initializeMovieRating_AlreadyExists() {
        // Given
        Long movieId = 1L;
        when(movieRatingRepository.existsByMovieId(movieId)).thenReturn(true);

        // When
        movieRatingService.initializeMovieRating(movieId);

        // Then
        verify(movieRatingRepository, never()).save(any());
    }

    @Test
    void deleteMovieRating() {
        // Given
        Long movieId = 1L;

        // When
        movieRatingService.deleteMovieRating(movieId);

        // Then
        verify(movieRatingRepository).deleteByMovieId(movieId);
    }
}