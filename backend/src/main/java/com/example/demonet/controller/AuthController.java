package com.example.demonet.controller;

import com.example.demonet.dto.LoginRequest;
import com.example.demonet.dto.RegisterRequest;
import com.example.demonet.dto.UserDTO;
import com.example.demonet.service.AuthService;
import com.example.demonet.entity.AppSetting;
import com.example.demonet.mapper.AppSettingMapper;
import com.example.demonet.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final StringRedisTemplate redisTemplate;
    private final AppSettingMapper appSettingMapper;

    /** Public config: Turnstile site key, invite-only toggle */
    @GetMapping("/config")
    public Map<String, Object> config() {
        AppSetting tsSetting = appSettingMapper.selectById("TURNSTILE_SITE_KEY");
        AppSetting inviteSetting = appSettingMapper.selectById("INVITE_ONLY");
        return Map.of(
            "turnstileSiteKey", tsSetting != null ? tsSetting.getSettingValue() : "",
            "inviteOnly", inviteSetting != null && "true".equalsIgnoreCase(inviteSetting.getSettingValue())
        );
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest req, HttpServletRequest request) {
        // IP rate limit: max 3 registrations per minute per IP
        String ip = request.getRemoteAddr();
        String rateKey = "rate:register:" + ip;
        Long count = redisTemplate.opsForValue().increment(rateKey);
        if (count == 1) redisTemplate.expire(rateKey, Duration.ofMinutes(1));
        if (count > 3) throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "注册太频繁，请稍后再试");

        Long userId = authService.register(req.getUsername(), req.getEmail(), req.getPassword(), req.getTurnstileToken(), req.getInviteCode());
        return authService.buildResponse(userId);
    }


    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        // IP rate limit: max 5 logins per minute per IP（审计报告 SEC-2）
        String ip = request.getRemoteAddr();
        String rateKey = "rate:login:" + ip;
        Long count = redisTemplate.opsForValue().increment(rateKey);
        if (count == 1) redisTemplate.expire(rateKey, Duration.ofMinutes(1));
        if (count > 5) throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "登录尝试过于频繁，请稍后再试");
        return authService.login(req.getUsername(), req.getPassword());
    }

    @GetMapping("/me")
    public UserDTO me(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return authService.getProfile(userId);
    }
}
