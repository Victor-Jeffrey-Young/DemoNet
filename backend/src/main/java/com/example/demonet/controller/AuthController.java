package com.example.demonet.controller;

import com.example.demonet.dto.UserDTO;
import com.example.demonet.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        return authService.register(
                body.get("username"),
                body.get("email"),
                body.get("password"));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        return authService.login(body.get("username"), body.get("password"));
    }

    @GetMapping("/me")
    public UserDTO me(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return authService.getProfile(userId);
    }
}
