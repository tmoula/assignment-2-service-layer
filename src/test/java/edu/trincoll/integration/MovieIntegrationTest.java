package edu.trincoll.integration;

import edu.trincoll.model.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MovieIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deleteMovie_allows204Or404() throws Exception {
        // Example: when deleting an ID that may or may not exist, your API may return 204 or 404.
        mockMvc.perform(delete("/api/movies/{id}", 9999L))
                // REPLACEMENT for: anyOf(status().isNoContent(), status().isNotFound())
                .andExpect(statusIsOneOf(204, 404));
    }

    @Test
    void getMovieById_allows200Or404() throws Exception {
        mockMvc.perform(get("/api/movies/{id}", 1L))
                // REPLACEMENT for: anyOf(status().isOk(), status().isNotFound())
                .andExpect(statusIsOneOf(200, 404));
    }

    @Test
    void createMovie_returns201() throws Exception {
        String payload = """
            {
              "title": "Inception",
              "genre": "Sci-Fi",
              "director": "Christopher Nolan",
              "rating": 9.0
            }
            """;

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("Inception"))
                .andExpect(jsonPath("$.genre").value("Sci-Fi"));
    }

    // --- Helper to allow multiple acceptable HTTP codes in a single expectation ---
    private static ResultMatcher statusIsOneOf(int... codes) {
        return result -> {
            int actual = result.getResponse().getStatus();
            for (int expected : codes) {
                if (actual == expected) return;
            }
            throw new AssertionError(
                    "Expected status to be one of " + java.util.Arrays.toString(codes) + " but was " + actual
            );
        };
    }
}
