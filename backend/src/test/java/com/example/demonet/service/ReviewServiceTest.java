package com.example.demonet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.entity.Review;
import com.example.demonet.mapper.ReviewMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReviewService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock private ReviewMapper reviewMapper;
    @Mock private JdbcTemplate jdbcTemplate;

    @Test
    void create_success() {
        when(reviewMapper.insert(any(Review.class))).thenReturn(1);

        Review result = reviewService.create(1L, 100L, 5, "Great game!");

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getItemId()).isEqualTo(100L);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Great game!");
        verify(reviewMapper).insert(any(Review.class));
    }

    @Test
    void listByItem_success() {
        Page<Review> mockPage = new Page<>();
        Review r = new Review();
        r.setId(1L);
        r.setRating(4);
        mockPage.setRecords(java.util.List.of(r));
        mockPage.setTotal(1);
        when(reviewMapper.selectPage(any(), any())).thenReturn(mockPage);

        Page<Review> result = reviewService.listByItem(100L, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    void stats_success() {
        when(jdbcTemplate.queryForMap(anyString(), eq(100L)))
                .thenReturn(Map.of("count", 5L, "avgRating", 4.2));

        Map<String, Object> result = reviewService.stats(100L);

        assertThat(result.get("count")).isEqualTo(5L);
        assertThat(result.get("avgRating")).isEqualTo(4.2);
    }

    @Test
    void delete_success() {
        when(reviewMapper.delete(any())).thenReturn(1);

        reviewService.delete(1L, 1L);

        verify(reviewMapper).delete(any());
    }

    @Test
    void delete_unauthorized() {
        when(reviewMapper.delete(any())).thenReturn(0);

        assertThatThrownBy(() -> reviewService.delete(1L, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("无权删除");
    }
}
