package com.example.demonet.service;

import com.example.demonet.entity.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodyUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * IGDBService 测试基类。
 *
 * <p>提供 IGDBService 测试的共享 Mock 对象和辅助方法,
 * 避免在每个测试方法中重复 stub 整个 RestClient 链。</p>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * @Test
 * void fetchGameById_success() {
 *     // 只需要 stub 实际会被调用的部分
 *     mockTokenCache("cached-token");
 *     mockRestClientChain(200, gameResponse);
 *
 *     Item result = igdbService.fetchGameById(100);
 *
 *     assertThat(result).isNotNull();
 * }
 * }</pre>
 *
 * <h3>注意:</h3>
 * <p>IGDBService 使用 @RequiredArgsConstructor 生成构造函数,
 * 子类无法直接调用。请在子类的 setUp() 中初始化 igdbService,
 * 并调用辅助方法设置必要的 mock。</p>
 */
@ExtendWith(MockitoExtension.class)
abstract class BaseIGDBServiceTest {

    @Mock
    protected StringRedisTemplate redisTemplate;

    @Mock
    protected ValueOperations<String, String> valueOperations;

    @Mock
    protected RestClient restClient;

    @Mock
    protected RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    protected ResponseSpec responseSpec;

    @Mock
    protected ObjectMapper objectMapper;

    @Mock
    protected JdbcTemplate jdbcTemplate;

    protected IGDBService igdbService;

    /**
     * 模拟 token 缓存命中。
     *
     * <p>默认情况下,所有 IGDBService 方法都会先调用 getAccessToken() 查询 Redis。
     * 此方法模拟缓存命中的场景。</p>
     *
     * @param cachedToken 缓存的 token 值 (传入 null 模拟缓存未命中)
     */
    protected void mockTokenCache(String cachedToken) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("igdb:token")).thenReturn(cachedToken);
    }

    /**
     * 模拟完整的 RestClient 调用链。
     *
     * <p>IGDBService.executeQuery() 的实际调用链:
     * <pre>
     * restClient.post()
     *     .uri(API_BASE + "/games")
     *     .header("Client-ID", ...)
     *     .header("Authorization", ...)
     *     .header("Content-Type", "text/plain")
     *     .body(bodyStr)
     *     .retrieve()
     *     .body(String.class)
     * </pre>
     *
     * <p>此方法自动 stub 整个链并返回指定响应。</p>
     *
     * @param responseBody 模拟的响应体 (如 JSON 字符串)
     */
    protected void mockRestClientChain(String responseBody) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(Object.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(anyString(), any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(responseBody);
    }

    /**
     * 模拟空响应场景。
     *
     * <p>此方法使用 lenient() 模式,因为当 token 缓存命中时,
     * RestClient 调用链可能根本不会被消费。</p>
     *
     * <p>仅在测试"缓存命中且 API 返回空"场景时使用此方法。</p>
     */
    protected void mockEmptyResponse() {
        // 空响应测试通常不关心 RestClient 链,
        // 使用 lenient() 避免 UnnecessaryStubbingException
        lenient().when(restClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.body(any(Object.class))).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.header(anyString(), any())).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.body(String.class)).thenReturn("[]");
    }
}
