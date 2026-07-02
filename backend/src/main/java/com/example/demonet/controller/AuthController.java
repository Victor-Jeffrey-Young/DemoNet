package com.example.demonet.controller;

import com.example.demonet.dto.LoginRequest;
import com.example.demonet.dto.RegisterRequest;
import com.example.demonet.dto.UserDTO;
import com.example.demonet.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(
                req.getUsername(),
                req.getEmail(),
                req.getPassword());
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