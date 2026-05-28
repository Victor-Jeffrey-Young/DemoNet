package com.example.demonet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.config.RabbitMQConfig;
import com.example.demonet.entity.Item;
import com.example.demonet.entity.Tag;
import com.example.demonet.mapper.ItemMapper;
import com.example.demonet.service.AdminService;
import com.example.demonet.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RabbitTemplate rabbitTemplate;
    private final ItemMapper itemMapper;
    private final AdminService adminService;
    private final TagService tagService;

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
    public Item toggleFeatured(@PathVariable Long id) {
        return adminService.toggleFeatured(id);
    }

    // ========== Carousel Management ==========

    @GetMapping("/carousel/{type}")
    public List<Item> getCarousel(@PathVariable String type) {
        return adminService.getCarouselItems(type);
    }

    @PostMapping("/carousel/{type}")
    public Map<String, String> saveCarousel(@PathVariable String type, @RequestBody Map<String, List<Long>> body) {
        adminService.saveCarouselOrder(type, body.get("itemIds"));
        return Map.of("message", "ok");
    }

    @DeleteMapping("/carousel/{type}/{itemId}")
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

    // ========== Fetch & Pending (existing) ==========

    @PostMapping("/fetch/steam")
    public Map<String, String> triggerSteam(@RequestBody Map<String, Object> body) {
        List<Integer> appIds = (List<Integer>) body.get("appIds");
        String targetType = (String) body.getOrDefault("targetType", "game");
        Map<String, Object> payload = Map.of("appIds", appIds, "targetType", targetType);
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_STEAM, payload);
        return Map.of("message", "Steam fetch queued: " + appIds.size() + " appIds → " + targetType);
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
}
