package com.bookmyseat.reviewservice.exception;

public class MovieServiceUnavailableException extends RuntimeException {

    public MovieServiceUnavailableException(String message) {
        super(message);
    }

    public MovieServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}