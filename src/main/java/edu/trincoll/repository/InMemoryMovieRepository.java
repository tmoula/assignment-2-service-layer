package edu.trincoll.repository;

import edu.trincoll.model.Movie;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryMovieRepository implements MovieRepository {

    /** Thread-safe storage keyed by internal ID */
    private final Map<Long, Movie> storage = new ConcurrentHashMap<>();

    /** Auto-incrementing ID generator starting at 1 */
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Movie save(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }
        if (movie.getId() == null) {
            movie.setId(idGenerator.getAndIncrement());
        }
        storage.put(movie.getId(), movie);
        return movie;
    }

    @Override
    public List<Movie> saveAll(List<Movie> movies) {
        if (movies == null || movies.isEmpty()) return List.of();
        List<Movie> saved = new ArrayList<>(movies.size());
        for (Movie m : movies) {
            saved.add(save(m));
        }
        return saved;
    }

    @Override
    public Optional<Movie> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Movie> findAll() {
        // Defensive copy of the collection (same object instances)
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public void deleteAll() {
        storage.clear();
        idGenerator.set(1);
    }

    /* ---------------- Domain-specific queries ---------------- */

    @Override
    public List<Movie> findByGenre(String genre) {
        if (genre == null || genre.isBlank()) return List.of();
        String target = genre.toLowerCase(Locale.ROOT);
        return storage.values().stream()
                .filter(m -> m.getGenre() != null && m.getGenre().toLowerCase(Locale.ROOT).equals(target))
                .toList();
    }

    @Override
    public List<Movie> findByTitleContainsIgnoreCase(String title) {
        if (title == null || title.isBlank()) return List.of();
        String needle = title.toLowerCase(Locale.ROOT);
        return storage.values().stream()
                .filter(m -> m.getTitle() != null && m.getTitle().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }
}
