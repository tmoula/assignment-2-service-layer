package edu.trincoll.controller;

import edu.trincoll.model.Movie;
import edu.trincoll.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST controller for managing the Movie Watch List.
 *
 * Handles HTTP requests only; all business logic lives in the service layer.
 */
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService service;

    public MovieController(MovieService service) {
        this.service = service;
    }

    /** Get all movies */
    @GetMapping
    public List<Movie> getAllMovies() {
        return service.findAll();
    }

    /** Get a movie by ID */
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Add a new movie */
    @PostMapping
    public ResponseEntity<Movie> addMovie(@RequestBody Movie movie) {
        try {
            Movie saved = service.save(movie);
            URI location = URI.create("/api/movies/" + saved.getId());
            return ResponseEntity.created(location).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** Update an existing movie */
    @PutMapping("/{id}")
    public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @RequestBody Movie movie) {
        if (!service.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        movie.setId(id);
        try {
            Movie updated = service.save(movie);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /** Delete a movie by ID */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        try {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /* ---------- Extra endpoints ---------- */

    /** Get movies by genre */
    @GetMapping("/genre/{genre}")
    public List<Movie> getMoviesByGenre(@PathVariable String genre) {
        return service.findByGenre(genre);
    }

    /** Search movies by title (case-insensitive) */
    @GetMapping("/search")
    public List<Movie> searchMovies(@RequestParam("title") String title) {
        return service.searchByTitle(title);
    }

    /** Get all unique genres */
    @GetMapping("/genres")
    public Set<String> getAllGenres() {
        return service.getAllUniqueGenres();
    }

    /** Get movies grouped by genre */
    @GetMapping("/grouped")
    public Map<String, List<Movie>> getMoviesGroupedByGenre() {
        return service.groupByGenre();
    }
}
