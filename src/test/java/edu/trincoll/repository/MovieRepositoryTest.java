package edu.trincoll.repository;

import edu.trincoll.model.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the movie repository layer.
 * These tests should pass when the repository is properly implemented.
 */
class MovieRepositoryTest {

    private MovieRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMovieRepository();
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should save and retrieve movie by ID")
    void testSaveAndFindById() {
        Movie movie = new Movie();
        movie.setTitle("Inception");
        movie.setGenre("Sci-Fi");

        Movie saved = repository.save(movie);

        assertThat(saved.getId()).isNotNull();

        Optional<Movie> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Inception");
    }

    @Test
    @DisplayName("Should return empty Optional for non-existent ID")
    void testFindByIdNotFound() {
        Optional<Movie> found = repository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all movies")
    void testFindAll() {
        repository.save(new Movie("Movie 1", "Action"));
        repository.save(new Movie("Movie 2", "Drama"));
        repository.save(new Movie("Movie 3", "Comedy"));

        List<Movie> all = repository.findAll();

        assertThat(all).hasSize(3);
        assertThat(all).extracting(Movie::getTitle)
                .containsExactlyInAnyOrder("Movie 1", "Movie 2", "Movie 3");
    }

    @Test
    @DisplayName("Should delete movie by ID")
    void testDeleteById() {
        Movie movie = repository.save(new Movie("To Delete", "Horror"));
        Long id = movie.getId();

        assertThat(repository.existsById(id)).isTrue();

        repository.deleteById(id);

        assertThat(repository.existsById(id)).isFalse();
        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should check if movie exists")
    void testExistsById() {
        Movie movie = repository.save(new Movie("Exists", "Thriller"));

        assertThat(repository.existsById(movie.getId())).isTrue();
        assertThat(repository.existsById(999L)).isFalse();
    }

    @Test
    @DisplayName("Should count movies correctly")
    void testCount() {
        assertThat(repository.count()).isZero();

        repository.save(new Movie("Movie 1", "Action"));
        repository.save(new Movie("Movie 2", "Drama"));

        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should delete all movies")
    void testDeleteAll() {
        repository.save(new Movie("Movie 1", "Action"));
        repository.save(new Movie("Movie 2", "Drama"));

        assertThat(repository.count()).isEqualTo(2);

        repository.deleteAll();

        assertThat(repository.count()).isZero();
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should save multiple movies")
    void testSaveAll() {
        List<Movie> movies = List.of(
                new Movie("Movie 1", "Action"),
                new Movie("Movie 2", "Comedy"),
                new Movie("Movie 3", "Drama")
        );

        List<Movie> saved = repository.saveAll(movies);

        assertThat(saved).hasSize(3);
        assertThat(saved).allMatch(movie -> movie.getId() != null);
        assertThat(repository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find movies by status")
    void testFindByStatus() {
        Movie released = new Movie("Released Movie", "Action");
        released.setStatus(Movie.Status.RELEASED);

        Movie upcoming = new Movie("Upcoming Movie", "Sci-Fi");
        upcoming.setStatus(Movie.Status.UPCOMING);

        Movie cancelled = new Movie("Cancelled Movie", "Horror");
        cancelled.setStatus(Movie.Status.CANCELLED);

        repository.save(released);
        repository.save(upcoming);
        repository.save(cancelled);

        List<Movie> releasedMovies = repository.findByStatus(Movie.Status.RELEASED);
        assertThat(releasedMovies).hasSize(1);
        assertThat(releasedMovies.get(0).getTitle()).isEqualTo("Released Movie");

        List<Movie> cancelledMovies = repository.findByStatus(Movie.Status.CANCELLED);
        assertThat(cancelledMovies).hasSize(1);
    }

    @Test
    @DisplayName("Should find movies by genre")
    void testFindByGenre() {
        Movie movie1 = new Movie("Movie 1", "Action");
        Movie movie2 = new Movie("Movie 2", "Drama");
        Movie movie3 = new Movie("Movie 3", "Action");

        repository.save(movie1);
        repository.save(movie2);
        repository.save(movie3);

        List<Movie> actionMovies = repository.findByGenre("Action");

        assertThat(actionMovies).hasSize(2);
        assertThat(actionMovies).extracting(Movie::getTitle)
                .containsExactlyInAnyOrder("Movie 1", "Movie 3");
    }

    @Test
    @DisplayName("Should find movies by title containing search term")
    void testFindByTitleContaining() {
        repository.save(new Movie("Inception", "Sci-Fi"));
        repository.save(new Movie("Interstellar", "Sci-Fi"));
        repository.save(new Movie("Dunkirk", "War"));
        repository.save(new Movie("Tenet", "Action"));

        List<Movie> sciFiMovies = repository.findByTitleContaining("In");

        assertThat(sciFiMovies).hasSize(2);
        assertThat(sciFiMovies).extracting(Movie::getTitle)
                .containsExactlyInAnyOrder("Inception", "Interstellar");
    }
}
