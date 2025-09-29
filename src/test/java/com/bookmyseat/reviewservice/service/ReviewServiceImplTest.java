package com.bookmyseat.reviewservice.service;

import com.bookmyseat.reviewservice.client.MovieServiceClient;
import com.bookmyseat.reviewservice.dto.*;
import com.bookmyseat.reviewservice.entity.Review;
import com.bookmyseat.reviewservice.exception.MovieNotFoundException;
import com.bookmyseat.reviewservice.exception.ReviewNotFoundException;
import com.bookmyseat.reviewservice.mapper.ReviewMapper;
import com.bookmyseat.reviewservice.repository.ReviewRepository;
import com.bookmyseat.reviewservice.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private MovieServiceClient movieServiceClient;

    @Mock
    private MovieRatingService movieRatingService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private ReviewSubmissionDTO reviewSubmissionDTO;
    private Review review;
    private ReviewDTO reviewDTO;
    private MovieDetailDTO movieDetailDTO;

    @BeforeEach
    void setUp() {
        reviewSubmissionDTO = new ReviewSubmissionDTO(1L, "John Doe",
                BigDecimal.valueOf(4.5), "Great movie!");

        review = new Review(1L, "John Doe", BigDecimal.valueOf(4.5), "Great movie!");
        review.setId(1L);
        review.setReviewDate(LocalDateTime.now());

        reviewDTO = new ReviewDTO(1L, 1L, "Inception", "John Doe",
                BigDecimal.valueOf(4.5), "Great movie!", LocalDateTime.now());

        movieDetailDTO = new MovieDetailDTO(1L, "Inception", "Mind-bending thriller",
                148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16));
    }

    @Test
    void submitReview_Success() {
        // Given
        when(movieServiceClient.getMovieById(1L)).thenReturn(movieDetailDTO);
        when(reviewMapper.toReview(reviewSubmissionDTO)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewMapper.toReviewDTO(review, "Inception")).thenReturn(reviewDTO);

        // When
        ReviewDTO result = reviewService.submitReview(reviewSubmissionDTO);

        // Then
        assertNotNull(result);
        assertEquals(reviewDTO.getId(), result.getId());
        assertEquals(reviewDTO.getMovieTitle(), result.getMovieTitle());

        verify(movieServiceClient).getMovieById(1L);
        verify(reviewRepository).save(review);
        verify(movieRatingService).updateMovieRating(review);
    }

    @Test
    void submitReview_MovieNotFound() {
        // Given
        when(movieServiceClient.getMovieById(1L))
                .thenThrow(new MovieNotFoundException("Movie not found with ID: 1"));

        // When & Then
        MovieNotFoundException exception = assertThrows(MovieNotFoundException.class,
                () -> reviewService.submitReview(reviewSubmissionDTO));

        assertEquals("Movie not found with ID: 1", exception.getMessage());
        verify(reviewRepository, never()).save(any());
        verify(movieRatingService, never()).updateMovieRating(any());
    }

    @Test
    void getReviewsForMovie_Success() {
        // Given
        Long movieId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Review> reviews = Arrays.asList(review);
        Page<Review> reviewPage = new PageImpl<>(reviews, pageable, 1);
        List<ReviewDTO> reviewDTOs = Arrays.asList(reviewDTO);

        when(movieServiceClient.getMovieById(movieId)).thenReturn(movieDetailDTO);
        when(reviewRepository.findByMovieIdOrderByReviewDateDesc(movieId, pageable))
                .thenReturn(reviewPage);
        when(reviewMapper.toReviewDTOList(reviews, "Inception")).thenReturn(reviewDTOs);
        when(reviewRepository.calculateAverageRating(movieId)).thenReturn(BigDecimal.valueOf(4.5));

        // When
        MovieReviewsResponseDTO result = reviewService.getReviewsForMovie(movieId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getReviews().size());
        assertEquals(BigDecimal.valueOf(4.5), result.getAverageRating());
        assertEquals(1L, result.getTotalReviews());
        assertEquals(0, result.getPage());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.getFirst());
        assertTrue(result.getLast());
    }

    @Test
    void getReviewsForMovie_MovieNotFound() {
        // Given
        Long movieId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        when(movieServiceClient.getMovieById(movieId))
                .thenThrow(new MovieNotFoundException("Movie not found with ID: 999"));

        // When & Then
        MovieNotFoundException exception = assertThrows(MovieNotFoundException.class,
                () -> reviewService.getReviewsForMovie(movieId, pageable));

        assertEquals("Movie not found with ID: 999", exception.getMessage());
        verify(reviewRepository, never()).findByMovieIdOrderByReviewDateDesc(any(), any());
    }

    @Test
    void getReviewById_Success() {
        // Given
        Long reviewId = 1L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(movieServiceClient.getMovieById(1L)).thenReturn(movieDetailDTO);
        when(reviewMapper.toReviewDTO(review, "Inception")).thenReturn(reviewDTO);

        // When
        ReviewDTO result = reviewService.getReviewById(reviewId);

        // Then
        assertNotNull(result);
        assertEquals(reviewDTO.getId(), result.getId());
        assertEquals(reviewDTO.getMovieTitle(), result.getMovieTitle());
    }

    @Test
    void getReviewById_ReviewNotFound() {
        // Given
        Long reviewId = 999L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // When & Then
        ReviewNotFoundException exception = assertThrows(ReviewNotFoundException.class,
                () -> reviewService.getReviewById(reviewId));

        assertEquals("Review not found with ID: 999", exception.getMessage());
    }

    @Test
    void getReviewById_MovieServiceFails() {
        // Given
        Long reviewId = 1L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(movieServiceClient.getMovieById(1L)).thenThrow(new RuntimeException("Service error"));
        when(reviewMapper.toReviewDTO(review, "Unknown Movie")).thenReturn(reviewDTO);

        // When
        ReviewDTO result = reviewService.getReviewById(reviewId);

        // Then
        assertNotNull(result);
        verify(reviewMapper).toReviewDTO(review, "Unknown Movie");
    }

    @Test
    void hasReviews_True() {
        // Given
        Long movieId = 1L;
        when(reviewRepository.existsByMovieId(movieId)).thenReturn(true);

        // When
        boolean result = reviewService.hasReviews(movieId);

        // Then
        assertTrue(result);
    }

    @Test
    void hasReviews_False() {
        // Given
        Long movieId = 1L;
        when(reviewRepository.existsByMovieId(movieId)).thenReturn(false);

        // When
        boolean result = reviewService.hasReviews(movieId);

        // Then
        assertFalse(result);
    }

    @Test
    void getReviewCount() {
        // Given
        Long movieId = 1L;
        when(reviewRepository.countByMovieId(movieId)).thenReturn(5L);

        // When
        long result = reviewService.getReviewCount(movieId);

        // Then
        assertEquals(5L, result);
    }

    @Test
    void getReviewsForMovie_NoAverageRating() {
        // Given
        Long movieId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Review> reviews = Arrays.asList(review);
        Page<Review> reviewPage = new PageImpl<>(reviews, pageable, 1);
        List<ReviewDTO> reviewDTOs = Arrays.asList(reviewDTO);

        when(movieServiceClient.getMovieById(movieId)).thenReturn(movieDetailDTO);
        when(reviewRepository.findByMovieIdOrderByReviewDateDesc(movieId, pageable))
                .thenReturn(reviewPage);
        when(reviewMapper.toReviewDTOList(reviews, "Inception")).thenReturn(reviewDTOs);
        when(reviewRepository.calculateAverageRating(movieId)).thenReturn(null);

        // When
        MovieReviewsResponseDTO result = reviewService.getReviewsForMovie(movieId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getAverageRating());
    }

    @Test
    void submitReview_RatingServiceFails() {
        // Given
        when(movieServiceClient.getMovieById(1L)).thenReturn(movieDetailDTO);
        when(reviewMapper.toReview(reviewSubmissionDTO)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewMapper.toReviewDTO(review, "Inception")).thenReturn(reviewDTO);
        doThrow(new RuntimeException("Rating service error"))
                .when(movieRatingService).updateMovieRating(review);

        // When
        ReviewDTO result = reviewService.submitReview(reviewSubmissionDTO);

        // Then
        assertNotNull(result);
        // Should not fail even if rating service fails
        verify(movieRatingService).updateMovieRating(review);
    }
}