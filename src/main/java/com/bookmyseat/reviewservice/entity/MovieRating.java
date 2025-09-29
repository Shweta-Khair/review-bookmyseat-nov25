package com.bookmyseat.reviewservice.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movie_ratings")
public class MovieRating {

    @Id
    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "rating_1_count")
    private Integer rating1Count = 0;

    @Column(name = "rating_2_count")
    private Integer rating2Count = 0;

    @Column(name = "rating_3_count")
    private Integer rating3Count = 0;

    @Column(name = "rating_4_count")
    private Integer rating4Count = 0;

    @Column(name = "rating_5_count")
    private Integer rating5Count = 0;

    @Column(name = "last_updated")
    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    // Default constructor
    public MovieRating() {}

    // Constructor for creating new movie rating
    public MovieRating(Long movieId) {
        this.movieId = movieId;
        this.averageRating = BigDecimal.ZERO;
        this.totalReviews = 0;
        this.rating1Count = 0;
        this.rating2Count = 0;
        this.rating3Count = 0;
        this.rating4Count = 0;
        this.rating5Count = 0;
    }

    // Constructor with initial values
    public MovieRating(Long movieId, BigDecimal averageRating, Integer totalReviews) {
        this.movieId = movieId;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.rating1Count = 0;
        this.rating2Count = 0;
        this.rating3Count = 0;
        this.rating4Count = 0;
        this.rating5Count = 0;
    }

    // Utility method to get rating count by value
    public Integer getRatingCount(int rating) {
        return switch (rating) {
            case 1 -> rating1Count;
            case 2 -> rating2Count;
            case 3 -> rating3Count;
            case 4 -> rating4Count;
            case 5 -> rating5Count;
            default -> 0;
        };
    }

    // Utility method to increment rating count
    public void incrementRatingCount(int rating) {
        switch (rating) {
            case 1 -> rating1Count++;
            case 2 -> rating2Count++;
            case 3 -> rating3Count++;
            case 4 -> rating4Count++;
            case 5 -> rating5Count++;
        }
    }

    // Getters and Setters
    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
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

    public Integer getRating1Count() {
        return rating1Count;
    }

    public void setRating1Count(Integer rating1Count) {
        this.rating1Count = rating1Count;
    }

    public Integer getRating2Count() {
        return rating2Count;
    }

    public void setRating2Count(Integer rating2Count) {
        this.rating2Count = rating2Count;
    }

    public Integer getRating3Count() {
        return rating3Count;
    }

    public void setRating3Count(Integer rating3Count) {
        this.rating3Count = rating3Count;
    }

    public Integer getRating4Count() {
        return rating4Count;
    }

    public void setRating4Count(Integer rating4Count) {
        this.rating4Count = rating4Count;
    }

    public Integer getRating5Count() {
        return rating5Count;
    }

    public void setRating5Count(Integer rating5Count) {
        this.rating5Count = rating5Count;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "MovieRating{" +
                "movieId=" + movieId +
                ", averageRating=" + averageRating +
                ", totalReviews=" + totalReviews +
                ", rating1Count=" + rating1Count +
                ", rating2Count=" + rating2Count +
                ", rating3Count=" + rating3Count +
                ", rating4Count=" + rating4Count +
                ", rating5Count=" + rating5Count +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}