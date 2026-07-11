package com.example.demonet.controller;

import com.example.demonet.service.AuthService;
import com.example.demonet.mapper.AppSettingMapper;
import com.example.demonet.entity.AppSetting;
import com.example.demonet.common.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 单元测试（Standalone MockMvc）。
 * 覆盖 register / login / config 端点。
 * AuthController 构造函数需要 AuthService, StringRedisTemplate, AppSettingMapper。
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private AppSettingMapper appSettingMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AuthController(authService, redisTemplate, appSettingMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        // Note: Redis stub moved to individual test methods to avoid
        // Mockito UnnecessaryStubbingException for tests that don't use it.
    }

    @Test
    void config_returnsSettings() throws Exception {
        AppSetting tsSetting = new AppSetting();
        tsSetting.setSettingValue("0x4AAAAAAA-test");
        when(appSettingMapper.selectById("TURNSTILE_SITE_KEY")).thenReturn(tsSetting);
        when(appSettingMapper.selectById("INVITE_ONLY")).thenReturn(null);

        mockMvc.perform(get("/api/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turnstileSiteKey").value("0x4AAAAAAA-test"))
                .andExpect(jsonPath("$.inviteOnly").value(false));
    }

    @Test
    void register_success() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(authService.register(eq("testuser"), eq("test@example.com"), eq("Pass1234"), isNull(), isNull()))
                .thenReturn(100L);
        when(authService.buildResponse(100L))
                .thenReturn(Map.of("token", "jwt-token-xxx", "message", "注册成功"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"Pass1234\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-xxx"))
                .andExpect(jsonPath("$.message").value("注册成功"));
    }

    @Test
    void register_invalidPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"short\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void register_missingField_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void register_rateLimited_returns429() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(5L);  // > 3 = rate limited

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"Pass1234\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("注册太频繁，请稍后再试"));
    }

    @Test
    void login_success() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(authService.login("testuser", "Pass1234"))
                .thenReturn(Map.of("token", "jwt-token-xxx", "message", "登录成功"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"Pass1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-xxx"))
                .andExpect(jsonPath("$.message").value("登录成功"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(authService.login("baduser", "wrongpass"))
                .thenThrow(new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"baduser\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void login_missingPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void login_rateLimited_returns429() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(10L);  // > 5 = rate limited

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"Pass1234\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("登录尝试过于频繁，请稍后再试"));
    }
}
