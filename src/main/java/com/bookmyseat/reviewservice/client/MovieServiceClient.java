package com.bookmyseat.reviewservice.client;

import com.bookmyseat.reviewservice.dto.MovieDetailDTO;
import com.bookmyseat.reviewservice.exception.MovieNotFoundException;
import com.bookmyseat.reviewservice.exception.MovieServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
public class MovieServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(MovieServiceClient.class);

    private final RestClient restClient;
    private final String movieServiceBaseUrl;

    public MovieServiceClient(RestClient.Builder restClientBuilder,
                             @Value("${movie-service.base-url}") String movieServiceBaseUrl) {
        this.movieServiceBaseUrl = movieServiceBaseUrl;
        this.restClient = restClientBuilder
                .baseUrl(movieServiceBaseUrl)
                .build();
    }

    @CircuitBreaker(name = "movieService", fallbackMethod = "getMovieAsyncFallback")
    @Retry(name = "movieService")
    @TimeLimiter(name = "movieService")
    public CompletionStage<MovieDetailDTO> getMovieByIdAsync(Long movieId) {
        return CompletableFuture.supplyAsync(() -> getMovieById(movieId));
    }

    @Retry(name = "movieService", fallbackMethod = "getMovieFallback")
    @CircuitBreaker(name = "movieService")
    public MovieDetailDTO getMovieById(Long movieId) {
        try {
            logger.debug("Fetching movie details for movieId: {}", movieId);

            MovieDetailDTO movie = restClient.get()
                    .uri("/api/v1/movies/{movieId}", movieId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        logger.warn("Movie not found with ID: {}", movieId);
                        throw new MovieNotFoundException(movieId);
                    })
                    .onStatus(status -> status.is5xxServerError(), (request, response) -> {
                        logger.error("Server error {} when fetching movie {}", response.getStatusCode(), movieId);
                        throw new MovieServiceUnavailableException("Movie service returned server error: " + response.getStatusCode());
                    })
                    .body(MovieDetailDTO.class);

            if (movie == null) {
                logger.warn("Movie service returned null for movieId: {}", movieId);
                throw new MovieNotFoundException(movieId);
            }

            logger.debug("Successfully fetched movie: {} for movieId: {}", movie.getTitle(), movieId);
            return movie;

        } catch (MovieNotFoundException e) {
            // Re-throw MovieNotFoundException without wrapping
            throw e;
        } catch (MovieServiceUnavailableException e) {
            // Already wrapped by onStatus handler, don't wrap again
            throw e;
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Movie not found with ID: {}", movieId);
            throw new MovieNotFoundException(movieId);
        } catch (HttpServerErrorException e) {
            // 5xx errors - service unavailable, should trigger retry
            logger.error("HTTP server error when fetching movie {}: {} - {}",
                        movieId, e.getStatusCode(), e.getMessage());
            throw new MovieServiceUnavailableException("Movie service server error: " + e.getMessage(), e);
        } catch (HttpClientErrorException e) {
            // 4xx errors other than 404
            logger.error("HTTP client error when fetching movie {}: {} - {}",
                        movieId, e.getStatusCode(), e.getMessage());
            throw new MovieServiceUnavailableException("Movie service client error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error when fetching movie {}: {}", movieId, e.getMessage(), e);
            throw new MovieServiceUnavailableException("Movie service unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Fallback method called after all retries are exhausted
     */
    public MovieDetailDTO getMovieFallback(Long movieId, Exception ex) {
        // Don't use fallback for MovieNotFoundException - rethrow it
        if (ex instanceof MovieNotFoundException) {
            throw (MovieNotFoundException) ex;
        }
        logger.error("Fallback triggered for movieId: {} after retries exhausted due to: {}",
                    movieId, ex.getMessage());
        return createFallbackMovie(movieId);
    }

    /**
     * Create a fallback movie object when service is unavailable
     */
    private MovieDetailDTO createFallbackMovie(Long movieId) {
        return new MovieDetailDTO(
            movieId,
            "Unknown Movie",
            "Movie details temporarily unavailable",
            0,
            "Unknown",
            "Unknown",
            null
        );
    }

    /**
     * Async fallback method for circuit breaker
     */
    public CompletionStage<MovieDetailDTO> getMovieAsyncFallback(Long movieId, Exception ex) {
        logger.error("Circuit breaker async fallback triggered for movieId: {} due to: {}",
                    movieId, ex.getMessage());
        return CompletableFuture.completedFuture(createFallbackMovie(movieId));
    }

    /**
     * Utility method to check if movie exists
     */
    public boolean movieExists(Long movieId) {
        try {
            getMovieById(movieId);
            return true;
        } catch (MovieNotFoundException e) {
            return false;
        } catch (MovieServiceUnavailableException e) {
            // If service is unavailable, we cannot determine if movie exists
            // Log warning and re-throw to let caller handle
            logger.warn("Cannot verify movie existence for {} due to service unavailability: {}",
                       movieId, e.getMessage());
            throw e;
        }
    }

    /**
     * Get movie title (used for display purposes)
     */
    public String getMovieTitle(Long movieId) {
        try {
            MovieDetailDTO movie = getMovieById(movieId);
            return movie.getTitle();
        } catch (MovieNotFoundException e) {
            logger.warn("Movie title not found for movieId: {}", movieId);
            return "Unknown Movie";
        } catch (MovieServiceUnavailableException e) {
            logger.warn("Cannot fetch movie title for {} due to service unavailability", movieId);
            return "Movie Title Unavailable";
        }
    }
}