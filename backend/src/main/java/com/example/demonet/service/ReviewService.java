package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.entity.Review;
import com.example.demonet.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;

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
        var qw = new LambdaQueryWrapper<Review>().eq(Review::getItemId, itemId);
        Long count = reviewMapper.selectCount(qw);
        if (count == null || count == 0) return Map.of("count", 0, "avgRating", 0);
        var reviews = reviewMapper.selectList(qw);
        double avg = reviews.stream().filter(r -> r.getRating() != null).mapToInt(Review::getRating).average().orElse(0);
        return Map.of("count", count, "avgRating", Math.round(avg * 10) / 10.0);
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
