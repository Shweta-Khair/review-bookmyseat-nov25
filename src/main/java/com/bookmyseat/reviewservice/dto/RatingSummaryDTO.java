package com.bookmyseat.reviewservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "Rating summary for a movie")
public class RatingSummaryDTO {

    @Schema(description = "ID of the movie", example = "1")
    private Long movieId;

    @Schema(description = "Title of the movie", example = "Inception")
    private String movieTitle;

    @Schema(description = "Average rating of the movie", example = "4.5")
    private BigDecimal averageRating;

    @Schema(description = "Total number of reviews", example = "150")
    private Integer totalReviews;

    @Schema(description = "Distribution of ratings by star count")
    private Map<String, Integer> ratingDistribution;

    // Default constructor
    public RatingSummaryDTO() {}

    // Constructor
    public RatingSummaryDTO(Long movieId, String movieTitle, BigDecimal averageRating,
                           Integer totalReviews, Map<String, Integer> ratingDistribution) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.ratingDistribution = ratingDistribution;
    }

    // Getters and Setters
    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Map<String, Integer> getRatingDistribution() {
        return ratingDistribution;
    }

    public void setRatingDistribution(Map<String, Integer> ratingDistribution) {
        this.ratingDistribution = ratingDistribution;
    }

    @Override
    public String toString() {
        return "RatingSummaryDTO{" +
                "movieId=" + movieId +
                ", movieTitle='" + movieTitle + '\'' +
                ", averageRating=" + averageRating +
                ", totalReviews=" + totalReviews +
                ", ratingDistribution=" + ratingDistribution +
                '}';
    }
}