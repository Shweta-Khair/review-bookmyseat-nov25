package com.bookmyseat.reviewservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieDetailDTO {
    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private String genre;
    private String language;
    private LocalDate releaseDate;

    // Default constructor
    public MovieDetailDTO() {}

    // Constructor
    public MovieDetailDTO(Long id, String title, String description, Integer durationMinutes,
                         String genre, String language, LocalDate releaseDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.genre = genre;
        this.language = language;
        this.releaseDate = releaseDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public String toString() {
        return "MovieDetailDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", durationMinutes=" + durationMinutes +
                ", genre='" + genre + '\'' +
                ", language='" + language + '\'' +
                ", releaseDate=" + releaseDate +
                '}';
    }
}