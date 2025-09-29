package com.bookmyseat.reviewservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Error response")
public class ErrorResponseDTO {

    @Schema(description = "Error message", example = "Movie not found")
    private String message;

    @Schema(description = "Error code", example = "MOVIE_NOT_FOUND")
    private String errorCode;

    @Schema(description = "HTTP status code", example = "404")
    private Integer status;

    @Schema(description = "Request path", example = "/api/v1/reviews/movie/999")
    private String path;

    @Schema(description = "Timestamp when error occurred")
    private LocalDateTime timestamp;

    @Schema(description = "Validation errors (if applicable)")
    private List<String> validationErrors;

    // Default constructor
    public ErrorResponseDTO() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructor for general errors
    public ErrorResponseDTO(String message, String errorCode, Integer status, String path) {
        this();
        this.message = message;
        this.errorCode = errorCode;
        this.status = status;
        this.path = path;
    }

    // Constructor with validation errors
    public ErrorResponseDTO(String message, String errorCode, Integer status, String path, List<String> validationErrors) {
        this(message, errorCode, status, path);
        this.validationErrors = validationErrors;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    @Override
    public String toString() {
        return "ErrorResponseDTO{" +
                "message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", status=" + status +
                ", path='" + path + '\'' +
                ", timestamp=" + timestamp +
                ", validationErrors=" + validationErrors +
                '}';
    }
}