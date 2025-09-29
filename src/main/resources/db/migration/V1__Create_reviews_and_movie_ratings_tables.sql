-- Create reviews table
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    rating DECIMAL(2,1) NOT NULL CHECK (rating >= 1.0 AND rating <= 5.0),
    comment TEXT,
    review_date TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_movie_id (movie_id),
    INDEX idx_review_date (review_date),
    INDEX idx_rating (rating),
    INDEX idx_user_name (user_name)
);

-- Create movie ratings cache table
CREATE TABLE movie_ratings (
    movie_id BIGINT PRIMARY KEY,
    average_rating DECIMAL(3,2),
    total_reviews INT DEFAULT 0,
    rating_1_count INT DEFAULT 0,
    rating_2_count INT DEFAULT 0,
    rating_3_count INT DEFAULT 0,
    rating_4_count INT DEFAULT 0,
    rating_5_count INT DEFAULT 0,
    last_updated TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_average_rating (average_rating),
    INDEX idx_total_reviews (total_reviews)
);