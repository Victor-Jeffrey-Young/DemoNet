package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.entity.Review;
import com.example.demonet.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public Review create(Long userId, Long itemId, Integer rating, String comment) {
        Review r = new Review();
        r.setUserId(userId);
        r.setItemId(itemId);
        r.setRating(rating);
        r.setComment(comment);
        r.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(r);
        return r;
    }

    public Page<Review> listByItem(Long itemId, int page, int size) {
        LambdaQueryWrapper<Review> qw = new LambdaQueryWrapper<Review>()
                .eq(Review::getItemId, itemId)
                .orderByDesc(Review::getCreatedAt);
        return reviewMapper.selectPage(Page.of(page, size), qw);
    }

    public Map<String, Object> stats(Long itemId) {
        Map<String, Object> result = jdbcTemplate.queryForMap(
                "SELECT COUNT(*) as count, COALESCE(AVG(rating), 0) as avgRating FROM reviews WHERE item_id = ?", itemId);
        Number count = (Number) result.get("count");
        Number avg = (Number) result.get("avgRating");
        return Map.of("count", count.longValue(), "avgRating", Math.round(avg.doubleValue() * 10) / 10.0);
    }

    public void delete(Long reviewId, Long userId) {
        LambdaQueryWrapper<Review> qw = new LambdaQueryWrapper<Review>()
                .eq(Review::getId, reviewId)
                .eq(Review::getUserId, userId);
        if (reviewMapper.delete(qw) == 0) {
            throw new RuntimeException("无权删除此评论");
        }
    }
}
