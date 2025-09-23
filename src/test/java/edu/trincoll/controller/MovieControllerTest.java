package edu.trincoll.controller;

import edu.trincoll.model.Movie;
import edu.trincoll.service.MovieService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private MovieService service;

    // --- Helper to allow multiple acceptable status codes (replaces anyOf(ResultMatcher...)) ---
    private static ResultMatcher statusIsOneOf(int... codes) {
        return result -> {
            int actual = result.getResponse().getStatus();
            for (int c : codes) if (actual == c) return;
            throw new AssertionError("Expected one of " + java.util.Arrays.toString(codes) + " but was " + actual);
        };
    }

    @Test
    void getAllMovies_returns200AndBody() throws Exception {
        Movie m = new Movie("Inception", "Sci-Fi", "Nolan", LocalDate.of(2010,7,16), 9.0);
        Mockito.when(service.findAll()).thenReturn(List.of(m));

        mockMvc.perform(get("/api/movies"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].title").value("Inception"))
               .andExpect(jsonPath("$[0].genre").value("Sci-Fi"));
    }

    @Test
    void getMovieById_returns200Or404() throws Exception {
        Movie m = new Movie("Dune", "Sci-Fi", "Villeneuve", LocalDate.of(2021,10,22), 8.5);
        Mockito.when(service.findById(1L)).thenReturn(java.util.Optional.of(m));
        Mockito.when(service.findById(9999L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/movies/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.title").value("Dune"));

        mockMvc.perform(get("/api/movies/9999"))
               .andExpect(status().isNotFound());
    }

    @Test
    void addMovie_returns201AndLocation() throws Exception {
        String payload = """
          {"title":"Interstellar","genre":"Sci-Fi","director":"Nolan","rating":9.0}
        """;
        Movie saved = new Movie("Interstellar", "Sci-Fi");
        saved.setId(42L);

        Mockito.when(service.save(any(Movie.class))).thenReturn(saved);

        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isCreated())
               .andExpect(header().string("Location", "/api/movies/42"))
               .andExpect(jsonPath("$.title").value("Interstellar"));
    }

    @Test
    void updateMovie_returns200WhenExists_else404() throws Exception {
        String payload = """
          {"title":"Inception (Updated)","genre":"Sci-Fi","rating":9.1}
        """;
        Mockito.when(service.existsById(5L)).thenReturn(true);
        Movie updated = new Movie("Inception (Updated)", "Sci-Fi");
        updated.setId(5L);
        Mockito.when(service.save(any(Movie.class))).thenReturn(updated);

        mockMvc.perform(put("/api/movies/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.title").value("Inception (Updated)"));

        Mockito.when(service.existsById(6L)).thenReturn(false);
        mockMvc.perform(put("/api/movies/6")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
               .andExpect(status().isNotFound());
    }

    @Test
    void deleteMovie_returns204Or404() throws Exception {
        Mockito.doNothing().when(service).deleteById(7L);
        Mockito.doThrow(new IllegalArgumentException("not found")).when(service).deleteById(9999L);

        mockMvc.perform(delete("/api/movies/7"))
               .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/movies/9999"))
               .andExpect(statusIsOneOf(404)); // uses helper
    }

    @Test
    void getMoviesByGenre_returnsList() throws Exception {
        Mockito.when(service.findByGenre("Drama"))
               .thenReturn(List.of(new Movie("Whiplash", "Drama")));

        mockMvc.perform(get("/api/movies/genre/Drama"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].genre").value("Drama"));
    }

    @Test
    void searchMoviesByTitle_returnsList() throws Exception {
        Mockito.when(service.searchByTitle("star"))
               .thenReturn(List.of(new Movie("Star Wars", "Sci-Fi"), new Movie("A Star Is Born", "Drama")));

        mockMvc.perform(get("/api/movies/search").param("title", "star"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.[*].title", containsInAnyOrder("Star Wars", "A Star Is Born")));
    }

    @Test
    void getAllGenres_returnsSet() throws Exception {
        Mockito.when(service.getAllUniqueGenres()).thenReturn(Set.of("Sci-Fi", "Drama"));

        mockMvc.perform(get("/api/movies/genres"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.[*]", containsInAnyOrder("Sci-Fi", "Drama")));
    }

    @Test
    void getMoviesGroupedByGenre_returnsMap() throws Exception {
        Mockito.when(service.groupByGenre()).thenReturn(Map.of(
                "Sci-Fi", List.of(new Movie("Dune", "Sci-Fi")),
                "Drama",  List.of(new Movie("Oppenheimer", "Drama"))
        ));

        mockMvc.perform(get("/api/movies/grouped"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.['Sci-Fi'][0].title").value("Dune"))
               .andExpect(jsonPath("$.['Drama'][0].title").value("Oppenheimer"));
    }
}


