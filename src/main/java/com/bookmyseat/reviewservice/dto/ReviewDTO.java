package com.bookmyseat.reviewservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Review information")
public class ReviewDTO {

    @Schema(description = "Review ID", example = "1")
    private Long id;

    @Schema(description = "ID of the movie being reviewed", example = "1")
    private Long movieId;

    @Schema(description = "Title of the movie", example = "Inception")
    private String movieTitle;

    @Schema(description = "Name of the user who submitted the review", example = "John Doe")
    private String userName;

    @Schema(description = "Rating given to the movie", example = "4.5")
    private BigDecimal rating;

    @Schema(description = "Comment about the movie", example = "Great movie! Highly recommended.")
    private String comment;

    @Schema(description = "Date and time when the review was submitted", example = "2025-09-29T10:30:00")
    private LocalDateTime reviewDate;

    // Default constructor
    public ReviewDTO() {}

    // Constructor
    public ReviewDTO(Long id, Long movieId, String movieTitle, String userName,
                    BigDecimal rating, String comment, LocalDateTime reviewDate) {
        this.id = id;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    @Override
    public String toString() {
        return "ReviewDTO{" +
                "id=" + id +
                ", movieId=" + movieId +
                ", movieTitle='" + movieTitle + '\'' +
                ", userName='" + userName + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", reviewDate=" + reviewDate +
                '}';
    }
}