package com.bookmyseat.reviewservice.repository;

import com.bookmyseat.reviewservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Find all reviews for a specific movie with pagination
     */
    Page<Review> findByMovieIdOrderByReviewDateDesc(Long movieId, Pageable pageable);

    /**
     * Find all reviews for a specific movie
     */
    List<Review> findByMovieIdOrderByReviewDateDesc(Long movieId);

    /**
     * Count total reviews for a movie
     */
    long countByMovieId(Long movieId);

    /**
     * Check if a movie has any reviews
     */
    boolean existsByMovieId(Long movieId);

    /**
     * Calculate average rating for a movie
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.movieId = :movieId")
    BigDecimal calculateAverageRating(@Param("movieId") Long movieId);

    /**
     * Get rating distribution for a movie
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.movieId = :movieId GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getRatingDistribution(@Param("movieId") Long movieId);

    /**
     * Count reviews by movie and rating value
     */
    long countByMovieIdAndRating(Long movieId, BigDecimal rating);

    /**
     * Find latest reviews for a movie (limited)
     */
    @Query("SELECT r FROM Review r WHERE r.movieId = :movieId ORDER BY r.reviewDate DESC")
    List<Review> findLatestReviewsForMovie(@Param("movieId") Long movieId, Pageable pageable);

    /**
     * Find reviews by user name
     */
    List<Review> findByUserNameOrderByReviewDateDesc(String userName);

    /**
     * Get movies with most reviews
     */
    @Query("SELECT r.movieId, COUNT(r) as reviewCount FROM Review r GROUP BY r.movieId ORDER BY reviewCount DESC")
    List<Object[]> findMoviesWithMostReviews(Pageable pageable);

    /**
     * Get highest rated movies (with minimum review count)
     */
    @Query("SELECT r.movieId, AVG(r.rating) as avgRating FROM Review r GROUP BY r.movieId HAVING COUNT(r) >= :minReviews ORDER BY avgRating DESC")
    List<Object[]> findHighestRatedMovies(@Param("minReviews") long minReviews, Pageable pageable);
}