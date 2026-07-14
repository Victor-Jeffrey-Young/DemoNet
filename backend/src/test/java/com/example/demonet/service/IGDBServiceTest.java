package com.example.demonet.service;

import com.example.demonet.entity.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
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
 * 继承 BaseIGDBServiceTest 提供共享 Mock 对象和辅助方法。
 *
 * <h3>测试策略:</h3>
 * <ul>
 *   <li>缓存命中的简单场景: 使用 mockTokenCache + mockRestClientChain</li>
 *   <li>空响应场景: 使用 mockEmptyResponse (宽松模式,避免不必要的 stub 异常)</li>
 * </ul>
 *
 * <p>注意:searchGames_returnsResults 测试完整 OAuth 流程(缓存未命中),
 * 这是唯一需要复杂 stub 的测试方法。</p>
 */
@ExtendWith(MockitoExtension.class)
class IGDBServiceTest extends BaseIGDBServiceTest {

    @BeforeEach
    void setUp() {
        // 手动初始化 igdbService (因为 @RequiredArgsConstructor 无法在父类 Mockito 环境中自动工作)
        igdbService = new IGDBService(restClient, redisTemplate, jdbcTemplate);
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchGames_returnsResults() {
        // 模拟缓存未命中,触发 OAuth 流程
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("igdb:token")).thenReturn(null);

        // Mock DB credentials
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

        // 完整 mock RestClient 链 (OAuth + executeQuery)
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(Object.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(anyString(), any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        // OAuth token 请求返回 ParameterizedTypeReference
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(tokenResponse);
        // executeQuery 请求返回 String
        when(responseSpec.body(eq(String.class))).thenReturn(gameResponse);

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
        // 模拟缓存未命中,但 DB 查询失败(credentials 为空),直接返回空列表
        mockTokenCache(null);

        List<Item> results = igdbService.searchGames("Test", 10);

        assertThat(results).isEmpty();
    }

    @Test
    void searchGames_usesCachedToken() {
        mockTokenCache("cached-token");
        mockRestClientChain("[]");

        List<Item> results = igdbService.searchGames("Test", 10);

        assertThat(results).isEmpty();
        // 搜索模式不发送 body(),验证 body() 未被调用
        verify(requestBodyUriSpec, never()).body(any());
    }

    @Test
    void fetchGameById_success() {
        mockTokenCache("cached-token");

        String gameResponse = "[{\"id\":100,\"name\":\"Specific Game\",\"summary\":\"Detailed.\","
                + "\"cover\":{\"url\":\"//images.igdb.com/specific.jpg\"},"
                + "\"first_release_date\":1600000000,"
                + "\"rating\":90.0,"
                + "\"involved_companies\":[],"
                + "\"genres\":[],\"platforms\":[],\"themes\":[],\"game_modes\":[],"
                + "\"screenshots\":[],\"videos\":[],\"websites\":[],\"similar_games\":[]}]";

        mockRestClientChain(gameResponse);

        Item result = igdbService.fetchGameById(100);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Specific Game");
        assertThat(result.getSlug()).isEqualTo("igdb-100");
    }

    @Test
    void fetchGameById_emptyResponse() {
        mockTokenCache("cached-token");

        // 空响应场景:RestClient 调用链可能根本不会被消费
        // 使用 mockEmptyResponse() 提供宽松的 stub,避免 UnnecessaryStubbingException
        mockEmptyResponse();

        Item result = igdbService.fetchGameById(100);

        assertThat(result).isNull();
    }
}
