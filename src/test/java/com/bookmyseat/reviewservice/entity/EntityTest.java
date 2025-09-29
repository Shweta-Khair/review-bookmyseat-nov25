package com.bookmyseat.reviewservice.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testReviewEntity() {
        Review review = new Review();

        review.setId(1L);
        review.setMovieId(100L);
        review.setUserName("John Doe");
        review.setRating(BigDecimal.valueOf(5));
        review.setComment("Excellent movie!");
        LocalDateTime now = LocalDateTime.now();
        review.setReviewDate(now);

        assertEquals(1L, review.getId());
        assertEquals(100L, review.getMovieId());
        assertEquals("John Doe", review.getUserName());
        assertEquals(BigDecimal.valueOf(5), review.getRating());
        assertEquals("Excellent movie!", review.getComment());
        assertEquals(now, review.getReviewDate());
    }

    @Test
    void testReviewEntityConstructor() {
        Review review = new Review(
            100L,
            "Jane Smith",
            BigDecimal.valueOf(4),
            "Good movie"
        );

        assertNotNull(review);
        assertEquals(100L, review.getMovieId());
        assertEquals("Jane Smith", review.getUserName());
        assertEquals(BigDecimal.valueOf(4), review.getRating());
        assertEquals("Good movie", review.getComment());
    }

    @Test
    void testReviewEntityNoArgsConstructor() {
        Review review = new Review();
        assertNotNull(review);
        assertNull(review.getId());
        assertNull(review.getMovieId());
        assertNull(review.getUserName());
        assertNull(review.getRating());
        assertNull(review.getComment());
        assertNull(review.getReviewDate());
    }

    @Test
    void testMovieRatingEntity() {
        MovieRating rating = new MovieRating();

        rating.setMovieId(100L);
        rating.setAverageRating(BigDecimal.valueOf(4.5));
        rating.setTotalReviews(50);
        rating.setRating1Count(2);
        rating.setRating2Count(3);
        rating.setRating3Count(5);
        rating.setRating4Count(15);
        rating.setRating5Count(25);

        assertEquals(100L, rating.getMovieId());
        assertEquals(BigDecimal.valueOf(4.5), rating.getAverageRating());
        assertEquals(50, rating.getTotalReviews());
        assertEquals(2, rating.getRating1Count());
        assertEquals(3, rating.getRating2Count());
        assertEquals(5, rating.getRating3Count());
        assertEquals(15, rating.getRating4Count());
        assertEquals(25, rating.getRating5Count());
    }

    @Test
    void testMovieRatingEntitySingleArgConstructor() {
        MovieRating rating = new MovieRating(100L);

        assertNotNull(rating);
        assertEquals(100L, rating.getMovieId());
        assertEquals(BigDecimal.ZERO, rating.getAverageRating());
        assertEquals(0, rating.getTotalReviews());
        assertEquals(0, rating.getRating1Count());
        assertEquals(0, rating.getRating2Count());
        assertEquals(0, rating.getRating3Count());
        assertEquals(0, rating.getRating4Count());
        assertEquals(0, rating.getRating5Count());
    }

    @Test
    void testMovieRatingEntityThreeArgConstructor() {
        MovieRating rating = new MovieRating(100L, BigDecimal.valueOf(4.0), 100);

        assertNotNull(rating);
        assertEquals(100L, rating.getMovieId());
        assertEquals(BigDecimal.valueOf(4.0), rating.getAverageRating());
        assertEquals(100, rating.getTotalReviews());
        assertEquals(0, rating.getRating1Count());
        assertEquals(0, rating.getRating2Count());
        assertEquals(0, rating.getRating3Count());
        assertEquals(0, rating.getRating4Count());
        assertEquals(0, rating.getRating5Count());
    }

    @Test
    void testMovieRatingEntityNoArgsConstructor() {
        MovieRating rating = new MovieRating();
        assertNotNull(rating);
        assertNull(rating.getMovieId());
        assertNull(rating.getAverageRating());
        assertEquals(0, rating.getTotalReviews());
        assertEquals(0, rating.getRating1Count());
        assertEquals(0, rating.getRating2Count());
        assertEquals(0, rating.getRating3Count());
        assertEquals(0, rating.getRating4Count());
        assertEquals(0, rating.getRating5Count());
    }

    @Test
    void testMovieRatingGetRatingCount() {
        MovieRating rating = new MovieRating(100L, BigDecimal.valueOf(4.0), 100);
        rating.setRating1Count(5);
        rating.setRating2Count(10);
        rating.setRating3Count(15);
        rating.setRating4Count(30);
        rating.setRating5Count(40);

        assertEquals(5, rating.getRatingCount(1));
        assertEquals(10, rating.getRatingCount(2));
        assertEquals(15, rating.getRatingCount(3));
        assertEquals(30, rating.getRatingCount(4));
        assertEquals(40, rating.getRatingCount(5));
        assertEquals(0, rating.getRatingCount(6)); // Invalid rating
    }

    @Test
    void testReviewToString() {
        Review review = new Review(100L, "John", BigDecimal.valueOf(5), "Great!");
        String toString = review.toString();
        assertNotNull(toString);
        assertTrue(toString.length() > 0);
    }

    @Test
    void testMovieRatingToString() {
        MovieRating rating = new MovieRating(100L, BigDecimal.valueOf(4.5), 50);
        String toString = rating.toString();
        assertNotNull(toString);
        assertTrue(toString.length() > 0);
    }
}