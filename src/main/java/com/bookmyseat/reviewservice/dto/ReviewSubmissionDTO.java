package com.bookmyseat.reviewservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Review submission request")
public class ReviewSubmissionDTO {

    @NotNull(message = "Movie ID is required")
    @Schema(description = "ID of the movie being reviewed", example = "1", required = true)
    private Long movieId;

    @NotBlank(message = "User name is required")
    @Size(max = 100, message = "User name must not exceed 100 characters")
    @Schema(description = "Name of the user submitting the review", example = "John Doe", required = true)
    private String userName;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    @Schema(description = "Rating given to the movie (1.0 to 5.0)", example = "4.5", required = true)
    private BigDecimal rating;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    @Schema(description = "Optional comment about the movie", example = "Great movie! Highly recommended.")
    private String comment;

    // Default constructor
    public ReviewSubmissionDTO() {}

    // Constructor
    public ReviewSubmissionDTO(Long movieId, String userName, BigDecimal rating, String comment) {
        this.movieId = movieId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
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

    @Override
    public String toString() {
        return "ReviewSubmissionDTO{" +
                "movieId=" + movieId +
                ", userName='" + userName + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                '}';
    }
}