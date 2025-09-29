package com.bookmyseat.reviewservice.service;

import com.bookmyseat.reviewservice.dto.ReviewDTO;
import com.bookmyseat.reviewservice.dto.ReviewSubmissionDTO;
import com.bookmyseat.reviewservice.dto.MovieReviewsResponseDTO;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    /**
     * Submit a new review for a movie
     * @param reviewSubmission Review data to submit
     * @return Created review
     */
    ReviewDTO submitReview(ReviewSubmissionDTO reviewSubmission);

    /**
     * Get paginated reviews for a specific movie
     * @param movieId Movie identifier
     * @param pageable Pagination parameters
     * @return Paginated reviews with movie information
     */
    MovieReviewsResponseDTO getReviewsForMovie(Long movieId, Pageable pageable);

    /**
     * Get review by ID
     * @param reviewId Review identifier
     * @return Review details
     */
    ReviewDTO getReviewById(Long reviewId);

    /**
     * Check if a movie has any reviews
     * @param movieId Movie identifier
     * @return true if movie has reviews, false otherwise
     */
    boolean hasReviews(Long movieId);

    /**
     * Get total review count for a movie
     * @param movieId Movie identifier
     * @return Number of reviews
     */
    long getReviewCount(Long movieId);
}