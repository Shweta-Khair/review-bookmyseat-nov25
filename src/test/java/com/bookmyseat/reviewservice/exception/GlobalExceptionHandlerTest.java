package com.bookmyseat.reviewservice.exception;

import com.bookmyseat.reviewservice.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleMovieNotFoundException_ReturnsNotFoundResponse() {
        // Given
        MovieNotFoundException exception = new MovieNotFoundException(123L);

        // When
        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleMovieNotFoundException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Movie not found with ID: 123", response.getBody().getMessage());
        assertEquals("MOVIE_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void handleReviewNotFoundException_ReturnsNotFoundResponse() {
        // Given
        ReviewNotFoundException exception = new ReviewNotFoundException(456L);

        // When
        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleReviewNotFoundException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Review not found with ID: 456", response.getBody().getMessage());
        assertEquals("REVIEW_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void handleMovieServiceUnavailableException_ReturnsServiceUnavailableResponse() {
        // Given
        MovieServiceUnavailableException exception = new MovieServiceUnavailableException("Service is down");

        // When
        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleMovieServiceUnavailableException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Movie service is currently unavailable. Please try again later.", response.getBody().getMessage());
        assertEquals("MOVIE_SERVICE_UNAVAILABLE", response.getBody().getErrorCode());
        assertEquals(503, response.getBody().getStatus());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void handleValidationException_ReturnsBadRequestWithErrors() {
        // Given
        FieldError fieldError1 = new FieldError("review", "rating", "must not be null");
        FieldError fieldError2 = new FieldError("review", "comment", "size must be between 1 and 500");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);

        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleValidationException(validationException, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getValidationErrors());
        assertEquals(2, response.getBody().getValidationErrors().size());
        assertTrue(response.getBody().getValidationErrors().get(0).contains("rating"));
        assertTrue(response.getBody().getValidationErrors().get(1).contains("comment"));
    }

    @Test
    void handleHttpClientErrorException_NotFound_ReturnsNotFoundResponse() {
        // Given
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                null,
                null
        );

        // When
        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleHttpClientErrorException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void handleHttpClientErrorException_OtherError_ReturnsAppropriateResponse() {
        // Given
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                null,
                null,
                null
        );

        // When
        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleHttpClientErrorException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("External service error", response.getBody().getMessage());
        assertEquals("EXTERNAL_SERVICE_ERROR", response.getBody().getErrorCode());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void handleGenericException_ReturnsInternalServerError() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleGenericException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void handleGenericException_WithNullPointerException_ReturnsInternalServerError() {
        // Given
        Exception exception = new NullPointerException("Null pointer error");

        // When
        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleGenericException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
    }

    @Test
    void handleGenericException_WithIllegalArgumentException_ReturnsInternalServerError() {
        // Given
        Exception exception = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleGenericException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
    }
}