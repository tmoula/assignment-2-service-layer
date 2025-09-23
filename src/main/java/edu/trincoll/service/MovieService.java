package edu.trincoll.service;
import edu.trincoll.model.Movie;
import edu.trincoll.repository.MovieRepository;
import edu.trincoll.repository.Repository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Collaboration Summary:
 * This service layer implements comprehensive business logic for Movie management including:
 * - CRUD operations (inherited from BaseService)
 * - Advanced querying by status, category, and tags
 * - Data aggregation and analytics (grouping, counting)
 * - Search functionality across title and description
 * - Bulk operations for archiving inactive items
 * 
 * Implementation uses Java 8+ streams, collectors, and set operations for efficient
 * data processing and follows Spring Boot service layer patterns.
 *
 * Service layer implementing business logic for Movie domain.
 * Extends BaseService for common CRUD operations.
 */
@Service
public class MovieService extends BaseService<Movie, Long> {
    private final MovieRepository repository;

    public MovieService(MovieRepository repository) {
        this.repository = repository;
    }

    @Override
    protected Repository<Movie, Long> getRepository() {
        return repository;
    }

    @Override
    public void validateEntity(Movie entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }
        if (entity.getTitle() == null || entity.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (entity.getTitle().length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
        // Additional validation rules
        if (entity.getDescription() != null && entity.getDescription().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters");
        }
        if (entity.getCategory() != null && entity.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty if provided");
        }
    }

    /**
     * Find movies by status
     */
    public List<Movie> findByStatus(Movie.Status status) {
        return repository.findByStatus(status);
    }

    /**
     * Find movies by category
     */
    public List<Movie> findByCategory(String category) {
        return repository.findByCategory(category);
    }

    /**
     * Group movies by category using Collectors
     */
    public Map<String, List<Movie>> groupByCategory() {
        return findAll().stream()
                .filter(movie -> movie.getCategory() != null)
                .collect(Collectors.groupingBy(Movie::getCategory));
    }

    /**
     * Get all unique tags from all movies
     */
    public Set<String> getAllUniqueTags() {
        return findAll().stream()
                .filter(movie -> movie.getTags() != null)
                .flatMap(movie -> movie.getTags().stream())
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Get count of movies per status
     */
    public Map<Movie.Status, Long> countByStatus() {
        return findAll().stream()
                .filter(movie -> movie.getStatus() != null)
                .collect(Collectors.groupingBy(
                        Movie::getStatus,
                        Collectors.counting()
                ));
    }

    /**
     * Find movies with multiple tags (AND operation)
     * Uses set intersection to find movies that contain ALL specified tags
     */
    public List<Movie> findByAllTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        
        return findAll().stream()
                .filter(movie -> movie.getTags() != null && !movie.getTags().isEmpty())
                .filter(movie -> {
                    Set<String> movieTags = new HashSet<>(movie.getTags());
                    return movieTags.containsAll(tags);
                })
                .collect(Collectors.toList());
    }

    /**
     * Find movies with any of the tags (OR operation)
     * Uses set intersection to find movies that contain ANY of the specified tags
     */
    public List<Movie> findByAnyTag(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        
        return findAll().stream()
                .filter(movie -> movie.getTags() != null && !movie.getTags().isEmpty())
                .filter(movie -> {
                    Set<String> movieTags = new HashSet<>(movie.getTags());
                    // Check if there's any intersection between movie tags and search tags
                    return movieTags.stream().anyMatch(tags::contains);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get most popular tags (top N by frequency)
     * Uses Map for counting and sorting by frequency
     */
    public List<String> getMostPopularTags(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        
        return findAll().stream()
                .filter(movie -> movie.getTags() != null)
                .flatMap(movie -> movie.getTags().stream())
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .collect(Collectors.groupingBy(
                        tag -> tag,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Search movies by query (searches title and description)
     * Case-insensitive search across title and description fields
     */
    public List<Movie> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String lowerQuery = query.toLowerCase().trim();
        
        return findAll().stream()
                .filter(movie -> {
                    boolean titleMatch = movie.getTitle() != null && 
                            movie.getTitle().toLowerCase().contains(lowerQuery);
                    boolean descriptionMatch = movie.getDescription() != null && 
                            movie.getDescription().toLowerCase().contains(lowerQuery);
                    return titleMatch || descriptionMatch;
                })
                .collect(Collectors.toList());
    }

    /**
     * Archive old movies (change status to ARCHIVED)
     * This implementation assumes movies with INACTIVE status should be archived.
     * Returns the number of movies that were archived.
     */
    public int archiveInactiveMovies() {
        List<Movie> inactiveMovies = findByStatus(Movie.Status.INACTIVE);
        
        int archivedCount = 0;
        for (Movie movie : inactiveMovies) {
            movie.setStatus(Movie.Status.ARCHIVED);
            try {
                update(movie.getId(), movie);
                archivedCount++;
            } catch (Exception e) {
                // Log error but continue processing other movies
                // In a real application, you might want to use a logger here
                System.err.println("Failed to archive movie with ID: " + movie.getId());
            }
        }
        
        return archivedCount;
    }

    // Additional utility methods that might be useful

    /**
     * Get movies by multiple statuses
     */
    public List<Movie> findByStatuses(Set<Movie.Status> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return Collections.emptyList();
        }
        
        return findAll().stream()
                .filter(movie -> movie.getStatus() != null && statuses.contains(movie.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Get summary statistics about movies
     */
    public Map<String, Object> getMovieStatistics() {
        List<Movie> allMovies = findAll();
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalMovies", allMovies.size());
        stats.put("statusCounts", countByStatus());
        stats.put("categoryCounts", allMovies.stream()
                .filter(movie -> movie.getCategory() != null)
                .collect(Collectors.groupingBy(Movie::getCategory, Collectors.counting())));
        stats.put("totalUniqueTags", getAllUniqueTags().size());
        stats.put("averageTagsPerMovie", allMovies.stream()
                .filter(movie -> movie.getTags() != null)
                .mapToInt(movie -> movie.getTags().size())
                .average()
                .orElse(0.0));
        
        return stats;
    }
}