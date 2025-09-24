package edu.trincoll.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Movie {
    private Long id;
    private String title;
    private String description;
    private String genre;
    private String director;
    private LocalDate releaseDate; // replaces releaseYear for date queries
    private double rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean favorite;
    private List<String> tags = new ArrayList<>();

    public enum Status {
        RELEASED, UPCOMING, CANCELLED,
        ACTIVE, INACTIVE, ARCHIVED   // ⟵
    }
    private Status status = Status.ACTIVE;

    // --- Constructors ---
    public Movie() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Movie(String title, String genre) {
        this();
        this.title = title;
        this.genre = genre;
    }

    public Movie(String title, String genre, String director, LocalDate releaseDate, double rating) {
        this();
        this.title = title;
        this.genre = genre;
        this.director = director;
        this.releaseDate = releaseDate;
        this.rating = rating;
    }

    // --- Getters/Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; this.updatedAt = LocalDateTime.now(); }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; this.updatedAt = LocalDateTime.now(); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; this.updatedAt = LocalDateTime.now(); }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; this.updatedAt = LocalDateTime.now(); }

    public String getCategory() { return getGenre(); }
    public void setCategory(String category) { setGenre(category); }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; this.updatedAt = LocalDateTime.now(); }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; this.updatedAt = LocalDateTime.now(); }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; this.updatedAt = LocalDateTime.now(); }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; this.updatedAt = LocalDateTime.now(); }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = (tags == null) ? new ArrayList<>() : new ArrayList<>(tags); this.updatedAt = LocalDateTime.now(); }
    public void addTag(String tag) { this.tags.add(tag); this.updatedAt = LocalDateTime.now(); }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; this.updatedAt = LocalDateTime.now(); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return String.format(
                "Movie[id=%d, title='%s', genre='%s', director='%s', releaseDate=%s, rating=%.1f, status=%s, favorite=%b, tags=%s]",
                id, title, genre, director, releaseDate, rating, status, favorite, tags
        );
    }
}
