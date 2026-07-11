package com.example.demonet.service;

import com.example.demonet.common.BusinessException;
import com.example.demonet.common.ConflictException;
import com.example.demonet.entity.AppSetting;
import com.example.demonet.entity.User;
import com.example.demonet.mapper.AppSettingMapper;
import com.example.demonet.mapper.InviteCodeMapper;
import com.example.demonet.mapper.UserMapper;
import com.example.demonet.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuthService 单元测试。
 * 使用 Mockito Mock 所有依赖，不连接数据库/Redis/外部 API。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AppSettingMapper appSettingMapper;
    @Mock private InviteCodeMapper inviteCodeMapper;
    @Mock private RestClient restClient;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ==================== register 测试 ====================

    @Test
    void register_success() {
        when(appSettingMapper.selectById("INVITE_ONLY")).thenReturn(null);
        when(appSettingMapper.selectById("TURNSTILE_SECRET_KEY")).thenReturn(null);
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("password123")).thenReturn("encodedHash");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return 1;
        });

        Long userId = authService.register("newuser", "user@example.com", "password123", null, null);

        assertThat(userId).isEqualTo(1L);
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_reservedUsername() {
        when(appSettingMapper.selectById("INVITE_ONLY")).thenReturn(null);
        when(appSettingMapper.selectById("TURNSTILE_SECRET_KEY")).thenReturn(null);

        assertThatThrownBy(() -> authService.register("admin", "admin@example.com", "pass", null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("系统保留");
    }

    @Test
    void register_usernameExists() {
        when(appSettingMapper.selectById("INVITE_ONLY")).thenReturn(null);
        when(appSettingMapper.selectById("TURNSTILE_SECRET_KEY")).thenReturn(null);
        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> authService.register("existinguser", "user@example.com", "pass", null, null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    void register_inviteOnly_noCode() {
        AppSetting inviteOnly = new AppSetting();
        inviteOnly.setSettingKey("INVITE_ONLY");
        inviteOnly.setSettingValue("true");
        when(appSettingMapper.selectById("INVITE_ONLY")).thenReturn(inviteOnly);

        assertThatThrownBy(() -> authService.register("newuser", "user@example.com", "pass", null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("邀请码");
    }

    // ==================== login 测试 ====================

    @Test
    void login_success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash("encodedHash");
        user.setRole("USER");
        user.setEnabled(1);

        when(valueOperations.get("lock:login:testuser")).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("password123", "encodedHash")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, "testuser", "USER")).thenReturn("jwt-token");

        Map<String, Object> result = authService.login("testuser", "password123");

        assertThat(result).containsKey("token");
        assertThat(result.get("token")).isEqualTo("jwt-token");
        verify(redisTemplate).delete("lock:login:testuser");
    }

    @Test
    void login_userNotFound() {
        when(valueOperations.get("lock:login:ghost")).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(null);
        when(valueOperations.increment("lock:login:ghost")).thenReturn(1L);

        assertThatThrownBy(() -> authService.login("ghost", "password"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void login_wrongPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash("encodedHash");
        user.setEnabled(1);

        when(valueOperations.get("lock:login:testuser")).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("wrongpass", "encodedHash")).thenReturn(false);
        when(valueOperations.increment("lock:login:testuser")).thenReturn(1L);

        assertThatThrownBy(() -> authService.login("testuser", "wrongpass"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("剩余尝试次数");
    }

    @Test
    void login_banned() {
        User user = new User();
        user.setId(1L);
        user.setUsername("banned");
        user.setPasswordHash("encodedHash");
        user.setRole("USER");
        user.setEnabled(0);

        when(valueOperations.get("lock:login:banned")).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("pass", "encodedHash")).thenReturn(true);

        assertThatThrownBy(() -> authService.login("banned", "pass"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("封禁");
    }

    @Test
    void login_locked() {
        when(valueOperations.get("lock:login:locked")).thenReturn("5");

        assertThatThrownBy(() -> authService.login("locked", "pass"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("锁定");
    }

    // ==================== getProfile 测试 ====================

    @Test
    void getProfile_success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole("USER");
        when(userMapper.selectById(1L)).thenReturn(user);

        var profile = authService.getProfile(1L);

        assertThat(profile).isNotNull();
        assertThat(profile.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getProfile_notFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        var profile = authService.getProfile(999L);

        assertThat(profile).isNull();
    }
}
