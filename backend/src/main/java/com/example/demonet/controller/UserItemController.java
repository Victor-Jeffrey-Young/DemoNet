package com.example.demonet.controller;

import com.example.demonet.entity.UserItem;
import com.example.demonet.service.UserItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserItemController {

    private final UserItemService userItemService;

    @PostMapping("/items")
    public UserItem save(@RequestBody Map<String, Object> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Long itemId = Long.valueOf(body.get("itemId").toString());
        String status = body.get("status").toString();
        return userItemService.saveOrUpdate(userId, itemId, status);
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
