package com.example.demonet;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

@SpringBootApplication
@RequiredArgsConstructor
public class DemoNetApplication {

    private final StringRedisTemplate redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(DemoNetApplication.class, args);
    }

    /** Clear cached query results on startup so code changes take effect immediately */
    @PostConstruct
    public void clearCaches() {
        Set<String> keys = redisTemplate.keys("hotItems*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
        keys = redisTemplate.keys("featured*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
        keys = redisTemplate.keys("recommended*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
        keys = redisTemplate.keys("visibleTypes*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
    }
}
