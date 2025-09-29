package com.bookmyseat.reviewservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Paginated response for movie reviews")
public class MovieReviewsResponseDTO {

    @Schema(description = "List of reviews for the movie")
    private List<ReviewDTO> reviews;

    @Schema(description = "Average rating of the movie", example = "4.5")
    private BigDecimal averageRating;

    @Schema(description = "Total number of reviews", example = "150")
    private Long totalReviews;

    @Schema(description = "Current page number", example = "0")
    private Integer page;

    @Schema(description = "Total number of pages", example = "15")
    private Integer totalPages;

    @Schema(description = "Number of reviews per page", example = "10")
    private Integer size;

    @Schema(description = "Whether this is the first page", example = "true")
    private Boolean first;

    @Schema(description = "Whether this is the last page", example = "false")
    private Boolean last;

    // Default constructor
    public MovieReviewsResponseDTO() {}

    // Constructor
    public MovieReviewsResponseDTO(List<ReviewDTO> reviews, BigDecimal averageRating, Long totalReviews,
                                  Integer page, Integer totalPages, Integer size, Boolean first, Boolean last) {
        this.reviews = reviews;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.page = page;
        this.totalPages = totalPages;
        this.size = size;
        this.first = first;
        this.last = last;
    }

    // Getters and Setters
    public List<ReviewDTO> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDTO> reviews) {
        this.reviews = reviews;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Boolean getFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Boolean getLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    @Override
    public String toString() {
        return "MovieReviewsResponseDTO{" +
                "reviews=" + reviews +
                ", averageRating=" + averageRating +
                ", totalReviews=" + totalReviews +
                ", page=" + page +
                ", totalPages=" + totalPages +
                ", size=" + size +
                ", first=" + first +
                ", last=" + last +
                '}';
    }
}