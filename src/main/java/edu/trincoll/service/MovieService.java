package edu.trincoll.service;

import edu.trincoll.model.Movie;
import edu.trincoll.repository.MovieRepository;
import edu.trincoll.repository.Repository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Collaboration Report:
 * - AI Tool Used: ChatGPT, Claude
 * - Most Helpful Prompt: Suggest improvements to error handling and input validation in the service layer 
 * - AI Mistake We Fixed: none
 * - Time Saved: a lot of time, about 12 hours
 * - Team Members: Taha Moula,Daniel Simon, Varvara Esina
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
        if (entity == null) throw new IllegalArgumentException("Movie cannot be null");
        if (entity.getTitle() == null || entity.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (entity.getTitle().length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
        if (entity.getDescription() != null && entity.getDescription().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters");
        }
        if (entity.getGenre() != null && entity.getGenre().trim().isEmpty()) {
            throw new IllegalArgumentException("Genre cannot be empty if provided");
        }
    }

    /* ---------- Methods used by MovieController ---------- */

    public List<Movie> findByGenre(String genre) {
        return (genre == null) ? List.of() : repository.findByGenre(genre);
    }

    public List<Movie> searchByTitle(String title) {
        if (title == null || title.isBlank()) return List.of();
        String q = title.toLowerCase();
        return repository.findByTitleContaining(title).stream()
                .filter(m -> m.getTitle() != null && m.getTitle().toLowerCase().contains(q))
                .toList();
    }

    public Set<String> getAllUniqueGenres() {
        return findAll().stream()
                .map(Movie::getGenre)
                .filter(g -> g != null && !g.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Map<String, List<Movie>> groupByGenre() {
        return findAll().stream()
                .filter(m -> m.getGenre() != null && !m.getGenre().isBlank())
                .collect(Collectors.groupingBy(Movie::getGenre));
    }

    /* ---------- Status utilities ---------- */

    public List<Movie> findByStatus(Movie.Status status) {
        return (status == null) ? List.of() : repository.findByStatus(status);
    }

    public Map<Movie.Status, Long> countByStatus() {
        return findAll().stream()
                .filter(m -> m.getStatus() != null)
                .collect(Collectors.groupingBy(Movie::getStatus, Collectors.counting()));
    }

    public int archiveInactiveMovies() {
        List<Movie> inactiveMovies = findByStatus(Movie.Status.INACTIVE);
        int archivedCount = 0;
        for (Movie movie : inactiveMovies) {
            movie.setStatus(Movie.Status.ARCHIVED);
            try {
                save(movie);
                archivedCount++;
            } catch (Exception e) {
                System.err.println("Failed to archive movie with ID: " + movie.getId());
            }
        }
        return archivedCount;
    }

    /* ---------- Tags utilities ---------- */

    public Set<String> getAllUniqueTags() {
        return findAll().stream()
                .filter(m -> m.getTags() != null)
                .flatMap(m -> m.getTags().stream())
                .filter(t -> t != null && !t.isBlank())
                .collect(Collectors.toSet());
    }

    public List<Movie> findByAllTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) return List.of();
        return findAll().stream()
                .filter(m -> m.getTags() != null && !m.getTags().isEmpty())
                .filter(m -> new HashSet<>(m.getTags()).containsAll(tags))
                .toList();
    }

    public List<Movie> findByAnyTag(Set<String> tags) {
        if (tags == null || tags.isEmpty()) return List.of();
        return findAll().stream()
                .filter(m -> m.getTags() != null && !m.getTags().isEmpty())
                .filter(m -> m.getTags().stream().anyMatch(tags::contains))
                .toList();
    }

    public List<String> getMostPopularTags(int limit) {
        if (limit <= 0) return List.of();
        return findAll().stream()
                .filter(m -> m.getTags() != null)
                .flatMap(m -> m.getTags().stream())
                .filter(t -> t != null && !t.isBlank())
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    /* ---------- Stats (using genre) ---------- */

    public Map<String, Object> getMovieStatistics() {
        List<Movie> all = findAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMovies", all.size());
        stats.put("statusCounts", countByStatus());
        stats.put("genreCounts", all.stream()
                .map(Movie::getGenre)
                .filter(g -> g != null && !g.isBlank())
                .collect(Collectors.groupingBy(g -> g, Collectors.counting())));
        stats.put("totalUniqueTags", getAllUniqueTags().size());
        stats.put("averageTagsPerMovie", all.stream()
                .filter(m -> m.getTags() != null)
                .mapToInt(m -> m.getTags().size())
                .average()
                .orElse(0.0));
        return stats;
    }

    /* ---------- Update helper ---------- */

    public Movie update(Long id, Movie payload) {
        return repository.findById(id).map(existing -> {
            if (payload.getTitle() != null) existing.setTitle(payload.getTitle());
            if (payload.getGenre() != null) existing.setGenre(payload.getGenre());
            if (payload.getDescription() != null) existing.setDescription(payload.getDescription());
            if (payload.getDirector() != null) existing.setDirector(payload.getDirector());
            if (payload.getReleaseDate() != null) existing.setReleaseDate(payload.getReleaseDate());
            if (payload.getRating() != 0.0) existing.setRating(payload.getRating());
            existing.setFavorite(payload.isFavorite());
            if (payload.getStatus() != null) existing.setStatus(payload.getStatus());
            if (payload.getTags() != null && !payload.getTags().isEmpty()) {
                existing.setTags(payload.getTags()); // make sure Movie has setTags(...)
            }
            return repository.save(existing);
        }).orElseThrow(() -> new NoSuchElementException("Movie not found: " + id));
    }

    /* ---------- Methods required by tests (category + search) ---------- */

    public Map<String, List<Movie>> groupByCategory() {
        return findAll().stream()
                .filter(m -> m.getCategory() != null && !m.getCategory().isBlank())
                .collect(Collectors.groupingBy(Movie::getCategory));
    }

    public List<Movie> search(String query) {
        if (query == null || query.isBlank()) return List.of();
        String q = query.toLowerCase();

        return findAll().stream()
                .filter(m -> {
                    boolean titleMatch = m.getTitle() != null && m.getTitle().toLowerCase().contains(q);
                    boolean descMatch  = m.getDescription() != null && m.getDescription().toLowerCase().contains(q);
                    // category is an alias to genre; either check is fine
                    boolean genreMatch = m.getGenre() != null && m.getGenre().toLowerCase().contains(q);
                    boolean categoryMatch = m.getCategory() != null && m.getCategory().toLowerCase().contains(q);
                    // optional: tags (won’t affect your current test data)
                    boolean tagMatch = m.getTags() != null && m.getTags().stream()
                            .filter(Objects::nonNull)
                            .map(String::toLowerCase)
                            .anyMatch(t -> t.contains(q));
                    return titleMatch || descMatch || genreMatch || categoryMatch || tagMatch;
                })
                .toList();
    }
}
