package com.bookmyseat.reviewservice.controller;

import com.bookmyseat.reviewservice.dto.*;
import com.bookmyseat.reviewservice.service.MovieRatingService;
import com.bookmyseat.reviewservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "Review management API")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;
    private final MovieRatingService movieRatingService;

    public ReviewController(ReviewService reviewService, MovieRatingService movieRatingService) {
        this.reviewService = reviewService;
        this.movieRatingService = movieRatingService;
    }

    @PostMapping
    @Operation(summary = "Submit a review", description = "Submit a new review for a movie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Movie not found"),
            @ApiResponse(responseCode = "503", description = "Movie service unavailable")
    })
    public ResponseEntity<ReviewDTO> submitReview(
            @Valid @RequestBody ReviewSubmissionDTO reviewSubmission) {

        logger.info("Received review submission for movie {} by user {}",
                   reviewSubmission.getMovieId(), reviewSubmission.getUserName());

        ReviewDTO createdReview = reviewService.submitReview(reviewSubmission);

        logger.info("Review {} created successfully for movie {}",
                   createdReview.getId(), createdReview.getMovieId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "Get reviews for a movie", description = "Retrieve paginated reviews for a specific movie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<MovieReviewsResponseDTO> getReviewsForMovie(
            @Parameter(description = "Movie ID", required = true)
            @PathVariable Long movieId,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field and direction")
            @RequestParam(defaultValue = "reviewDate,desc") String sort) {

        logger.debug("Fetching reviews for movie {} - page: {}, size: {}, sort: {}",
                    movieId, page, size, sort);

        // Parse sort parameter
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        MovieReviewsResponseDTO response = reviewService.getReviewsForMovie(movieId, pageable);

        logger.debug("Found {} reviews for movie {} on page {}",
                    response.getReviews().size(), movieId, page);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/movie/{movieId}/rating")
    @Operation(summary = "Get movie rating summary", description = "Get aggregated rating information for a movie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating summary retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<RatingSummaryDTO> getMovieRatingSummary(
            @Parameter(description = "Movie ID", required = true)
            @PathVariable Long movieId) {

        logger.debug("Fetching rating summary for movie: {}", movieId);

        RatingSummaryDTO ratingSummary = movieRatingService.getMovieRatingSummary(movieId);

        logger.debug("Rating summary for movie {}: avg={}, total={}",
                    movieId, ratingSummary.getAverageRating(), ratingSummary.getTotalReviews());

        return ResponseEntity.ok(ratingSummary);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID", description = "Retrieve a specific review by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ReviewDTO> getReviewById(
            @Parameter(description = "Review ID", required = true)
            @PathVariable Long reviewId) {

        logger.debug("Fetching review by ID: {}", reviewId);

        ReviewDTO review = reviewService.getReviewById(reviewId);

        return ResponseEntity.ok(review);
    }

    @GetMapping("/movie/{movieId}/has-reviews")
    @Operation(summary = "Check if movie has reviews", description = "Check if a specific movie has any reviews")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed successfully")
    })
    public ResponseEntity<Boolean> hasReviews(
            @Parameter(description = "Movie ID", required = true)
            @PathVariable Long movieId) {

        logger.debug("Checking if movie {} has reviews", movieId);

        boolean hasReviews = reviewService.hasReviews(movieId);

        return ResponseEntity.ok(hasReviews);
    }

    @GetMapping("/movie/{movieId}/count")
    @Operation(summary = "Get review count", description = "Get total count of reviews for a movie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    public ResponseEntity<Long> getReviewCount(
            @Parameter(description = "Movie ID", required = true)
            @PathVariable Long movieId) {

        logger.debug("Getting review count for movie: {}", movieId);

        long count = reviewService.getReviewCount(movieId);

        return ResponseEntity.ok(count);
    }
}