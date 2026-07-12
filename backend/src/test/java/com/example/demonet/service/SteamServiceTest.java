package com.example.demonet.service;

import com.example.demonet.entity.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
 * SteamService 外部 API Mock 测试。
 * mock RestClient 调用链，模拟 Steam Store API 响应。
 */
@ExtendWith(MockitoExtension.class)
class SteamServiceTest {

    @InjectMocks
    private SteamService steamService;

    @Mock
    private RestClient restClient;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SteamGridDBService steamGridDBService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @SuppressWarnings("unchecked")
    @Test
    void fetchAppDetail_success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("steam:poster:730")).thenReturn(null);

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Counter-Strike 2");
        data.put("type", "game");
        data.put("header_image", "https://cdn.steamstatic.com/header.jpg");
        data.put("short_description", "A competitive FPS.");
        data.put("developers", new ArrayList<>(List.of("Valve")));
        data.put("publishers", new ArrayList<>(List.of("Valve")));
        data.put("genres", new ArrayList<>(List.of(Map.of("description", "Action"), Map.of("description", "FPS"))));
        data.put("windows", true);
        data.put("mac", true);
        data.put("linux", true);
        data.put("is_free", true);
        data.put("recommendations", Map.of("total", 50000));
        data.put("release_date", Map.of("date", "Aug 21, 2012"));
        data.put("supported_languages", "English<strong>*</strong>, French, Simplified Chinese");
        data.put("price_overview", Map.of("currency", "USD", "final", 0));
        data.put("pc_requirements", Map.of("minimum", "<strong>Min:</strong><br>OS: Windows 10"));
        data.put("screenshots", new ArrayList<>(List.of(Map.of("path_full", "https://cdn/screenshot1.jpg"))));
        data.put("movies", new ArrayList<>());
        data.put("dlc", new ArrayList<>());
        data.put("categories", new ArrayList<>(List.of(
                Map.of("description", "Multi-player"), Map.of("description", "Co-op")
        )));

        Map<String, Object> appResponse = Map.of(
                "730", Map.of("success", true, "data", data)
        );

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(appResponse).when(responseSpec).body(any(ParameterizedTypeReference.class));

        when(steamGridDBService.findPosterUrl(730L)).thenReturn(null);

        Item result = steamService.fetchAppDetail(730L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Counter-Strike 2");
        assertThat(result.getSlug()).isEqualTo("steam-730");
        assertThat(result.getType()).isEqualTo("game");
        assertThat(result.getSource()).isEqualTo("steam");
        assertThat(result.getExternalId()).isEqualTo("730");
        assertThat(result.getExternalLink()).contains("store.steampowered.com/app/730");
        assertThat(result.getCoverUrl()).isEqualTo("https://cdn.steamstatic.com/header.jpg");
        assertThat(result.getDescription()).isEqualTo("A competitive FPS.");
        assertThat(result.getRecommendations()).isEqualTo(50000);
        assertThat(result.getInfoJson()).contains("Valve");
        assertThat(result.getInfoJson()).contains("Action");
        assertThat(result.getInfoJson()).contains("PC");
        assertThat(result.getInfoJson()).contains("true");
    }

    @SuppressWarnings("unchecked")
    @Test
    void fetchAppDetail_notFound() {
        Map<String, Object> appResponse = Map.of(
                "99999", Map.of("success", false)
        );

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(appResponse).when(responseSpec).body(any(ParameterizedTypeReference.class));

        Item result = steamService.fetchAppDetail(99999L);

        assertThat(result).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    void fetchAppDetail_apiThrows() {
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doThrow(new RuntimeException("Timeout")).when(responseSpec).body(any(ParameterizedTypeReference.class));

        assertThatThrownBy(() -> steamService.fetchAppDetail(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Timeout");
    }

    @SuppressWarnings("unchecked")
    @Test
    void fetchByAppIds_multiple() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Team Fortress 2");
        data.put("type", "game");
        data.put("header_image", "https://cdn/tf2.jpg");
        data.put("short_description", "Class-based shooter.");
        data.put("is_free", true);
        data.put("developers", new ArrayList<>(List.of("Valve")));
        data.put("publishers", new ArrayList<>(List.of("Valve")));
        data.put("genres", new ArrayList<>());
        data.put("windows", true);
        data.put("mac", true);
        data.put("linux", true);

        Map<String, Object> response1 = Map.of(
                "440", Map.of("success", true, "data", data)
        );

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString());
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(response1).when(responseSpec).body(any(ParameterizedTypeReference.class));

        List<Item> results = steamService.fetchByAppIds(List.of(440L));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Team Fortress 2");
    }
}
