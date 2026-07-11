package com.example.demonet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SteamGridDBService 外部 API Mock 测试。
 * mock RestClient 的 2 步 API 调用链（游戏解析 + 封面查询）。
 */
@ExtendWith(MockitoExtension.class)
class SteamGridDBServiceTest {

    @InjectMocks
    private SteamGridDBService steamGridDBService;

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
        when(jdbcTemplate.queryForObject(
                eq("SELECT setting_value FROM app_settings WHERE setting_key = 'STEAMGRIDDB_API_KEY'"),
                eq(String.class)))
                .thenReturn("test-sgdb-key");
    }

    @SuppressWarnings("unchecked")
    @Test
    void findPosterUrl_success() {
        Map<String, Object> gameResp = Map.of(
                "success", true,
                "data", Map.of("id", 12345)
        );
        Map<String, Object> gridResp = Map.of(
                "success", true,
                "data", List.of(Map.of("url", "https://cdn.steamgriddb.com/cover.jpg"))
        );

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString(), isA(String.class));
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(gameResp).doReturn(gridResp)
                .when(responseSpec).body(any(ParameterizedTypeReference.class));

        String url = steamGridDBService.findPosterUrl(730L);

        assertThat(url).isEqualTo("https://cdn.steamgriddb.com/cover.jpg");
        verify(restClient, times(2)).get();
    }

    @SuppressWarnings("unchecked")
    @Test
    void findPosterUrl_gameNotFound() {
        Map<String, Object> gameResp = Map.of(
                "success", false,
                "data", null
        );

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString(), isA(String.class));
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(gameResp).when(responseSpec).body(any(ParameterizedTypeReference.class));

        String url = steamGridDBService.findPosterUrl(730L);

        assertThat(url).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    void findPosterUrl_noGrids() {
        Map<String, Object> gameResp = Map.of(
                "success", true,
                "data", Map.of("id", 12345)
        );
        Map<String, Object> gridResp = Map.of(
                "success", true,
                "data", List.of()
        );

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString(), isA(String.class));
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doReturn(gameResp).doReturn(gridResp)
                .when(responseSpec).body(any(ParameterizedTypeReference.class));

        String url = steamGridDBService.findPosterUrl(730L);

        assertThat(url).isNull();
    }

    @Test
    void findPosterUrl_emptyApiKey() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT setting_value FROM app_settings WHERE setting_key = 'STEAMGRIDDB_API_KEY'"),
                eq(String.class)))
                .thenReturn("");

        String url = steamGridDBService.findPosterUrl(730L);

        assertThat(url).isNull();
        verify(restClient, never()).get();
    }

    @SuppressWarnings("unchecked")
    @Test
    void findPosterUrl_apiThrows() {
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString(), isA(String.class));
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestHeadersUriSpec).retrieve();
        doThrow(new RuntimeException("API error")).when(responseSpec).body(any(ParameterizedTypeReference.class));

        String url = steamGridDBService.findPosterUrl(730L);

        assertThat(url).isNull();
    }
}
