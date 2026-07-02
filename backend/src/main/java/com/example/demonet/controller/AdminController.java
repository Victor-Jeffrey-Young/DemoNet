package com.example.demonet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.config.RabbitMQConfig;
import com.example.demonet.entity.Item;
import com.example.demonet.entity.Tag;
import com.example.demonet.mapper.ItemMapper;
import com.example.demonet.service.AdminService;
import com.example.demonet.service.SteamService;
import com.example.demonet.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.CacheEvict;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RabbitTemplate rabbitTemplate;
    private final ItemMapper itemMapper;
    private final AdminService adminService;
    private final TagService tagService;
    private final JdbcTemplate jdbcTemplate;
    private final SteamService steamService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    // ========== Item CRUD ==========

    @GetMapping("/items")
    public IPage<Item> listItems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return adminService.listAllItems(page, size, type, keyword, status);
    }

    @GetMapping("/items/{id}")
    public Map<String, Object> getItem(@PathVariable Long id) {
        Map<String, Object> result = adminService.getItemWithTags(id);
        if (result == null) {
            return Map.of("error", "Item not found");
        }
        return result;
    }

    @PostMapping("/items")
    public Item createItem(@RequestBody Item item) {
        return adminService.createItem(item);
    }

    @PutMapping("/items/{id}")
    public Item updateItem(@PathVariable Long id, @RequestBody Item item) {
        return adminService.updateItem(id, item);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        adminService.deleteItem(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/items/{id}/status")
    public Item updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        return adminService.updateStatus(id, status);
    }

    @PutMapping("/items/{id}/featured")
    @CacheEvict(value = {"featured", "hotItems", "recommended"}, allEntries = true)
    public Item toggleFeatured(@PathVariable Long id) {
        return adminService.toggleFeatured(id);
    }

    // ========== Carousel Management ==========

    @GetMapping("/carousel/{type}")
    public List<Item> getCarousel(@PathVariable String type) {
        return adminService.getCarouselItems(type);
    }

    @PostMapping("/carousel/{type}")
    @CacheEvict(value = {"featured", "hotItems", "recommended"}, allEntries = true)
    public Map<String, String> saveCarousel(@PathVariable String type, @RequestBody Map<String, List<Long>> body) {
        adminService.saveCarouselOrder(type, body.get("itemIds"));
        return Map.of("message", "ok");
    }

    @DeleteMapping("/carousel/{type}/{itemId}")
    @CacheEvict(value = {"featured", "hotItems", "recommended"}, allEntries = true)
    public Map<String, String> removeFromCarousel(@PathVariable String type, @PathVariable Long itemId) {
        adminService.removeFromCarousel(itemId);
        return Map.of("message", "ok");
    }

    // ========== Image Upload ==========

    @PostMapping(value = "/upload/{id}", consumes = "multipart/form-data")
    public Map<String, String> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "cover") String type) {
        try {
            Item item = itemMapper.selectById(id);
            if (item == null) {
                return Map.of("error", "Item not found");
            }

            Path uploadPath = Paths.get(new java.io.File(uploadDir).getAbsolutePath());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + ext;
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            String url = "/uploads/" + filename;

            switch (type) {
                case "wide_cover" -> item.setWideCoverUrl(url);
                case "poster" -> item.setPosterUrl(url);
                case "reader" -> {
                    String info = item.getInfoJson();
                    if (info == null || info.isBlank()) {
                        item.setInfoJson("{\"reader_url\":\"" + url + "\"}");
                    } else {
                        String cleaned = info
                            .replaceAll(",\\s*\"reader_url\"\\s*:\\s*\"[^\"]*\"", "")
                            .replaceAll("\"reader_url\"\\s*:\\s*\"[^\"]*\"\\s*,?\\s*", "");
                        if (cleaned.strip().equals("{}")) {
                            item.setInfoJson("{\"reader_url\":\"" + url + "\"}");
                        } else {
                            String inner = cleaned.strip().replaceAll("^\\{|}$", "");
                            item.setInfoJson("{" + inner + ",\"reader_url\":\"" + url + "\"}");
                        }
                    }
                }
                default -> item.setCoverUrl(url);
            }
            itemMapper.updateById(item);

            return Map.of("url", url, "type", type);
        } catch (IOException e) {
            return Map.of("error", "上传失败: " + e.getMessage());
        }
    }

    // ========== Tag Management ==========

    @GetMapping("/tags")
    public List<Tag> listAllTags() {
        return tagService.listAll();
    }

    @GetMapping("/tags/paged")
    public IPage<Tag> listTags(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword) {
        return tagService.listPaginated(page, size, keyword);
    }

    @PostMapping("/tags")
    public Tag createTag(@RequestBody Map<String, String> body) {
        return tagService.create(body.get("name"));
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/items/{id}/tags")
    public ResponseEntity<Void> associateItemTags(
            @PathVariable Long id,
            @RequestBody List<Long> tagIds) {
        tagService.associateItem(id, tagIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{id}/tags/{tagId}")
    public ResponseEntity<Void> removeItemTag(
            @PathVariable Long id,
            @PathVariable Long tagId) {
        tagService.removeItemTag(id, tagId);
        return ResponseEntity.ok().build();
    }

    // ========== Stats ==========

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return adminService.getStats();
    }

    // ========== Category Visibility ==========

    @GetMapping("/categories/settings")
    public List<Map<String, Object>> getCategorySettings() {
        return jdbcTemplate.queryForList("SELECT * FROM category_settings ORDER BY sort_order");
    }

    @PutMapping("/categories/settings")
    @CacheEvict(value = {"featured", "hotItems", "recommended"}, allEntries = true)
    public Map<String, String> updateCategorySettings(@RequestBody List<Map<String, Object>> settings) {
        for (Map<String, Object> s : settings) {
            String type = (String) s.get("type");
            boolean visible = Boolean.TRUE.equals(s.get("visible"));
            int sortOrder = s.containsKey("sort_order") ? ((Number) s.get("sort_order")).intValue() : 0;
            jdbcTemplate.update("REPLACE INTO category_settings (type, visible, sort_order) VALUES (?, ?, ?)",
                    type, visible ? 1 : 0, sortOrder);
        }
        return Map.of("message", "ok");
    }

    // ========== Fetch & Pending (existing) ==========

    
    @PostMapping("/backfill/poster-urls")
    public Map<String, Object> backfillPosterUrls() {
        int count = steamService.backfillPosterUrls();
        return Map.of("message", "Backfilled poster_url for " + count + " games", "count", count);
    }
    @PostMapping("/fetch/steam")
    public Map<String, Object> triggerSteam(@RequestBody Map<String, Object> body) {
        List<Integer> appIds = (List<Integer>) body.get("appIds");
        String targetType = (String) body.getOrDefault("targetType", "game");
        // Check which appIds already exist
        List<String> existingNames = new ArrayList<>();
        List<Integer> newIds = new ArrayList<>();
        for (Integer appId : appIds) {
            String slug = "steam-" + appId;
            Item existing = itemMapper.selectOne(
                    new LambdaQueryWrapper<Item>().eq(Item::getSlug, slug));
            if (existing != null) {
                existingNames.add(existing.getTitle());
            } else {
                newIds.add(appId);
            }
        }
        Map<String, Object> payload = Map.of("appIds", appIds, "targetType", targetType);
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_STEAM, payload);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", appIds.size());
        result.put("newCount", newIds.size());
        result.put("existingCount", existingNames.size());
        if (!existingNames.isEmpty()) {
            result.put("existingNames", existingNames);
            result.put("message", String.format("已加入队列: %d 个新游戏 + %d 个更新 (%s)",
                    newIds.size(), existingNames.size(), String.join(", ", existingNames)));
        } else {
            result.put("message", "已加入队列: " + appIds.size() + " 个游戏 → " + targetType);
        }
        return result;
    }

    @GetMapping("/steam/search")
    public List<Map<String, Object>> searchSteam(@RequestParam String q) {
        Map<String, Object> resp = RestClient.create().get()
                .uri("https://store.steampowered.com/api/storesearch/?term={q}&cc=cn&l=schinese", q)
                .retrieve()
                .body(Map.class);
        if (resp == null) return List.of();
        List<Map<String, Object>> items = (List<Map<String, Object>>) resp.get("items");
        if (items == null) return List.of();
        
        // Batch-check types to filter out DLCs
        Set<Integer> dlcIds = new HashSet<>();
        if (!items.isEmpty()) {
            String ids = items.stream()
                    .map(it -> String.valueOf(it.get("id")))
                    .collect(java.util.stream.Collectors.joining(","));
            try {
                Map<String, Object> check = RestClient.create().get()
                        .uri("https://store.steampowered.com/api/appdetails?appids=" + ids + "&cc=cn&l=schinese")
                        .retrieve()
                        .body(Map.class);
                if (check != null) {
                    for (Map<String, Object> item : items) {
                        Object appId = item.get("id");
                        Map<String, Object> appData = (Map<String, Object>) check.get(String.valueOf(appId));
                        if (appData != null && Boolean.TRUE.equals(appData.get("success"))) {
                            Map<String, Object> data = (Map<String, Object>) appData.get("data");
                            if (data != null && "dlc".equals(String.valueOf(data.getOrDefault("type", "")))) {
                                dlcIds.add(((Number) appId).intValue());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("DLC filter check failed: {}", e.getMessage());
            }
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : items) {
            if (dlcIds.contains(((Number) item.get("id")).intValue())) continue;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", item.get("id"));
            entry.put("name", item.get("name"));
            entry.put("price", item.get("price"));
            entry.put("tinyImage", item.get("tiny_image"));
            result.add(entry);
        }
        return result;
    }

    @PostMapping("/fetch/tmdb")
    public Map<String, String> triggerTMDB(@RequestBody Map<String, Object> body) {
        String query = (String) body.get("query");
        String targetType = (String) body.getOrDefault("targetType", "movie");
        Map<String, Object> payload = Map.of("query", query, "targetType", targetType);
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_TMDB, payload);
        return Map.of("message", "TMDB fetch queued: " + query + " → " + targetType);
    }

    @PostMapping("/fetch/anilist")
    public Map<String, String> triggerAniList(@RequestBody Map<String, Object> body) {
        String query = (String) body.get("query");
        String targetType = (String) body.getOrDefault("targetType", "anime");
        Map<String, Object> payload = Map.of("query", query, "targetType", targetType);
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_ANILIST, payload);
        return Map.of("message", "AniList fetch queued: " + query + " → " + targetType);
    }

    @PostMapping("/fetch/bangumi")
    public Map<String, String> triggerBangumi(@RequestBody Map<String, Object> body) {
        String query = (String) body.get("query");
        String targetType = (String) body.getOrDefault("targetType", "anime");
        Map<String, Object> payload = Map.of("query", query, "targetType", targetType);
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_BANGUMI, payload);
        return Map.of("message", "Bangumi fetch queued: " + query + " → " + targetType);
    }

    @PostMapping("/fetch/tmdb-tv")
    public Map<String, String> triggerTMDBTV(@RequestBody Map<String, Object> body) {
        String query = (String) body.get("query");
        String targetType = (String) body.getOrDefault("targetType", "anime");
        Map<String, Object> payload = Map.of("query", query, "targetType", targetType);
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_TMDB_TV, payload);
        return Map.of("message", "TMDB TV fetch queued: " + query + " → " + targetType);
    }

    @PostMapping("/fetch/itunes")
    public Map<String, String> triggerItunes(@RequestBody Map<String, Object> body) {
        String query = (String) body.get("query");
        String targetType = (String) body.getOrDefault("targetType", "music");
        Map<String, Object> payload = Map.of("query", query, "targetType", targetType);
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_ITUNES, payload);
        return Map.of("message", "iTunes fetch queued: " + query + " → " + targetType);
    }

    @PostMapping("/fetch/igdb")
    public Map<String, String> triggerIGDB(@RequestBody Map<String, Object> body) {
        String query = (String) body.get("query");
        String endpoint = (String) body.getOrDefault("endpoint", "search");
        int limit = body.get("limit") != null ? ((Number) body.get("limit")).intValue() : 10;
        String targetType = (String) body.getOrDefault("targetType", "game");
        Map<String, Object> payload = Map.of(
                "query", query != null ? query : "",
                "endpoint", endpoint,
                "limit", limit,
                "targetType", targetType
        );
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_IGDB, payload);
        return Map.of("message", "IGDB fetch queued: " + endpoint + " limit=" + limit + " → " + targetType);
    }

    @GetMapping("/pending")
    public IPage<Item> listPending(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Item> p = new Page<>(page, size);
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getStatus, 0);
        wrapper.orderByDesc(Item::getCreatedAt);
        return itemMapper.selectPage(p, wrapper);
    }

    @PutMapping("/approve/{id}")
    public Item approve(@PathVariable Long id) {
        Item item = itemMapper.selectById(id);
        if (item != null) {
            item.setStatus(1);
            itemMapper.updateById(item);
        }
        return item;
    }

    @PutMapping("/reject/{id}")
    public void reject(@PathVariable Long id) {
        itemMapper.deleteById(id);
    }

    @PutMapping("/reject/batch")
    public Map<String, String> rejectBatch(@RequestBody List<Long> ids) {
        itemMapper.deleteBatchIds(ids);
        return Map.of("message", "已批量拒绝 " + ids.size() + " 条");
    }
}
