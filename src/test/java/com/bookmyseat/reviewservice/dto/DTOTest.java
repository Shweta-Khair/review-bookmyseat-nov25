package com.bookmyseat.reviewservice.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    @Test
    void testErrorResponseDTO() {
        ErrorResponseDTO dto = new ErrorResponseDTO();

        dto.setMessage("Error message");
        dto.setErrorCode("ERR001");
        dto.setStatus(404);
        dto.setTimestamp(LocalDateTime.now());
        dto.setPath("/test/path");

        assertEquals("Error message", dto.getMessage());
        assertEquals("ERR001", dto.getErrorCode());
        assertEquals(404, dto.getStatus());
        assertNotNull(dto.getTimestamp());
        assertEquals("/test/path", dto.getPath());
    }

    @Test
    void testMovieDetailDTO() {
        LocalDate releaseDate = LocalDate.of(2010, 7, 16);
        MovieDetailDTO dto = new MovieDetailDTO(
            1L, "Inception", "Mind-bending thriller",
            148, "Sci-Fi", "English", releaseDate
        );

        assertEquals(1L, dto.getId());
        assertEquals("Inception", dto.getTitle());
        assertEquals("Mind-bending thriller", dto.getDescription());
        assertEquals(148, dto.getDurationMinutes());
        assertEquals("Sci-Fi", dto.getGenre());
        assertEquals("English", dto.getLanguage());
        assertEquals(releaseDate, dto.getReleaseDate());

        dto.setId(2L);
        dto.setTitle("Updated Title");
        dto.setDescription("Updated Description");
        dto.setDurationMinutes(120);
        dto.setGenre("Action");
        dto.setLanguage("Spanish");
        dto.setReleaseDate(LocalDate.of(2020, 1, 1));

        assertEquals(2L, dto.getId());
        assertEquals("Updated Title", dto.getTitle());
        assertEquals("Updated Description", dto.getDescription());
        assertEquals(120, dto.getDurationMinutes());
        assertEquals("Action", dto.getGenre());
        assertEquals("Spanish", dto.getLanguage());
        assertEquals(LocalDate.of(2020, 1, 1), dto.getReleaseDate());
    }

    @Test
    void testReviewDTO() {
        LocalDateTime reviewDate = LocalDateTime.now();
        ReviewDTO dto = new ReviewDTO();

        dto.setId(1L);
        dto.setMovieId(1L);
        dto.setMovieTitle("Inception");
        dto.setUserName("John Doe");
        dto.setRating(BigDecimal.valueOf(5));
        dto.setComment("Great movie!");
        dto.setReviewDate(reviewDate);

        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getMovieId());
        assertEquals("Inception", dto.getMovieTitle());
        assertEquals("John Doe", dto.getUserName());
        assertEquals(BigDecimal.valueOf(5), dto.getRating());
        assertEquals("Great movie!", dto.getComment());
        assertEquals(reviewDate, dto.getReviewDate());
    }

    @Test
    void testReviewSubmissionDTO() {
        ReviewSubmissionDTO dto = new ReviewSubmissionDTO();

        dto.setMovieId(1L);
        dto.setUserName("John Doe");
        dto.setRating(BigDecimal.valueOf(5));
        dto.setComment("Excellent!");

        assertEquals(1L, dto.getMovieId());
        assertEquals("John Doe", dto.getUserName());
        assertEquals(BigDecimal.valueOf(5), dto.getRating());
        assertEquals("Excellent!", dto.getComment());
    }

    @Test
    void testRatingSummaryDTO() {
        Map<String, Integer> ratingDistribution = new HashMap<>();
        ratingDistribution.put("5", 10);
        ratingDistribution.put("4", 5);

        RatingSummaryDTO dto = new RatingSummaryDTO(
            1L, "Inception", BigDecimal.valueOf(4.5),
            15, ratingDistribution
        );

        assertEquals(1L, dto.getMovieId());
        assertEquals("Inception", dto.getMovieTitle());
        assertEquals(BigDecimal.valueOf(4.5), dto.getAverageRating());
        assertEquals(15, dto.getTotalReviews());
        assertEquals(ratingDistribution, dto.getRatingDistribution());

        dto.setMovieId(2L);
        dto.setMovieTitle("Matrix");
        dto.setAverageRating(BigDecimal.valueOf(4.0));
        dto.setTotalReviews(20);
        dto.setRatingDistribution(new HashMap<>());

        assertEquals(2L, dto.getMovieId());
        assertEquals("Matrix", dto.getMovieTitle());
        assertEquals(BigDecimal.valueOf(4.0), dto.getAverageRating());
        assertEquals(20, dto.getTotalReviews());
        assertNotNull(dto.getRatingDistribution());
    }

    @Test
    void testMovieReviewsResponseDTO() {
        ReviewDTO review1 = new ReviewDTO();
        review1.setId(1L);
        review1.setMovieTitle("Inception");

        List<ReviewDTO> reviews = Arrays.asList(review1);

        MovieReviewsResponseDTO dto = new MovieReviewsResponseDTO();
        dto.setReviews(reviews);
        dto.setAverageRating(BigDecimal.valueOf(4.5));
        dto.setTotalReviews(100L);
        dto.setPage(0);
        dto.setTotalPages(1);
        dto.setSize(10);
        dto.setFirst(true);
        dto.setLast(false);

        assertEquals(1, dto.getReviews().size());
        assertEquals(BigDecimal.valueOf(4.5), dto.getAverageRating());
        assertEquals(100L, dto.getTotalReviews());
        assertEquals(0, dto.getPage());
        assertEquals(1, dto.getTotalPages());
        assertEquals(10, dto.getSize());
        assertTrue(dto.getFirst());
        assertFalse(dto.getLast());
    }

    @Test
    void testDTONotNull() {
        MovieDetailDTO dto1 = new MovieDetailDTO(
            1L, "Inception", "Description",
            148, "Sci-Fi", "English", LocalDate.of(2010, 7, 16)
        );
        assertNotNull(dto1);
        assertNotNull(dto1.toString());
    }
}