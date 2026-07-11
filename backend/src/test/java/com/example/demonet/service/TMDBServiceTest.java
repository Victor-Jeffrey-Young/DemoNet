package com.example.demonet.service;

import com.example.demonet.entity.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TMDBService 外部 API Mock 测试。
 * mock RestClient 的调用链，不发起真实 HTTP 请求。
 */
@ExtendWith(MockitoExtension.class)
class TMDBServiceTest {

    @InjectMocks
    private TMDBService tmdbService;

    @Mock
    private RestClient restClient;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        // jdbcTemplate stub moved to individual tests that need it
        // to avoid UnnecessaryStubbingException (searchMovies_emptyApiKey overrides it).
    }

    @SuppressWarnings("unchecked")
    @Test
    void fetchMovieDetail_success() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT setting_value FROM app_settings WHERE setting_key = 'TMDB_API_KEY'"),
                eq(String.class)))
                .thenReturn("test-api-key");
        Map<String, Object> tmdbResponse = new HashMap<>();
        tmdbResponse.put("id", 12345);
        tmdbResponse.put("title", "Test Movie");
        tmdbResponse.put("overview", "A test movie description.");
        tmdbResponse.put("poster_path", "/test.jpg");
        tmdbResponse.put("backdrop_path", "/backdrop.jpg");
        tmdbResponse.put("release_date", "2024-01-15");
        tmdbResponse.put("runtime", 120);
        tmdbResponse.put("genres", List.of(Map.of("name", "Action"), Map.of("name", "Sci-Fi")));
        tmdbResponse.put("videos", Map.of("results", List.of()));
        tmdbResponse.put("credits", Map.of("crew", List.of(
                Map.of("job", "Director", "name", "Test Director")
        )));

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(tmdbResponse).when(responseSpec).body(any(ParameterizedTypeReference.class));

        Item result = tmdbService.fetchMovieDetail(12345);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Movie");
        assertThat(result.getSlug()).isEqualTo("tmdb-12345");
        assertThat(result.getType()).isEqualTo("movie");
        assertThat(result.getSource()).isEqualTo("tmdb");
        assertThat(result.getExternalId()).isEqualTo("12345");
        assertThat(result.getExternalLink()).isEqualTo("https://www.themoviedb.org/movie/12345");
        assertThat(result.getDescription()).isEqualTo("A test movie description.");
        assertThat(result.getCoverUrl()).contains("image.tmdb.org");
        assertThat(result.getWideCoverUrl()).contains("image.tmdb.org");
        assertThat(result.getInfoJson()).contains("Test Director");
        assertThat(result.getInfoJson()).contains("Action");
        assertThat(result.getInfoJson()).contains("120min");
        assertThat(result.getInfoJson()).contains("2024");
    }

    @SuppressWarnings("unchecked")
    @Test
    void fetchMovieDetail_apiReturnsNull() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT setting_value FROM app_settings WHERE setting_key = 'TMDB_API_KEY'"),
                eq(String.class)))
                .thenReturn("test-api-key");
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(null).when(responseSpec).body(any(ParameterizedTypeReference.class));

        Item result = tmdbService.fetchMovieDetail(99999);

        assertThat(result).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    void fetchMovieDetail_apiThrows() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT setting_value FROM app_settings WHERE setting_key = 'TMDB_API_KEY'"),
                eq(String.class)))
                .thenReturn("test-api-key");
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doThrow(new RuntimeException("API unreachable")).when(responseSpec).body(any(ParameterizedTypeReference.class));

        Item result = tmdbService.fetchMovieDetail(99999);

        assertThat(result).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchMovies_returnsResults() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT setting_value FROM app_settings WHERE setting_key = 'TMDB_API_KEY'"),
                eq(String.class)))
                .thenReturn("test-api-key");
        Map<String, Object> searchResponse = Map.of(
                "results", List.of(Map.of("id", 12345))
        );
        Map<String, Object> detailResponse = new HashMap<>();
        detailResponse.put("id", 12345);
        detailResponse.put("title", "Test Movie");
        detailResponse.put("overview", "Search result movie.");
        detailResponse.put("poster_path", "/test.jpg");
        detailResponse.put("backdrop_path", "/backdrop.jpg");
        detailResponse.put("release_date", "2024-01-15");
        detailResponse.put("runtime", 90);
        detailResponse.put("genres", List.of());
        detailResponse.put("videos", Map.of("results", List.of()));
        detailResponse.put("credits", Map.of("crew", List.of()));

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(searchResponse).doReturn(detailResponse)
                .when(responseSpec).body(any(ParameterizedTypeReference.class));

        List<Item> results = tmdbService.searchMovies("Test");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Test Movie");
    }

    @Test
    void searchMovies_emptyApiKey() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT setting_value FROM app_settings WHERE setting_key = 'TMDB_API_KEY'"),
                eq(String.class)))
                .thenReturn("");

        List<Item> results = tmdbService.searchMovies("Test");

        assertThat(results).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    void fetchTVDetail_success() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT setting_value FROM app_settings WHERE setting_key = 'TMDB_API_KEY'"),
                eq(String.class)))
                .thenReturn("test-api-key");
        Map<String, Object> tvResponse = new HashMap<>();
        tvResponse.put("id", 56789);
        tvResponse.put("name", "Test TV Show");
        tvResponse.put("overview", "A great TV series.");
        tvResponse.put("poster_path", "/tv.jpg");
        tvResponse.put("backdrop_path", "/tvbackdrop.jpg");
        tvResponse.put("first_air_date", "2023-03-20");
        tvResponse.put("number_of_episodes", 12);
        tvResponse.put("number_of_seasons", 1);
        tvResponse.put("genres", List.of(Map.of("name", "Drama")));
        tvResponse.put("videos", Map.of("results", List.of(
                Map.of("site", "YouTube", "type", "Trailer", "key", "abc123")
        )));
        tvResponse.put("created_by", new ArrayList<>(List.of(Map.of("name", "Test Creator"))));
        tvResponse.put("networks", new ArrayList<>(List.of(Map.of("name", "Test Network"))));
        tvResponse.put("credits", Map.of());

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(tvResponse).when(responseSpec).body(any(ParameterizedTypeReference.class));

        Item result = tmdbService.fetchTVDetail(56789);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test TV Show");
        assertThat(result.getSlug()).isEqualTo("tmdb-tv-56789");
        assertThat(result.getType()).isEqualTo("anime");
        assertThat(result.getSource()).isEqualTo("tmdb");
        assertThat(result.getInfoJson()).contains("Test Creator");
        assertThat(result.getInfoJson()).contains("Test Network");
        assertThat(result.getInfoJson()).contains("12");
        assertThat(result.getInfoJson()).contains("youtube.com");
    }
}
