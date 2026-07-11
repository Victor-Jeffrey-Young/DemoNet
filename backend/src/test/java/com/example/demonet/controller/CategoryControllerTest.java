package com.example.demonet.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CategoryController 单元测试（Standalone MockMvc）。
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController(jdbcTemplate))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getVisibleCategories_success() throws Exception {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(
                Map.of("type", "game", "visible", 1, "sort_order", 0),
                Map.of("type", "movie", "visible", 1, "sort_order", 1)
        ));

        mockMvc.perform(get("/api/categories/visible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("game"));
    }

    @Test
    void getVisibleCategories_empty() throws Exception {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/categories/visible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
