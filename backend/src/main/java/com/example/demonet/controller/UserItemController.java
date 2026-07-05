package com.example.demonet.controller;

import com.example.demonet.entity.UserItem;
import com.example.demonet.service.UserItemService;
import com.example.demonet.dto.UserItemSaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserItemController {

    private final UserItemService userItemService;

    @PostMapping("/items")
    public UserItem save(@Valid @RequestBody UserItemSaveRequest body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return userItemService.saveOrUpdate(userId, body.getItemId(), body.getStatus());
    }

    @GetMapping("/items")
    public List<Map<String, Object>> list(
            @RequestParam(required = false) String status,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return userItemService.listByUser(userId, status);
    }

    @DeleteMapping("/items/{itemId}")
    public void remove(@PathVariable Long itemId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        userItemService.remove(userId, itemId);
    }

    @GetMapping("/items/{itemId}")
    public UserItem getOne(@PathVariable Long itemId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return userItemService.getStatus(userId, itemId);
    }
}
