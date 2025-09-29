package com.bookmyseat.reviewservice.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(String message) {
        super(message);
    }

    public ReviewNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReviewNotFoundException(Long reviewId) {
        super("Review not found with ID: " + reviewId);
    }
}