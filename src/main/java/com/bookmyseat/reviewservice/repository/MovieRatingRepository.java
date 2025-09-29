package com.bookmyseat.reviewservice.repository;

import com.bookmyseat.reviewservice.entity.MovieRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRatingRepository extends JpaRepository<MovieRating, Long> {

    /**
     * Find movie rating by movie ID
     */
    Optional<MovieRating> findByMovieId(Long movieId);

    /**
     * Check if rating exists for a movie
     */
    boolean existsByMovieId(Long movieId);

    /**
     * Find movies with rating above threshold
     */
    List<MovieRating> findByAverageRatingGreaterThanEqualOrderByAverageRatingDesc(BigDecimal minRating);

    /**
     * Find movies with minimum number of reviews
     */
    List<MovieRating> findByTotalReviewsGreaterThanEqualOrderByTotalReviewsDesc(Integer minReviews);

    /**
     * Get top rated movies with minimum review count
     */
    @Query("SELECT mr FROM MovieRating mr WHERE mr.totalReviews >= :minReviews ORDER BY mr.averageRating DESC, mr.totalReviews DESC")
    List<MovieRating> findTopRatedMovies(@Param("minReviews") Integer minReviews);

    /**
     * Get most reviewed movies
     */
    @Query("SELECT mr FROM MovieRating mr ORDER BY mr.totalReviews DESC, mr.averageRating DESC")
    List<MovieRating> findMostReviewedMovies();

    /**
     * Get movies by rating range
     */
    @Query("SELECT mr FROM MovieRating mr WHERE mr.averageRating BETWEEN :minRating AND :maxRating ORDER BY mr.averageRating DESC")
    List<MovieRating> findMoviesByRatingRange(@Param("minRating") BigDecimal minRating, @Param("maxRating") BigDecimal maxRating);

    /**
     * Get average rating across all movies
     */
    @Query("SELECT AVG(mr.averageRating) FROM MovieRating mr WHERE mr.totalReviews > 0")
    BigDecimal getOverallAverageRating();

    /**
     * Get total reviews across all movies
     */
    @Query("SELECT SUM(mr.totalReviews) FROM MovieRating mr")
    Long getTotalReviewsCount();

    /**
     * Delete rating cache for a movie
     */
    void deleteByMovieId(Long movieId);
}