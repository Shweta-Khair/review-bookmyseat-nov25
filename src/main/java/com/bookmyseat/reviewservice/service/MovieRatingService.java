package com.bookmyseat.reviewservice.service;

import com.bookmyseat.reviewservice.dto.RatingSummaryDTO;
import com.bookmyseat.reviewservice.entity.Review;

public interface MovieRatingService {

    /**
     * Get rating summary for a movie
     * @param movieId Movie identifier
     * @return Rating summary with distribution
     */
    RatingSummaryDTO getMovieRatingSummary(Long movieId);

    /**
     * Update movie rating cache when a new review is added
     * @param review New review to include in calculations
     */
    void updateMovieRating(Review review);

    /**
     * Recalculate and update movie rating from all reviews
     * @param movieId Movie identifier
     */
    void recalculateMovieRating(Long movieId);

    /**
     * Initialize rating cache for a movie if it doesn't exist
     * @param movieId Movie identifier
     */
    void initializeMovieRating(Long movieId);

    /**
     * Delete rating cache for a movie
     * @param movieId Movie identifier
     */
    void deleteMovieRating(Long movieId);
}