package com.bookmyseat.reviewservice.exception;

public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(String message) {
        super(message);
    }

    public MovieNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MovieNotFoundException(Long movieId) {
        super("Movie not found with ID: " + movieId);
    }
}