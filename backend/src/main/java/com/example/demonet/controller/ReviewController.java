package com.example.demonet.controller;

import com.example.demonet.entity.Review;
import com.example.demonet.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> create(Authentication auth, @RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(auth.getName());
        Long itemId = Long.valueOf(body.get("itemId").toString());
        Integer rating = body.get("rating") != null ? Integer.valueOf(body.get("rating").toString()) : null;
        String comment = (String) body.getOrDefault("comment", "");
        return ResponseEntity.ok(reviewService.create(userId, itemId, rating, comment));
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<Map<String, Object>> list(@PathVariable Long itemId,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        var result = reviewService.listByItem(itemId, page, size);
        return ResponseEntity.ok(Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "stats", reviewService.stats(itemId)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id, Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        reviewService.delete(id, userId);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}
