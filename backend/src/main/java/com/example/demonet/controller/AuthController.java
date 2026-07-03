package com.example.demonet.controller;

import com.example.demonet.dto.LoginRequest;
import com.example.demonet.dto.RegisterRequest;
import com.example.demonet.dto.UserDTO;
import com.example.demonet.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
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
    private final JdbcTemplate jdbcTemplate;
    private final RestClient restClient = RestClient.create();

    /** Public config: Turnstile site key, invite-only toggle */
    @GetMapping("/config")
    public Map<String, Object> config() {
        return Map.of(
            "turnstileSiteKey", getSetting("TURNSTILE_SITE_KEY"),
            "inviteOnly", "true".equalsIgnoreCase(getSetting("INVITE_ONLY"))
        );
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest req, HttpServletRequest request) {
        // IP rate limit: max 3 registrations per minute per IP
        String ip = request.getRemoteAddr();
        String rateKey = "rate:register:" + ip;
        Long count = redisTemplate.opsForValue().increment(rateKey);
        if (count == 1) redisTemplate.expire(rateKey, Duration.ofMinutes(1));
        if (count > 3) throw new RuntimeException("注册太频繁，请稍后再试");

        // Invite-only check (before Turnstile, to show invite errors first)
        if ("true".equalsIgnoreCase(getSetting("INVITE_ONLY"))) {
            verifyInviteCode(req.getInviteCode());
        }

        // Turnstile verification (only if secret key is configured)
        verifyTurnstile(req.getTurnstileToken());

        Long userId = authService.register(req.getUsername(), req.getEmail(), req.getPassword());
        // Mark invite code as used (if invite-only mode)
        if (req.getInviteCode() != null && !req.getInviteCode().isBlank()) {
            jdbcTemplate.update("UPDATE invite_codes SET used_by=?, used_at=NOW() WHERE code=? AND used_by IS NULL",
                    userId, req.getInviteCode().toUpperCase());
        }
        return authService.buildResponse(userId);
    }

    private void verifyTurnstile(String token) {
        String secret = getSetting("TURNSTILE_SECRET_KEY");
        if (secret == null || secret.isBlank()) return;
        if (token == null || token.isBlank()) throw new RuntimeException("请完成人机验证");
        try {
            Map<String, Object> resp = restClient.post()
                    .uri("https://challenges.cloudflare.com/turnstile/v0/siteverify")
                    .body(Map.of("secret", secret, "response", token))
                    .retrieve()
                    .body(Map.class);
            if (resp == null || !Boolean.TRUE.equals(resp.get("success")))
                throw new RuntimeException("人机验证失败，请重试");
        } catch (RuntimeException e) { throw e; }
        catch (Exception e) {
            // Network / SSL errors: degrade gracefully, don't block registration
            log.warn("Turnstile verify unreachable (degraded): {}", e.toString());
        }
    }

    private void verifyInviteCode(String code) {
        if (code == null || code.isBlank()) throw new RuntimeException("当前仅支持邀请注册，请输入邀请码");
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM invite_codes WHERE code = ? AND used_by IS NULL", Integer.class, code.toUpperCase());
        if (count == null || count == 0) throw new RuntimeException("邀请码无效或已被使用");
    }

    private String getSetting(String key) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT setting_value FROM app_settings WHERE setting_key = ?", String.class, key);
        } catch (Exception e) { return ""; }
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req.getUsername(), req.getPassword());
    }

    @GetMapping("/me")
    public UserDTO me(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return authService.getProfile(userId);
    }
}
