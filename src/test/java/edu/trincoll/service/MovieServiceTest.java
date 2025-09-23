package edu.trincoll.service;

import edu.trincoll.model.Movie;
import edu.trincoll.repository.InMemoryMovieRepository;
import edu.trincoll.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the MovieService layer.
 * Tests both inherited BaseService functionality and MovieService-specific methods.
 */
class MovieServiceTest {

    private MovieService service;
    private MovieRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMovieRepository();
        service = new MovieService(repository);
        repository.deleteAll();
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should reject null movie")
        void testValidateNullMovie() {
            assertThatThrownBy(() -> service.validateEntity(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }

        @Test
        @DisplayName("Should reject movie without title")
        void testValidateNoTitle() {
            Movie movie = new Movie();
            movie.setDescription("Description");

            assertThatThrownBy(() -> service.validateEntity(movie))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Title is required");
        }

        @Test
        @DisplayName("Should reject movie with empty title")
        void testValidateEmptyTitle() {
            Movie movie = new Movie("   ", "Description");

            assertThatThrownBy(() -> service.validateEntity(movie))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Title is required");
        }

        @Test
        @DisplayName("Should reject movie with title too long")
        void testValidateTitleTooLong() {
            String longTitle = "a".repeat(101);
            Movie movie = new Movie(longTitle, "Description");

            assertThatThrownBy(() -> service.validateEntity(movie))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot exceed 100 characters");
        }

        @Test
        @DisplayName("Should accept valid movie")
        void testValidateValidMovie() {
            Movie movie = new Movie("Valid Title", "Valid Description");
            movie.setCategory("Action");

            assertThatNoException().isThrownBy(() -> service.validateEntity(movie));
        }
    }

    @Nested
    @DisplayName("CRUD Operations Tests")
    class CrudTests {

        @Test
        @DisplayName("Should save movie with validation")
        void testSave() {
            Movie movie = new Movie("Test Movie", "Description");

            Movie saved = service.save(movie);

            assertThat(saved.getId()).isNotNull();
            assertThat(service.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should not save invalid movie")
        void testSaveInvalid() {
            Movie movie = new Movie("", "Description");

            assertThatThrownBy(() -> service.save(movie))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(service.count()).isZero();
        }

        @Test
        @DisplayName("Should find movie by ID")
        void testFindById() {
            Movie movie = service.save(new Movie("Test", "Desc"));

            assertThat(service.findById(movie.getId())).isPresent();
            assertThat(service.findById(999L)).isEmpty();
        }

        @Test
        @DisplayName("Should delete movie by ID")
        void testDeleteById() {
            Movie movie = service.save(new Movie("To Delete", "Desc"));
            Long id = movie.getId();

            service.deleteById(id);

            assertThat(service.findById(id)).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent movie")
        void testDeleteNonExistent() {
            assertThatThrownBy(() -> service.deleteById(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("Collection Operations Tests")
    class CollectionTests {

        @BeforeEach
        void setUpTestData() {
            Movie movie1 = new Movie("Action Movie 1", "Blockbuster action");
            movie1.setCategory("Action");
            movie1.setStatus(Movie.Status.ACTIVE);
            movie1.addTag("blockbuster");
            movie1.addTag("hero");

            Movie movie2 = new Movie("Romantic Movie", "Love story");
            movie2.setCategory("Romance");
            movie2.setStatus(Movie.Status.ACTIVE);
            movie2.addTag("love");

            Movie movie3 = new Movie("Action Movie 2", "Sequel");
            movie3.setCategory("Action");
            movie3.setStatus(Movie.Status.INACTIVE);
            movie3.addTag("sequel");

            Movie movie4 = new Movie("Archived Movie", "Classic");
            movie4.setCategory("Action");
            movie4.setStatus(Movie.Status.ARCHIVED);
            movie4.addTag("blockbuster");

            service.save(movie1);
            service.save(movie2);
            service.save(movie3);
            service.save(movie4);
        }

        @Test
        @DisplayName("Should group movies by category")
        void testGroupByCategory() {
            Map<String, List<Movie>> grouped = service.groupByCategory();

            assertThat(grouped).hasSize(2);
            assertThat(grouped.get("Action")).hasSize(3);
            assertThat(grouped.get("Romance")).hasSize(1);
        }

        @Test
        @DisplayName("Should get all unique tags")
        void testGetAllUniqueTags() {
            Set<String> tags = service.getAllUniqueTags();

            assertThat(tags).hasSize(4);
            assertThat(tags).containsExactlyInAnyOrder(
                    "blockbuster", "hero", "love", "sequel"
            );
        }

        @Test
        @DisplayName("Should count movies by status")
        void testCountByStatus() {
            Map<Movie.Status, Long> counts = service.countByStatus();

            assertThat(counts).hasSize(3);
            assertThat(counts.get(Movie.Status.ACTIVE)).isEqualTo(2);
            assertThat(counts.get(Movie.Status.INACTIVE)).isEqualTo(1);
            assertThat(counts.get(Movie.Status.ARCHIVED)).isEqualTo(1);
        }

        @Test
        @DisplayName("Should find movies with all specified tags")
        void testFindByAllTags() {
            Movie movie5 = new Movie("Multi-tag Movie", "Multiple tags");
            movie5.addTag("blockbuster");
            movie5.addTag("hero");
            service.save(movie5);

            List<Movie> results = service.findByAllTags(Set.of("blockbuster", "hero"));

            assertThat(results).hasSize(2);
            assertThat(results).extracting(Movie::getTitle)
                    .containsExactlyInAnyOrder("Action Movie 1", "Multi-tag Movie");
        }

        @Test
        @DisplayName("Should find movies with any of specified tags")
        void testFindByAnyTag() {
            List<Movie> results = service.findByAnyTag(Set.of("love", "sequel"));

            assertThat(results).hasSize(2);
            assertThat(results).extracting(Movie::getTitle)
                    .containsExactlyInAnyOrder("Romantic Movie", "Action Movie 2");
        }

        @Test
        @DisplayName("Should get most popular tags")
        void testGetMostPopularTags() {
            List<String> popular = service.getMostPopularTags(2);

            assertThat(popular).hasSize(2);
            assertThat(popular.get(0)).isEqualTo("blockbuster"); // appears twice
        }

        @Test
        @DisplayName("Should search movies by query")
        void testSearch() {
            List<Movie> results = service.search("action");

            assertThat(results).hasSize(3);
            assertThat(results).extracting(Movie::getTitle)
                    .contains("Action Movie 1", "Action Movie 2");
        }

        @Test
        @DisplayName("Should archive inactive movies")
        void testArchiveInactiveMovies() {
            int archived = service.archiveInactiveMovies();

            assertThat(archived).isEqualTo(1);

            List<Movie> inactiveMovies = service.findByStatus(Movie.Status.INACTIVE);
            assertThat(inactiveMovies).isEmpty();

            List<Movie> archivedMovies = service.findByStatus(Movie.Status.ARCHIVED);
            assertThat(archivedMovies).hasSize(2);
        }
    }
}