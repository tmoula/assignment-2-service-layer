package edu.trincoll.service;

import edu.trincoll.model.Movie;
import edu.trincoll.repository.InMemoryMovieRepository;
import edu.trincoll.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class MovieServiceCoverageTest {

    private MovieService service;
    private MovieRepository repository;

    @BeforeEach
    void setup() {
        repository = new InMemoryMovieRepository();
        service = new MovieService(repository);
        repository.deleteAll();
    }

    /* ---------------- Validation ---------------- */

    @Test
    @DisplayName("validateEntity: reject empty genre if provided")
    void validateEntity_rejectsEmptyGenre() {
        Movie m = new Movie("Has Title", null);
        m.setGenre("   "); // triggers: "Genre cannot be empty if provided"
        assertThatThrownBy(() -> service.validateEntity(m))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Genre cannot be empty");
    }

    /* ---------------- Search (broad) ---------------- */

    @Test
    @DisplayName("search: matches title/description/genre/tags (case-insensitive)")
    void search_matchesAllFields() {
        Movie a = new Movie("Action Hero", "Drama"); // title hit
        a.setDescription("Not really action");       // desc hit
        a.addTag("epic");

        Movie b = new Movie("Quiet Movie", "Action"); // genre hit
        b.addTag("classic");

        Movie c = new Movie("Tag Only", "Indie");
        c.addTag("AcTiOn-packed"); // tag hit via contains + case-insensitive

        Movie d = new Movie("Irrelevant", "Comedy");

        service.save(a);
        service.save(b);
        service.save(c);
        service.save(d);

        List<Movie> results = service.search("action");
        assertThat(results).extracting(Movie::getTitle)
                .contains("Action Hero", "Quiet Movie", "Tag Only")
                .doesNotContain("Irrelevant");
    }

    @Test
    @DisplayName("search: blank/null query returns empty")
    void search_blank() {
        assertThat(service.search("")).isEmpty();
        assertThat(service.search("   ")).isEmpty();
        assertThat(service.search(null)).isEmpty();
    }

    /* ---------------- Genre utilities ---------------- */

    @Test
    @DisplayName("groupByGenre: ignores null genres")
    void groupByGenre_ignoresNull() {
        Movie m1 = new Movie("A", "Action");
        Movie m2 = new Movie("B", "Action");
        Movie m3 = new Movie(); m3.setTitle("C"); m3.setGenre(null); // null genre -> ignored

        service.save(m1); service.save(m2); service.save(m3);

        Map<String, List<Movie>> grouped = service.groupByGenre();
        assertThat(grouped).hasSize(1);
        assertThat(grouped.get("Action")).hasSize(2);
    }

    @Test
    @DisplayName("getAllUniqueGenres: distinct, non-blank only (null allowed, blank rejected by validation)")
    void getAllUniqueGenres_distinct() {
        service.save(new Movie("T1", "Action"));
        service.save(new Movie("T2", "Action"));
        service.save(new Movie("T3", "Drama"));
        // blank genre would be rejected by validation; use null instead
        Movie nullGenre = new Movie(); nullGenre.setTitle("T4"); nullGenre.setGenre(null);
        service.save(nullGenre);

        Set<String> genres = service.getAllUniqueGenres();
        assertThat(genres).containsExactlyInAnyOrder("Action", "Drama");
    }

    /* ---------------- Status utilities ---------------- */

    @Test
    @DisplayName("countByStatus: counts only non-null statuses (explicitly null one)")
    void countByStatus_counts() {
        Movie a = new Movie("A", "X"); a.setStatus(Movie.Status.ACTIVE);
        Movie b = new Movie("B", "X"); b.setStatus(Movie.Status.INACTIVE);
        Movie c = new Movie("C", "X"); c.setStatus(Movie.Status.CANCELLED);
        Movie d = new Movie("D", "X"); d.setStatus(Movie.Status.ARCHIVED);
        Movie e = new Movie("E", "X"); e.setStatus(null); // IMPORTANT: default is ACTIVE, so force null

        service.save(a); service.save(b); service.save(c); service.save(d); service.save(e);

        Map<Movie.Status, Long> counts = service.countByStatus();
        assertThat(counts.get(Movie.Status.ACTIVE)).isEqualTo(1);
        assertThat(counts.get(Movie.Status.INACTIVE)).isEqualTo(1);
        assertThat(counts.get(Movie.Status.CANCELLED)).isEqualTo(1);
        assertThat(counts.get(Movie.Status.ARCHIVED)).isEqualTo(1);
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        assertThat(total).isEqualTo(4); // null status excluded
    }

    @Test
    @DisplayName("findByStatus(null) returns empty")
    void findByStatus_null() {
        assertThat(service.findByStatus(null)).isEmpty();
    }

    /* ---------------- Tags utilities ---------------- */

    @Test
    @DisplayName("findByAllTags: empty/null input returns empty")
    void findByAllTags_empty() {
        assertThat(service.findByAllTags(Set.of())).isEmpty();
        assertThat(service.findByAllTags(null)).isEmpty();
    }

    @Test
    @DisplayName("findByAnyTag: empty/null input returns empty")
    void findByAnyTag_empty() {
        assertThat(service.findByAnyTag(Set.of())).isEmpty();
        assertThat(service.findByAnyTag(null)).isEmpty();
    }

    @Test
    @DisplayName("getMostPopularTags: limit <= 0 returns empty")
    void getMostPopularTags_limitZero() {
        assertThat(service.getMostPopularTags(0)).isEmpty();
        assertThat(service.getMostPopularTags(-1)).isEmpty();
    }

    /* ---------------- update helper & stats ---------------- */

    @Test
    @DisplayName("update: partial fields; rating unchanged when payload rating == 0.0")
    void update_partial_noRatingChangeOnZero() {
        Movie existing = new Movie("Old", "Drama", "Someone", LocalDate.of(2020,1,1), 6.5);
        existing.setDescription("old");
        existing.setStatus(Movie.Status.ACTIVE);
        existing = service.save(existing);

        Movie payload = new Movie();
        payload.setTitle("New");
        payload.setGenre("Action");
        payload.setDescription("new");
        payload.setRating(0.0); // should NOT update rating

        Movie updated = service.update(existing.getId(), payload);

        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(updated.getGenre()).isEqualTo("Action");
        assertThat(updated.getDescription()).isEqualTo("new");
        assertThat(updated.getRating()).isEqualTo(6.5); // unchanged

        // now actually change rating
        Movie payload2 = new Movie();
        payload2.setRating(9.1);
        Movie updated2 = service.update(existing.getId(), payload2);
        assertThat(updated2.getRating()).isEqualTo(9.1);
    }

    @Test
    @DisplayName("getMovieStatistics: totals, genreCounts, statusCounts, averages")
    void getMovieStatistics_ok() {
        Movie a = new Movie("A", "Action"); a.setStatus(Movie.Status.ACTIVE); a.addTag("one");
        Movie b = new Movie("B", "Action"); b.setStatus(Movie.Status.INACTIVE); b.addTag("one"); b.addTag("two");
        Movie c = new Movie("C", "Drama");  c.setStatus(Movie.Status.CANCELLED);

        service.save(a); service.save(b); service.save(c);

        Map<String, Object> stats = service.getMovieStatistics();

        assertThat(stats.get("totalMovies")).isEqualTo(3);
        @SuppressWarnings("unchecked")
        Map<String, Long> genreCounts = (Map<String, Long>) stats.get("genreCounts");
        assertThat(genreCounts.get("Action")).isEqualTo(2);
        assertThat(genreCounts.get("Drama")).isEqualTo(1);

        @SuppressWarnings("unchecked")
        Map<Movie.Status, Long> statusCounts = (Map<Movie.Status, Long>) stats.get("statusCounts");
        assertThat(statusCounts.get(Movie.Status.ACTIVE)).isEqualTo(1);
        assertThat(statusCounts.get(Movie.Status.INACTIVE)).isEqualTo(1);
        assertThat(statusCounts.get(Movie.Status.CANCELLED)).isEqualTo(1);

        assertThat((Integer) stats.get("totalUniqueTags")).isEqualTo(2);
        double avg = (double) stats.get("averageTagsPerMovie");
        assertThat(avg).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("archiveInactiveMovies: moves INACTIVE → ARCHIVED and counts")
    void archiveInactiveMovies_ok() {
        Movie active = new Movie("A", "X");   active.setStatus(Movie.Status.ACTIVE);
        Movie inactive = new Movie("B", "X"); inactive.setStatus(Movie.Status.INACTIVE);

        service.save(active);
        service.save(inactive);

        int archived = service.archiveInactiveMovies();
        assertThat(archived).isEqualTo(1);

        assertThat(service.findByStatus(Movie.Status.INACTIVE)).isEmpty();
        assertThat(service.findByStatus(Movie.Status.ARCHIVED)).extracting(Movie::getTitle)
                .containsExactly("B");
    }

    /* ---------------- quick extra edges to lift coverage ---------------- */

    @Test
    @DisplayName("findByGenre(null) returns empty")
    void findByGenre_null() {
        assertThat(service.findByGenre(null)).isEmpty();
    }

    @Test
    @DisplayName("searchByTitle: blank returns empty; case-insensitive works")
    void searchByTitle_cases() {
        assertThat(service.searchByTitle("")).isEmpty();
        service.save(new Movie("InCePtIoN", "Sci-Fi"));
        assertThat(service.searchByTitle("incept")).extracting(Movie::getTitle)
                .containsExactly("InCePtIoN");
    }
}
