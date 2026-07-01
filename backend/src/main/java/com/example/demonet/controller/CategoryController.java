package com.example.demonet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/visible")
    public List<Map<String, Object>> getVisibleCategories() {
        return jdbcTemplate.queryForList("SELECT type, visible, sort_order FROM category_settings ORDER BY sort_order");
    }
}
