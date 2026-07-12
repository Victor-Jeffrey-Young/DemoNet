package com.example.demonet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.entity.Review;
import com.example.demonet.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.withSettings;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReviewController 单元测试（Standalone MockMvc）。
 * 使用 SecurityMockMvcRequestPostProcessors.authentication() 模拟带 Long 类型 principal 的认证用户。
 */
@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ReviewController(reviewService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .apply(springSecurity())
                .build();
    }

    private Authentication mockAuth(Long userId) {
        Authentication auth = mock(Authentication.class, withSettings().lenient());
        when(auth.getPrincipal()).thenReturn(userId);
        return auth;
    }

    @Test
    void create_success() throws Exception {
        Review review = new Review();
        review.setId(1L);
        review.setUserId(1L);
        review.setItemId(100L);
        review.setRating(5);
        review.setComment("Great!");
        when(reviewService.create(anyLong(), eq(100L), eq(5), eq("Great!"))).thenReturn(review);

        mockMvc.perform(post("/api/reviews")
                        .with(authentication(mockAuth(1L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":100,\"rating\":5,\"comment\":\"Great!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void create_invalidRating_returns400() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .with(authentication(mockAuth(1L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":100,\"rating\":6}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_missingItemId_returns400() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .with(authentication(mockAuth(1L)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"comment\":\"no itemId\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listByItem_success() throws Exception {
        Page<Review> page = new Page<>();
        Review r = new Review();
        r.setId(1L);
        r.setRating(4);
        page.setRecords(List.of(r));
        page.setTotal(1);
        when(reviewService.listByItem(eq(100L), anyInt(), anyInt())).thenReturn(page);
        when(reviewService.stats(100L)).thenReturn(Map.of("count", 1L, "avgRating", 4.0));

        mockMvc.perform(get("/api/reviews/item/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records").isArray())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.stats.count").value(1));
    }

    @Test
    void delete_success() throws Exception {
        doNothing().when(reviewService).delete(1L, 1L);

        mockMvc.perform(delete("/api/reviews/1")
                        .with(authentication(mockAuth(1L)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(reviewService).delete(1L, 1L);
    }
}
