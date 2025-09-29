package com.bookmyseat.reviewservice.mapper;

import com.bookmyseat.reviewservice.dto.ReviewDTO;
import com.bookmyseat.reviewservice.dto.ReviewSubmissionDTO;
import com.bookmyseat.reviewservice.entity.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReviewMapperTest {

    private ReviewMapper reviewMapper;

    @BeforeEach
    void setUp() {
        reviewMapper = new ReviewMapper();
    }

    @Test
    void toReviewDTO_WithValidReview_ReturnsDTO() {
        // Given
        Review review = new Review(1L, "John Doe", BigDecimal.valueOf(4.5), "Great movie!");
        review.setId(100L);

        // When
        ReviewDTO result = reviewMapper.toReviewDTO(review);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getMovieId());
        assertNull(result.getMovieTitle()); // Title not set in basic mapping
        assertEquals("John Doe", result.getUserName());
        assertEquals(BigDecimal.valueOf(4.5), result.getRating());
        assertEquals("Great movie!", result.getComment());
        // ReviewDate is set by JPA @CreationTimestamp, so it might be null in unit tests
    }

    @Test
    void toReviewDTO_WithNull_ReturnsNull() {
        // When
        ReviewDTO result = reviewMapper.toReviewDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void toReviewDTO_WithMovieTitle_SetsTitle() {
        // Given
        Review review = new Review(1L, "John Doe", BigDecimal.valueOf(4.5), "Great movie!");
        review.setId(100L);
        String movieTitle = "Inception";

        // When
        ReviewDTO result = reviewMapper.toReviewDTO(review, movieTitle);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getMovieId());
        assertEquals("Inception", result.getMovieTitle());
        assertEquals("John Doe", result.getUserName());
        assertEquals(BigDecimal.valueOf(4.5), result.getRating());
        assertEquals("Great movie!", result.getComment());
    }

    @Test
    void toReviewDTO_WithMovieTitleAndNullReview_ReturnsNull() {
        // When
        ReviewDTO result = reviewMapper.toReviewDTO(null, "Inception");

        // Then
        assertNull(result);
    }

    @Test
    void toReview_WithValidSubmissionDTO_ReturnsEntity() {
        // Given
        ReviewSubmissionDTO submissionDTO = new ReviewSubmissionDTO(
                1L, "Jane Smith", BigDecimal.valueOf(5.0), "Excellent!");

        // When
        Review result = reviewMapper.toReview(submissionDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getMovieId());
        assertEquals("Jane Smith", result.getUserName());
        assertEquals(BigDecimal.valueOf(5.0), result.getRating());
        assertEquals("Excellent!", result.getComment());
        assertNull(result.getId()); // ID not set by mapper
    }

    @Test
    void toReview_WithNull_ReturnsNull() {
        // When
        Review result = reviewMapper.toReview(null);

        // Then
        assertNull(result);
    }

    @Test
    void toReviewDTOList_WithValidList_ReturnsDTOList() {
        // Given
        Review review1 = new Review(1L, "User1", BigDecimal.valueOf(4.0), "Good");
        review1.setId(100L);
        Review review2 = new Review(2L, "User2", BigDecimal.valueOf(5.0), "Excellent");
        review2.setId(101L);
        List<Review> reviews = Arrays.asList(review1, review2);

        // When
        List<ReviewDTO> result = reviewMapper.toReviewDTOList(reviews);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals(1L, result.get(0).getMovieId());
        assertEquals(101L, result.get(1).getId());
        assertEquals(2L, result.get(1).getMovieId());
    }

    @Test
    void toReviewDTOList_WithNull_ReturnsNull() {
        // When
        List<ReviewDTO> result = reviewMapper.toReviewDTOList(null);

        // Then
        assertNull(result);
    }

    @Test
    void toReviewDTOList_WithMovieTitle_SetsTitle() {
        // Given
        Review review1 = new Review(1L, "User1", BigDecimal.valueOf(4.0), "Good");
        review1.setId(100L);
        Review review2 = new Review(1L, "User2", BigDecimal.valueOf(5.0), "Excellent");
        review2.setId(101L);
        List<Review> reviews = Arrays.asList(review1, review2);
        String movieTitle = "The Matrix";

        // When
        List<ReviewDTO> result = reviewMapper.toReviewDTOList(reviews, movieTitle);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("The Matrix", result.get(0).getMovieTitle());
        assertEquals("The Matrix", result.get(1).getMovieTitle());
    }

    @Test
    void toReviewDTOList_WithMovieTitleAndNull_ReturnsNull() {
        // When
        List<ReviewDTO> result = reviewMapper.toReviewDTOList(null, "The Matrix");

        // Then
        assertNull(result);
    }

    @Test
    void updateReview_WithValidData_UpdatesEntity() {
        // Given
        Review review = new Review(1L, "Old User", BigDecimal.valueOf(3.0), "Old comment");
        review.setId(100L);
        ReviewSubmissionDTO submissionDTO = new ReviewSubmissionDTO(
                2L, "New User", BigDecimal.valueOf(5.0), "New comment");

        // When
        reviewMapper.updateReview(review, submissionDTO);

        // Then
        assertEquals(100L, review.getId()); // ID should not change
        assertEquals(2L, review.getMovieId());
        assertEquals("New User", review.getUserName());
        assertEquals(BigDecimal.valueOf(5.0), review.getRating());
        assertEquals("New comment", review.getComment());
    }

    @Test
    void updateReview_WithNullReview_DoesNothing() {
        // Given
        ReviewSubmissionDTO submissionDTO = new ReviewSubmissionDTO(
                2L, "New User", BigDecimal.valueOf(5.0), "New comment");

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> reviewMapper.updateReview(null, submissionDTO));
    }

    @Test
    void updateReview_WithNullSubmissionDTO_DoesNothing() {
        // Given
        Review review = new Review(1L, "User", BigDecimal.valueOf(3.0), "Comment");
        review.setId(100L);

        // When
        reviewMapper.updateReview(review, null);

        // Then - review should remain unchanged
        assertEquals(100L, review.getId());
        assertEquals(1L, review.getMovieId());
        assertEquals("User", review.getUserName());
        assertEquals(BigDecimal.valueOf(3.0), review.getRating());
        assertEquals("Comment", review.getComment());
    }

    @Test
    void updateReview_WithBothNull_DoesNothing() {
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> reviewMapper.updateReview(null, null));
    }
}