package edu.trincoll.model;

import java.time.LocalDateTime;



public class Movie {
    private Long id;
    private String title;
    private String description;
    private String genre;
    private String director;
    private int releaseYear;
    private double rating; // e.g., IMDb style (0–10)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- Constructors ---
    public Movie() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Movie(String title, String genre, String director, int releaseYear, double rating) {
        this();
        this.title = title;
        this.genre = genre;
        this.director = director;
        this.releaseYear = releaseYear;
        this.rating = rating;
    }

    // --- Getters/Setters ---
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public String getGenre() {
        return genre;
    }
    public void setGenre(String genre) {
        this.genre = genre;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDirector() {
        return director;
    }
    public void setDirector(String director) {
        this.director = director;
        this.updatedAt = LocalDateTime.now();
    }

    public int getReleaseYear() {
        return releaseYear;
    }
    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
        this.updatedAt = LocalDateTime.now();
    }

    public double getRating() {
        return rating;
    }
    public void setRating(double rating) {
        this.rating = rating;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return String.format("Movie[id=%d, title='%s', genre='%s', director='%s', year=%d, rating=%.1f]",
                id, title, genre, director, releaseYear, rating);
    }
}
