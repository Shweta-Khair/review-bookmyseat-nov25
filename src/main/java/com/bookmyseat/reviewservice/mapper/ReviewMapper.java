package com.bookmyseat.reviewservice.mapper;

import com.bookmyseat.reviewservice.dto.ReviewDTO;
import com.bookmyseat.reviewservice.dto.ReviewSubmissionDTO;
import com.bookmyseat.reviewservice.entity.Review;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReviewMapper {

    /**
     * Convert Review entity to ReviewDTO
     */
    public ReviewDTO toReviewDTO(Review review) {
        if (review == null) {
            return null;
        }

        return new ReviewDTO(
                review.getId(),
                review.getMovieId(),
                null, // Movie title will be set by service layer
                review.getUserName(),
                review.getRating(),
                review.getComment(),
                review.getReviewDate()
        );
    }

    /**
     * Convert Review entity to ReviewDTO with movie title
     */
    public ReviewDTO toReviewDTO(Review review, String movieTitle) {
        ReviewDTO dto = toReviewDTO(review);
        if (dto != null) {
            dto.setMovieTitle(movieTitle);
        }
        return dto;
    }

    /**
     * Convert ReviewSubmissionDTO to Review entity
     */
    public Review toReview(ReviewSubmissionDTO submissionDTO) {
        if (submissionDTO == null) {
            return null;
        }

        return new Review(
                submissionDTO.getMovieId(),
                submissionDTO.getUserName(),
                submissionDTO.getRating(),
                submissionDTO.getComment()
        );
    }

    /**
     * Convert list of Review entities to list of ReviewDTOs
     */
    public List<ReviewDTO> toReviewDTOList(List<Review> reviews) {
        if (reviews == null) {
            return null;
        }

        return reviews.stream()
                .map(this::toReviewDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Review entities to list of ReviewDTOs with movie title
     */
    public List<ReviewDTO> toReviewDTOList(List<Review> reviews, String movieTitle) {
        if (reviews == null) {
            return null;
        }

        return reviews.stream()
                .map(review -> toReviewDTO(review, movieTitle))
                .collect(Collectors.toList());
    }

    /**
     * Update existing Review entity with ReviewSubmissionDTO data
     */
    public void updateReview(Review review, ReviewSubmissionDTO submissionDTO) {
        if (review == null || submissionDTO == null) {
            return;
        }

        review.setMovieId(submissionDTO.getMovieId());
        review.setUserName(submissionDTO.getUserName());
        review.setRating(submissionDTO.getRating());
        review.setComment(submissionDTO.getComment());
    }
}