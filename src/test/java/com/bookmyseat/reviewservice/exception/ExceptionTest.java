package com.bookmyseat.reviewservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void testMovieNotFoundException() {
        Long movieId = 123L;
        MovieNotFoundException exception = new MovieNotFoundException(movieId);

        assertNotNull(exception);
        assertEquals("Movie not found with ID: 123", exception.getMessage());
        assertTrue(exception.getMessage().contains(movieId.toString()));
    }

    @Test
    void testMovieNotFoundExceptionWithMessage() {
        MovieNotFoundException exception = new MovieNotFoundException(999L);

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void testReviewNotFoundException() {
        Long reviewId = 456L;
        ReviewNotFoundException exception = new ReviewNotFoundException(reviewId);

        assertNotNull(exception);
        assertEquals("Review not found with ID: 456", exception.getMessage());
        assertTrue(exception.getMessage().contains(reviewId.toString()));
    }

    @Test
    void testReviewNotFoundExceptionWithMessage() {
        ReviewNotFoundException exception = new ReviewNotFoundException(999L);

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void testMovieServiceUnavailableException() {
        String errorMessage = "Service is down";
        MovieServiceUnavailableException exception = new MovieServiceUnavailableException(errorMessage);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testMovieServiceUnavailableExceptionWithCause() {
        String errorMessage = "Connection timeout";
        Throwable cause = new RuntimeException("Network error");
        MovieServiceUnavailableException exception = new MovieServiceUnavailableException(errorMessage, cause);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNotNull(exception.getCause());
        assertEquals("Network error", exception.getCause().getMessage());
    }

    @Test
    void testExceptionInheritance() {
        MovieNotFoundException movieException = new MovieNotFoundException(1L);
        ReviewNotFoundException reviewException = new ReviewNotFoundException(1L);
        MovieServiceUnavailableException serviceException = new MovieServiceUnavailableException("Error");

        // All should be RuntimeExceptions
        assertTrue(movieException instanceof RuntimeException);
        assertTrue(reviewException instanceof RuntimeException);
        assertTrue(serviceException instanceof RuntimeException);
    }

    @Test
    void testExceptionMessages() {
        MovieNotFoundException movieException = new MovieNotFoundException(42L);
        ReviewNotFoundException reviewException = new ReviewNotFoundException(84L);

        assertNotNull(movieException.getMessage());
        assertNotNull(reviewException.getMessage());
        assertFalse(movieException.getMessage().isEmpty());
        assertFalse(reviewException.getMessage().isEmpty());
    }

    @Test
    void testExceptionThrown() {
        assertThrows(MovieNotFoundException.class, () -> {
            throw new MovieNotFoundException(1L);
        });

        assertThrows(ReviewNotFoundException.class, () -> {
            throw new ReviewNotFoundException(1L);
        });

        assertThrows(MovieServiceUnavailableException.class, () -> {
            throw new MovieServiceUnavailableException("Service down");
        });
    }

    @Test
    void testMovieServiceExceptionCauseChain() {
        RuntimeException rootCause = new RuntimeException("Root cause");
        Exception intermediateCause = new Exception("Intermediate", rootCause);
        MovieServiceUnavailableException exception = new MovieServiceUnavailableException(
            "Top level error",
            intermediateCause
        );

        assertNotNull(exception.getCause());
        assertNotNull(exception.getCause().getCause());
        assertEquals("Root cause", exception.getCause().getCause().getMessage());
    }

    @Test
    void testExceptionStackTrace() {
        MovieNotFoundException exception = new MovieNotFoundException(100L);

        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }
}