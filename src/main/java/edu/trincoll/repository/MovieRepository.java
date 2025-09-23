package edu.trincoll.repository;

import java.time.LocalDate;
import java.util.List;

import edu.trincoll.model.Movie;

/**
 * Repository interface for Movie entities.
 * Add domain-specific query methods that make sense for movies.
 */
public interface MovieRepository extends Repository<Movie, Long> {
    
    /**
     * Find all movies with a specific status (e.g., released, upcoming)
     */
    List<Movie> findByStatus(Movie.Status status);
    
    /**
     * Find all movies in a specific genre
     */
    List<Movie> findByGenre(String genre);
    
    /**
     * Find all movies containing a specific tag
     */
    List<Movie> findByTag(String tag);
    
    /**
     * Find movies with title containing search term (case-insensitive)
     */
    List<Movie> findByTitleContaining(String searchTerm);
    
    // Additional domain-specific queries for movies
    /**
     * Find movies directed by a specific director
     */
    List<Movie> findByDirector(String director);
    
    /**
     * Find movies released after a specific date
     */
    List<Movie> findByReleaseDateAfter(LocalDate date);
    
    /**
     * Find movies that are marked as favorite
     */
    List<Movie> findByFavoriteTrue();
}
