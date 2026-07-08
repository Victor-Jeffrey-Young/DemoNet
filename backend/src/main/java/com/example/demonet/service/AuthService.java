package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demonet.dto.UserDTO;
import com.example.demonet.entity.User;
import com.example.demonet.mapper.UserMapper;
import com.example.demonet.entity.AppSetting;
import com.example.demonet.entity.InviteCode;
import com.example.demonet.mapper.AppSettingMapper;
import com.example.demonet.mapper.InviteCodeMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import com.example.demonet.common.BusinessException;
import com.example.demonet.common.ConflictException;
import com.example.demonet.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AppSettingMapper appSettingMapper;
    private final InviteCodeMapper inviteCodeMapper;
    private final RestClient restClient;
    private final StringRedisTemplate redisTemplate;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    /** Registers user, returns the created user ID. */
    public Long register(String username, String email, String password, String turnstileToken, String inviteCode) {
        // Invite-only check (before Turnstile, to show invite errors first)
        AppSetting inviteOnlySetting = appSettingMapper.selectById("INVITE_ONLY");
        if (inviteOnlySetting != null && "true".equalsIgnoreCase(inviteOnlySetting.getSettingValue())) {
            if (inviteCode == null || inviteCode.isBlank()) throw new BusinessException("当前仅支持邀请注册，请输入邀请码");
            Long count = inviteCodeMapper.selectCount(new LambdaQueryWrapper<InviteCode>().eq(InviteCode::getCode, inviteCode.toUpperCase()).isNull(InviteCode::getUsedBy));
            if (count == null || count == 0) throw new BusinessException("邀请码无效或已被使用");
        }
        
        // Turnstile verification
        AppSetting tsSetting = appSettingMapper.selectById("TURNSTILE_SECRET_KEY");
        if (tsSetting != null && tsSetting.getSettingValue() != null && !tsSetting.getSettingValue().isBlank()) {
            if (turnstileToken == null || turnstileToken.isBlank()) throw new BusinessException("请完成人机验证");
            try {
                Map<String, Object> resp = restClient.post()
                        .uri("https://challenges.cloudflare.com/turnstile/v0/siteverify")
                        .body(Map.of("secret", tsSetting.getSettingValue(), "response", turnstileToken))
                        .retrieve()
                        .body(new ParameterizedTypeReference<Map<String, Object>>() {});
                if (resp == null || !Boolean.TRUE.equals(resp.get("success")))
                    throw new BusinessException("人机验证失败，请重试");
            } catch (BusinessException e) { throw e; }
            catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(AuthService.class).warn("Turnstile API error: {}", e.getMessage());
                throw new BusinessException("验证码服务不可用，请稍后重试");
            }
        }

        Set<String> reserved = Set.of("admin", "root", "system", "administrator", "moderator", "owner", "superuser", "guest", "anonymous", "api");
        if (reserved.contains(username.toLowerCase())) {
            throw new BusinessException("该用户名为系统保留字段，请使用其他用户名");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0) {
            throw new ConflictException("用户名已存在");
        }
        String finalEmail = (email != null && !email.isBlank()) ? email : username + "@demo.local";
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, finalEmail)) > 0) {
            throw new ConflictException("邮箱已被注册");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(finalEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setEnabled(1);
        userMapper.insert(user);
        
        // Mark invite code as used
        if (inviteCode != null && !inviteCode.isBlank()) {
            InviteCode ic = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>().eq(InviteCode::getCode, inviteCode.toUpperCase()).isNull(InviteCode::getUsedBy));
            if (ic != null) {
                ic.setUsedBy(user.getId());
                ic.setUsedAt(java.time.LocalDateTime.now());
                inviteCodeMapper.updateById(ic);
            }
        }
        
        return user.getId();
    }

    public Map<String, Object> buildResponse(Long userId) {
        User user = userMapper.selectById(userId);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        return Map.of("token", token, "user", UserDTO.fromEntity(user));
    }

    public Map<String, Object> login(String username, String password) {
        // 账号锁定检查（审计报告 SEC-2）：连续失败 5 次锁定 15 分钟
        String lockKey = "lock:login:" + username.toLowerCase();
        String attemptsStr = redisTemplate.opsForValue().get(lockKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "账号已被锁定，请 " + LOCK_DURATION.toMinutes() + " 分钟后再试");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            // 记录失败次数
            Long newCount = redisTemplate.opsForValue().increment(lockKey);
            if (newCount != null && newCount == 1) redisTemplate.expire(lockKey, LOCK_DURATION);
            int remaining = MAX_LOGIN_ATTEMPTS - (newCount != null ? newCount.intValue() : attempts + 1);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, remaining > 0
                    ? "用户名或密码错误，剩余尝试次数：" + remaining
                    : "用户名或密码错误，账号已被锁定 " + LOCK_DURATION.toMinutes() + " 分钟");
        }
        if (user.getEnabled() != null && user.getEnabled() == 0) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "账号已被封禁，请联系管理员");
        }
        // 登录成功，清除失败计数
        redisTemplate.delete(lockKey);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        return Map.of("token", token, "user", UserDTO.fromEntity(user));
    }

    public UserDTO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? UserDTO.fromEntity(user) : null;
    }
}
