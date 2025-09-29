package com.bookmyseat.reviewservice.exception;

import com.bookmyseat.reviewservice.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleMovieNotFoundException(
            MovieNotFoundException ex, HttpServletRequest request) {

        logger.warn("Movie not found: {}", ex.getMessage());

        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "MOVIE_NOT_FOUND",
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleReviewNotFoundException(
            ReviewNotFoundException ex, HttpServletRequest request) {

        logger.warn("Review not found: {}", ex.getMessage());

        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "REVIEW_NOT_FOUND",
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MovieServiceUnavailableException.class)
    public ResponseEntity<ErrorResponseDTO> handleMovieServiceUnavailableException(
            MovieServiceUnavailableException ex, HttpServletRequest request) {

        logger.error("Movie service unavailable: {}", ex.getMessage());

        ErrorResponseDTO error = new ErrorResponseDTO(
                "Movie service is currently unavailable. Please try again later.",
                "MOVIE_SERVICE_UNAVAILABLE",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        logger.warn("Validation failed: {}", ex.getMessage());

        List<String> validationErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }

        ErrorResponseDTO error = new ErrorResponseDTO(
                "Validation failed",
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpClientErrorException(
            HttpClientErrorException ex, HttpServletRequest request) {

        logger.error("HTTP client error: {} - {}", ex.getStatusCode(), ex.getMessage());

        String message = "External service error";
        String errorCode = "EXTERNAL_SERVICE_ERROR";
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            message = "Resource not found";
            errorCode = "RESOURCE_NOT_FOUND";
        }

        ErrorResponseDTO error = new ErrorResponseDTO(
                message,
                errorCode,
                status.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFoundException(NoResourceFoundException ex) {
        // Suppress logging for favicon.ico and other static resource requests
        if (ex.getMessage() != null && ex.getMessage().contains("favicon.ico")) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        logger.warn("Static resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {

        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
                "An unexpected error occurred. Please try again later.",
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}