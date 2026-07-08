package com.example.demonet;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class DemoNetApplication {

    private final StringRedisTemplate redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(DemoNetApplication.class, args);
    }

    /** Clear cached query results on startup so code changes take effect immediately.
     *  使用 SCAN 代替 KEYS，避免阻塞 Redis 主线程（审计报告 ARCH-3） */
    @PostConstruct
    public void clearCaches() {
        for (String pattern : new String[]{"hotItems*", "featured*", "recommended*", "visibleTypes*"}) {
            Set<String> keys = scanKeys(pattern);
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared {} keys matching {}", keys.size(), pattern);
            }
        }
    }

    /** 使用 SCAN 迭代匹配的 key，避免 KEYS 阻塞 Redis 主线程 */
    @SuppressWarnings("deprecation")
    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        try {
            redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Void>) connection -> {
                ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
                try (Cursor<byte[]> cursor = connection.scan(options)) {
                    while (cursor.hasNext()) {
                        keys.add(new String(cursor.next()));
                    }
                } catch (Exception e) {
                    log.warn("SCAN failed for pattern {}: {}", pattern, e.getMessage());
                }
                return null;
            });
        } catch (DataAccessException e) {
            log.warn("Redis connection failed during cache clear: {}", e.getMessage());
        }
        return keys;
    }
}
