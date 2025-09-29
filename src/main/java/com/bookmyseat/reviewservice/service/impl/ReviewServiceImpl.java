package com.bookmyseat.reviewservice.service.impl;

import com.bookmyseat.reviewservice.client.MovieServiceClient;
import com.bookmyseat.reviewservice.dto.*;
import com.bookmyseat.reviewservice.entity.Review;
import com.bookmyseat.reviewservice.exception.MovieNotFoundException;
import com.bookmyseat.reviewservice.exception.ReviewNotFoundException;
import com.bookmyseat.reviewservice.mapper.ReviewMapper;
import com.bookmyseat.reviewservice.repository.ReviewRepository;
import com.bookmyseat.reviewservice.service.MovieRatingService;
import com.bookmyseat.reviewservice.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final MovieServiceClient movieServiceClient;
    private final MovieRatingService movieRatingService;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                           ReviewMapper reviewMapper,
                           MovieServiceClient movieServiceClient,
                           MovieRatingService movieRatingService) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.movieServiceClient = movieServiceClient;
        this.movieRatingService = movieRatingService;
    }

    @Override
    public ReviewDTO submitReview(ReviewSubmissionDTO reviewSubmission) {
        logger.debug("Submitting review for movie {} by user {}",
                    reviewSubmission.getMovieId(), reviewSubmission.getUserName());

        // Validate movie exists via Movie Service
        MovieDetailDTO movie;
        try {
            movie = movieServiceClient.getMovieById(reviewSubmission.getMovieId());
        } catch (MovieNotFoundException e) {
            logger.warn("Attempted to review non-existent movie: {}", reviewSubmission.getMovieId());
            throw e;
        }

        // Convert DTO to entity and save
        Review review = reviewMapper.toReview(reviewSubmission);
        Review savedReview = reviewRepository.save(review);

        logger.info("Review {} submitted successfully for movie {} by user {}",
                   savedReview.getId(), savedReview.getMovieId(), savedReview.getUserName());

        // Update movie rating cache
        try {
            movieRatingService.updateMovieRating(savedReview);
        } catch (Exception e) {
            logger.error("Failed to update movie rating cache for movie {}: {}",
                        savedReview.getMovieId(), e.getMessage(), e);
            // Don't fail the review submission if rating cache update fails
        }

        // Convert to DTO with movie title
        return reviewMapper.toReviewDTO(savedReview, movie.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public MovieReviewsResponseDTO getReviewsForMovie(Long movieId, Pageable pageable) {
        logger.debug("Fetching reviews for movie {} with pagination: {}", movieId, pageable);

        // Validate movie exists
        MovieDetailDTO movie;
        try {
            movie = movieServiceClient.getMovieById(movieId);
        } catch (MovieNotFoundException e) {
            logger.warn("Attempted to get reviews for non-existent movie: {}", movieId);
            throw e;
        }

        // Get paginated reviews
        Page<Review> reviewPage = reviewRepository.findByMovieIdOrderByReviewDateDesc(movieId, pageable);
        List<ReviewDTO> reviewDTOs = reviewMapper.toReviewDTOList(reviewPage.getContent(), movie.getTitle());

        // Get or calculate average rating
        BigDecimal averageRating = reviewRepository.calculateAverageRating(movieId);
        if (averageRating == null) {
            averageRating = BigDecimal.ZERO;
        }

        logger.debug("Found {} reviews for movie {} with average rating {}",
                    reviewPage.getTotalElements(), movieId, averageRating);

        return new MovieReviewsResponseDTO(
                reviewDTOs,
                averageRating,
                reviewPage.getTotalElements(),
                reviewPage.getNumber(),
                reviewPage.getTotalPages(),
                reviewPage.getSize(),
                reviewPage.isFirst(),
                reviewPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDTO getReviewById(Long reviewId) {
        logger.debug("Fetching review by ID: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    logger.warn("Review not found with ID: {}", reviewId);
                    return new ReviewNotFoundException("Review not found with ID: " + reviewId);
                });

        // Get movie title for the review
        String movieTitle;
        try {
            MovieDetailDTO movie = movieServiceClient.getMovieById(review.getMovieId());
            movieTitle = movie.getTitle();
        } catch (Exception e) {
            logger.warn("Failed to fetch movie title for review {}: {}", reviewId, e.getMessage());
            movieTitle = "Unknown Movie";
        }

        return reviewMapper.toReviewDTO(review, movieTitle);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasReviews(Long movieId) {
        boolean hasReviews = reviewRepository.existsByMovieId(movieId);
        logger.debug("Movie {} has reviews: {}", movieId, hasReviews);
        return hasReviews;
    }

    @Override
    @Transactional(readOnly = true)
    public long getReviewCount(Long movieId) {
        long count = reviewRepository.countByMovieId(movieId);
        logger.debug("Movie {} has {} reviews", movieId, count);
        return count;
    }
}