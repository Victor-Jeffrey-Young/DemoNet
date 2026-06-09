package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demonet.dto.UserDTO;
import com.example.demonet.entity.User;
import com.example.demonet.mapper.UserMapper;
import com.example.demonet.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public Map<String, Object> register(String username, String email, String password) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        String finalEmail = (email != null && !email.isBlank()) ? email : username + "@demo.local";
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, finalEmail)) > 0) {
            throw new RuntimeException("邮箱已被注册");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(finalEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("USER");
        userMapper.insert(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        return Map.of("token", token, "user", UserDTO.fromEntity(user));
    }

    public Map<String, Object> login(String username, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        return Map.of("token", token, "user", UserDTO.fromEntity(user));
    }

    public UserDTO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? UserDTO.fromEntity(user) : null;
    }
}
