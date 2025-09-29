package com.bookmyseat.reviewservice.service.impl;

import com.bookmyseat.reviewservice.client.MovieServiceClient;
import com.bookmyseat.reviewservice.dto.MovieDetailDTO;
import com.bookmyseat.reviewservice.dto.RatingSummaryDTO;
import com.bookmyseat.reviewservice.entity.MovieRating;
import com.bookmyseat.reviewservice.entity.Review;
import com.bookmyseat.reviewservice.repository.MovieRatingRepository;
import com.bookmyseat.reviewservice.repository.ReviewRepository;
import com.bookmyseat.reviewservice.service.MovieRatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class MovieRatingServiceImpl implements MovieRatingService {

    private static final Logger logger = LoggerFactory.getLogger(MovieRatingServiceImpl.class);

    private final MovieRatingRepository movieRatingRepository;
    private final ReviewRepository reviewRepository;
    private final MovieServiceClient movieServiceClient;

    public MovieRatingServiceImpl(MovieRatingRepository movieRatingRepository,
                                 ReviewRepository reviewRepository,
                                 MovieServiceClient movieServiceClient) {
        this.movieRatingRepository = movieRatingRepository;
        this.reviewRepository = reviewRepository;
        this.movieServiceClient = movieServiceClient;
    }

    @Override
    @Transactional(readOnly = true)
    public RatingSummaryDTO getMovieRatingSummary(Long movieId) {
        logger.debug("Getting rating summary for movie: {}", movieId);

        // Get movie details
        MovieDetailDTO movie;
        try {
            movie = movieServiceClient.getMovieById(movieId);
        } catch (Exception e) {
            logger.error("Failed to fetch movie details for rating summary: {}", e.getMessage());
            throw e;
        }

        // Try to get from cache first
        Optional<MovieRating> cachedRating = movieRatingRepository.findByMovieId(movieId);

        if (cachedRating.isPresent()) {
            MovieRating rating = cachedRating.get();
            Map<String, Integer> distribution = buildRatingDistribution(rating);

            logger.debug("Found cached rating for movie {}: avg={}, total={}",
                        movieId, rating.getAverageRating(), rating.getTotalReviews());

            return new RatingSummaryDTO(
                    movieId,
                    movie.getTitle(),
                    rating.getAverageRating(),
                    rating.getTotalReviews(),
                    distribution
            );
        }

        // Calculate rating on demand if not cached
        logger.debug("No cached rating found for movie {}, calculating from reviews", movieId);
        recalculateMovieRating(movieId);

        // Try again after calculation
        Optional<MovieRating> newRating = movieRatingRepository.findByMovieId(movieId);
        if (newRating.isPresent()) {
            MovieRating rating = newRating.get();
            Map<String, Integer> distribution = buildRatingDistribution(rating);

            return new RatingSummaryDTO(
                    movieId,
                    movie.getTitle(),
                    rating.getAverageRating(),
                    rating.getTotalReviews(),
                    distribution
            );
        }

        // No reviews exist, return empty summary
        logger.debug("No reviews found for movie {}", movieId);
        return new RatingSummaryDTO(
                movieId,
                movie.getTitle(),
                BigDecimal.ZERO,
                0,
                new HashMap<>()
        );
    }

    @Override
    public void updateMovieRating(Review review) {
        logger.debug("Updating movie rating cache for movie {} after new review",
                    review.getMovieId());

        Long movieId = review.getMovieId();
        Optional<MovieRating> existingRating = movieRatingRepository.findByMovieId(movieId);

        if (existingRating.isPresent()) {
            // Update existing rating
            updateExistingRating(existingRating.get(), review);
        } else {
            // Create new rating entry
            createNewRating(movieId, review);
        }
    }

    @Override
    public void recalculateMovieRating(Long movieId) {
        logger.debug("Recalculating movie rating for movie: {}", movieId);

        List<Review> reviews = reviewRepository.findByMovieIdOrderByReviewDateDesc(movieId);
        if (reviews.isEmpty()) {
            logger.debug("No reviews found for movie {}, removing rating cache", movieId);
            movieRatingRepository.deleteByMovieId(movieId);
            return;
        }

        // Calculate average rating
        BigDecimal sum = reviews.stream()
                .map(Review::getRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageRating = sum.divide(BigDecimal.valueOf(reviews.size()), 2, RoundingMode.HALF_UP);

        // Calculate rating distribution
        MovieRating movieRating = movieRatingRepository.findByMovieId(movieId)
                .orElse(new MovieRating(movieId));

        // Reset counts
        movieRating.setRating1Count(0);
        movieRating.setRating2Count(0);
        movieRating.setRating3Count(0);
        movieRating.setRating4Count(0);
        movieRating.setRating5Count(0);

        // Count each rating
        for (Review review : reviews) {
            int ratingValue = review.getRating().intValue();
            movieRating.incrementRatingCount(ratingValue);
        }

        movieRating.setAverageRating(averageRating);
        movieRating.setTotalReviews(reviews.size());

        movieRatingRepository.save(movieRating);

        logger.info("Updated rating for movie {}: avg={}, total={}",
                   movieId, averageRating, reviews.size());
    }

    @Override
    public void initializeMovieRating(Long movieId) {
        if (!movieRatingRepository.existsByMovieId(movieId)) {
            logger.debug("Initializing rating cache for movie: {}", movieId);
            MovieRating movieRating = new MovieRating(movieId);
            movieRatingRepository.save(movieRating);
        }
    }

    @Override
    public void deleteMovieRating(Long movieId) {
        logger.debug("Deleting rating cache for movie: {}", movieId);
        movieRatingRepository.deleteByMovieId(movieId);
    }

    private void updateExistingRating(MovieRating movieRating, Review newReview) {
        // Get all reviews to recalculate (this ensures accuracy)
        List<Review> allReviews = reviewRepository.findByMovieIdOrderByReviewDateDesc(movieRating.getMovieId());

        // Calculate new average
        BigDecimal sum = allReviews.stream()
                .map(Review::getRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageRating = sum.divide(BigDecimal.valueOf(allReviews.size()), 2, RoundingMode.HALF_UP);

        // Reset and recalculate distribution
        movieRating.setRating1Count(0);
        movieRating.setRating2Count(0);
        movieRating.setRating3Count(0);
        movieRating.setRating4Count(0);
        movieRating.setRating5Count(0);

        for (Review review : allReviews) {
            int ratingValue = review.getRating().intValue();
            movieRating.incrementRatingCount(ratingValue);
        }

        movieRating.setAverageRating(averageRating);
        movieRating.setTotalReviews(allReviews.size());

        movieRatingRepository.save(movieRating);

        logger.debug("Updated existing rating for movie {}: avg={}, total={}",
                    movieRating.getMovieId(), averageRating, allReviews.size());
    }

    private void createNewRating(Long movieId, Review firstReview) {
        MovieRating movieRating = new MovieRating(movieId);
        movieRating.setAverageRating(firstReview.getRating());
        movieRating.setTotalReviews(1);

        int ratingValue = firstReview.getRating().intValue();
        movieRating.incrementRatingCount(ratingValue);

        movieRatingRepository.save(movieRating);

        logger.debug("Created new rating for movie {}: avg={}, total=1",
                    movieId, firstReview.getRating());
    }

    private Map<String, Integer> buildRatingDistribution(MovieRating movieRating) {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("1", movieRating.getRating1Count());
        distribution.put("2", movieRating.getRating2Count());
        distribution.put("3", movieRating.getRating3Count());
        distribution.put("4", movieRating.getRating4Count());
        distribution.put("5", movieRating.getRating5Count());
        return distribution;
    }
}