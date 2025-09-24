package edu.trincoll.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class MovieModelTest {

    @Test
    void gettersSettersAndToString() {
        Movie m = new Movie();
        m.setId(1L);
        m.setTitle("Inception");
        m.setDescription("Dream heist");
        m.setGenre("Sci-Fi");
        m.setDirector("Christopher Nolan");
        m.setReleaseDate(LocalDate.of(2010, 7, 16));
        m.setRating(9.0);
        m.setFavorite(true);
        m.setStatus(Movie.Status.RELEASED);
        m.addTag("classic");
        m.addTag("mind-bending");

        assertEquals(1L, m.getId());
        assertEquals("Inception", m.getTitle());
        assertEquals("Sci-Fi", m.getGenre());
        assertEquals("Christopher Nolan", m.getDirector());
        assertEquals(LocalDate.of(2010, 7, 16), m.getReleaseDate());
        assertEquals(9.0, m.getRating(), 0.0001);
        assertTrue(m.isFavorite());
        assertEquals(Movie.Status.RELEASED, m.getStatus());
        assertTrue(m.getTags().contains("classic"));

        String s = m.toString();
        assertTrue(s.contains("Inception"));
        assertTrue(s.contains("Sci-Fi"));
    }

    @Test
    void setTags_replacesListDefensively() {
        Movie m = new Movie();
        java.util.List<String> tags = new java.util.ArrayList<>();
        tags.add("a"); tags.add("b");
        m.setTags(tags);
        assertEquals(2, m.getTags().size());
        tags.add("c"); // should NOT affect m
        assertEquals(2, m.getTags().size());
    }
}

