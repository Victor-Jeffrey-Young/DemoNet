package com.example.demonet.test;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * 测试认证工具类。
 *
 * <p>提供标准的认证 mock 对象,用于 Standalone MockMvc 测试。
 * 避免在每个测试类中重复创建认证 mock 逻辑。</p>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 在 @BeforeEach 中
 * auth = TestAuthentication.withUserId(1L);
 *
 * // 在测试方法中
 * mockMvc.perform(get("/api/endpoint").principal(auth))
 * }</pre>
 *
 * <h3>设计原则:</h3>
 * <ul>
 *   <li>使用 lenient() 模式,避免严格模式下因非必经 stub 导致的测试失败</li>
 *   <li>默认返回 userId,与项目所有 Controller 的认证模式一致</li>
 *   <li>可扩展支持角色和权限</li>
 * </ul>
 */
public class TestAuthentication {

    /**
     * 创建带有指定 userId 的认证 mock 对象。
     *
     * <p>使用 lenient() 模式,适用于 Standalone MockMvc 测试场景。
     * 返回的 Authentication 对象可通过 .getPrincipal() 获取 userId。</p>
     *
     * @param userId 用户 ID
     * @return mock Authentication 对象
     */
    public static Authentication withUserId(Long userId) {
        Authentication auth = mock(Authentication.class, withSettings().lenient());
        when(auth.getPrincipal()).thenReturn((Object) userId);
        return auth;
    }

    /**
     * 私有构造方法,禁止实例化工具类。
     */
    private TestAuthentication() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
}
