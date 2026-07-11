package com.example.demonet.service;

import com.example.demonet.entity.Item;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * IGDBService 外部 API Mock 测试。
 * mock OAuth token 获取和 API 查询的完整调用链。
 * 注意：restClient.post() 返回 RequestBodyUriSpec（不是 RequestHeadersUriSpec）。
 */
@ExtendWith(MockitoExtension.class)
class IGDBServiceTest {

    @InjectMocks
    private IGDBService igdbService;

    @Mock
    private RestClient restClient;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;
    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // jdbcTemplate stubs moved to individual tests that need them
        // to avoid Mockito UnnecessaryStubbingException.
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchGames_returnsResults() {
        when(valueOperations.get("igdb:token")).thenReturn(null);
        // Mock DB credentials for OAuth flow
        when(jdbcTemplate.queryForObject(
                eq("SELECT setting_value FROM app_settings WHERE setting_key = 'IGDB_CLIENT_ID'"),
                eq(String.class)))
                .thenReturn("test-client-id");
        when(jdbcTemplate.queryForObject(
                eq("SELECT setting_value FROM app_settings WHERE setting_key = 'IGDB_CLIENT_SECRET'"),
                eq(String.class)))
                .thenReturn("test-client-secret");

        Map<String, Object> tokenResponse = Map.of(
                "access_token", "test-access-token",
                "expires_in", 3600
        );

        String gameResponse = "[{\"id\":1,\"name\":\"Test Game\",\"summary\":\"A test game.\","
                + "\"cover\":{\"url\":\"//images.igdb.com/cover.jpg\"},"
                + "\"first_release_date\":1700000000,"
                + "\"rating\":85.0,\"total_rating_count\":100,"
                + "\"genres\":[{\"name\":\"Action\"},{\"name\":\"RPG\"}],"
                + "\"platforms\":[{\"name\":\"PC\"}],"
                + "\"themes\":[{\"name\":\"Fantasy\"}],"
                + "\"game_modes\":[{\"name\":\"Single player\"}],"
                + "\"involved_companies\":[{\"company\":{\"name\":\"Test Studio\"},\"developer\":true,\"publisher\":true}],"
                + "\"screenshots\":[],\"videos\":[],\"websites\":[],\"similar_games\":[]}]";

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).body(any());
        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodyUriSpec).retrieve();
        doReturn(tokenResponse).when(responseSpec).body(any(ParameterizedTypeReference.class));
        doReturn(gameResponse).when(responseSpec).body(String.class);

        List<Item> results = igdbService.searchGames("Test", 10);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Test Game");
        assertThat(results.get(0).getType()).isEqualTo("game");
        assertThat(results.get(0).getSource()).isEqualTo("igdb");
        assertThat(results.get(0).getExternalId()).isEqualTo("1");
        assertThat(results.get(0).getInfoJson()).contains("Test Studio");
        assertThat(results.get(0).getInfoJson()).contains("Action");
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchGames_noToken() {
        when(valueOperations.get("igdb:token")).thenReturn(null);

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).body(any());
        doReturn(responseSpec).when(requestBodyUriSpec).retrieve();
        doReturn(null).when(responseSpec).body(any(ParameterizedTypeReference.class));

        List<Item> results = igdbService.searchGames("Test", 10);

        assertThat(results).isEmpty();
    }

    @Test
    void searchGames_usesCachedToken() {
        when(valueOperations.get("igdb:token")).thenReturn("cached-token");

        String gameResponse = "[]";
        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodyUriSpec).retrieve();
        doReturn(gameResponse).when(responseSpec).body(String.class);

        List<Item> results = igdbService.searchGames("Test", 10);

        assertThat(results).isEmpty();
        verify(requestBodyUriSpec, never()).body(any());
    }

    @Test
    void fetchGameById_success() {
        when(valueOperations.get("igdb:token")).thenReturn("cached-token");

        String gameResponse = "[{\"id\":100,\"name\":\"Specific Game\",\"summary\":\"Detailed.\","
                + "\"cover\":{\"url\":\"//images.igdb.com/specific.jpg\"},"
                + "\"first_release_date\":1600000000,"
                + "\"rating\":90.0,"
                + "\"involved_companies\":[],"
                + "\"genres\":[],\"platforms\":[],\"themes\":[],\"game_modes\":[],"
                + "\"screenshots\":[],\"videos\":[],\"websites\":[],\"similar_games\":[]}]";

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodyUriSpec).retrieve();
        doReturn(gameResponse).when(responseSpec).body(String.class);

        Item result = igdbService.fetchGameById(100);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Specific Game");
        assertThat(result.getSlug()).isEqualTo("igdb-100");
    }

    @Test
    void fetchGameById_emptyResponse() {
        when(valueOperations.get("igdb:token")).thenReturn("cached-token");

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).header(anyString(), anyString());
        doReturn(responseSpec).when(requestBodyUriSpec).retrieve();
        doReturn("[]").when(responseSpec).body(String.class);

        Item result = igdbService.fetchGameById(100);

        assertThat(result).isNull();
    }
}
