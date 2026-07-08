package com.example.demonet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.config.RabbitMQConfig;
import com.example.demonet.entity.Item;
import com.example.demonet.entity.AppSetting;
import com.example.demonet.entity.CategorySetting;
import com.example.demonet.entity.InviteCode;
import com.example.demonet.entity.User;
import com.example.demonet.mapper.AppSettingMapper;
import com.example.demonet.mapper.CategorySettingMapper;
import com.example.demonet.mapper.InviteCodeMapper;
import com.example.demonet.mapper.UserMapper;
import com.example.demonet.entity.Tag;
import com.example.demonet.mapper.ItemMapper;
import com.example.demonet.service.AdminService;
import com.example.demonet.service.SteamService;
import com.example.demonet.service.SteamGridDBService;
import com.example.demonet.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.core.ParameterizedTypeReference;
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
import jakarta.validation.Valid;
import com.example.demonet.dto.request.AdminRequests.*;
import com.example.demonet.common.BusinessException;
import org.springframework.http.HttpStatus;

@Slf4j
@SuppressWarnings("null")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RabbitTemplate rabbitTemplate;
    private final ItemMapper itemMapper;
    private final AdminService adminService;
    private final TagService tagService;
    private final AppSettingMapper appSettingMapper;
    private final CategorySettingMapper categorySettingMapper;
    private final InviteCodeMapper inviteCodeMapper;
    private final UserMapper userMapper;
    private final tools.jackson.databind.ObjectMapper objectMapper;
    private final SteamService steamService;
    private final SteamGridDBService steamGridDBService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final org.springframework.web.client.RestClient restClient;

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
    public Item updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusReq body) {
        Integer status = body.getStatus();
        return adminService.updateStatus(id, status);
    }

    @PutMapping("/items/{id}/featured")
    @CacheEvict(value = {"featured", "hotItems", "recommended"}, allEntries = true)
    public Item toggleFeatured(@PathVariable Long id) {
        return adminService.toggleFeatured(id);
    }

    @PostMapping("/items/batch-delete")
    @CacheEvict(value = {"featured", "hotItems", "recommended"}, allEntries = true)
    public Map<String, Object> batchDelete(@Valid @RequestBody BatchDeleteReq body) {
        List<Long> ids = body.getIds();
        int count = adminService.batchDelete(ids);
        return Map.of("message", "已删除 " + count + " 条内容", "count", count);
    }

    @PostMapping("/items/batch-status")
    @CacheEvict(value = {"featured", "hotItems", "recommended"}, allEntries = true)
    public Map<String, Object> batchUpdateStatus(@Valid @RequestBody BatchStatusUpdateReq body) {
        List<Long> ids = body.getIds();
        Integer status = body.getStatus();
        int count = adminService.batchUpdateStatus(ids, status);
        String label = status == 1 ? "已上线" : "已下架";
        return Map.of("message", label + " " + count + " 条内容", "count", count);
    }

    // ========== Carousel Management ==========

    @GetMapping("/carousel/{type}")
    public List<Item> getCarousel(@PathVariable String type) {
        return adminService.getCarouselItems(type);
    }

    @PostMapping("/carousel/{type}")
    @CacheEvict(value = {"featured", "hotItems", "recommended"}, allEntries = true)
    public Map<String, String> saveCarousel(@PathVariable String type, @Valid @RequestBody CarouselSaveReq body) {
        adminService.saveCarouselOrder(type, body.getItemIds());
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

            String contentType = file.getContentType();
            if (contentType == null) {
                return Map.of("error", "文件类型未识别");
            }
            List<String> allowedTypes = "reader".equals(type)
                ? List.of("image/jpeg", "image/png", "image/webp", "image/gif", "application/pdf", "application/epub+zip")
                : List.of("image/jpeg", "image/png", "image/webp", "image/gif");
                
            if (!allowedTypes.contains(contentType)) {
                return Map.of("error", "不支持的文件类型: " + contentType);
            }

            Path uploadPath = Paths.get(new java.io.File(uploadDir).getAbsolutePath());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            }
            // 扩展名白名单校验（审计报告 SEC-4）：防止上传 .html/.svg 等含脚本的文件
            Set<String> allowedExts = "reader".equals(type)
                ? Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif", ".pdf", ".epub")
                : Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");
            if (!allowedExts.contains(ext)) {
                return Map.of("error", "不支持的文件扩展名: " + ext + "，允许: " + String.join(", ", allowedExts));
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
                    try {
                        Map<String, Object> map = new LinkedHashMap<>();
                        if (info != null && !info.isBlank()) {
                            map = objectMapper.readValue(info, new tools.jackson.core.type.TypeReference<Map<String, Object>>() {});
                        }
                        map.put("reader_url", url);
                        item.setInfoJson(objectMapper.writeValueAsString(map));
                    } catch (Exception e) {
                        log.error("Failed to update reader_url in infoJson", e);
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
    public Tag createTag(@Valid @RequestBody TagCreateReq body) {
        return tagService.create(body.getName());
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
    public List<CategorySetting> getCategorySettings() {
        return categorySettingMapper.selectList(new LambdaQueryWrapper<CategorySetting>().orderByAsc(CategorySetting::getSortOrder));
    }

    @PutMapping("/categories/settings")
    @CacheEvict(value = {"featured", "hotItems", "recommended", "visibleTypes"}, allEntries = true)
    public Map<String, String> updateCategorySettings(@RequestBody List<CategorySetting> settings) {
        for (CategorySetting s : settings) {
            String type = s.getType();
            List<String> validTypes = List.of("game", "movie", "anime", "boardgame", "model", "book", "music", "digital", "coffee", "offline");
            if (type == null || !validTypes.contains(type)) continue;
            int sortOrder = s.getSortOrder() != null ? s.getSortOrder() : 0;
            int visible = s.getVisible() != null ? s.getVisible() : 0;
            CategorySetting cs = new CategorySetting();
            cs.setType(type);
            cs.setVisible(visible);
            cs.setSortOrder(sortOrder);
            if (categorySettingMapper.selectById(type) != null) {
                categorySettingMapper.updateById(cs);
            } else {
                categorySettingMapper.insert(cs);
            }
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
    public Map<String, Object> triggerSteam(@Valid @RequestBody TriggerSteamReq body) {
        List<Integer> appIds = body.getAppIds();
        String targetType = body.getTargetType();
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
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchSteam(@RequestParam String q) {
        Map<String, Object> resp = restClient.get()
                .uri("https://store.steampowered.com/api/storesearch/?term={q}&cc=cn&l=schinese", q)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        if (resp == null) return List.of();
        List<Map<String, Object>> items = (List<Map<String, Object>>) resp.get("items");
        if (items == null) return List.of();
        
        // Batch-check types to filter out DLCs
        Set<Integer> dlcIds = new HashSet<>();
        if (!items.isEmpty()) {
            String ids = items.stream()
                    .map(it -> String.valueOf(it.get("id")))
                    .collect(Collectors.joining(","));
            try {
                Map<String, Object> check = restClient.get()
                        .uri("https://store.steampowered.com/api/appdetails?appids=" + ids + "&cc=cn&l=schinese")
                        .retrieve()
                        .body(new ParameterizedTypeReference<Map<String, Object>>() {});
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
    public Map<String, String> triggerTMDB(@Valid @RequestBody TriggerSearchReq body) {
        String query = body.getQuery();
        String targetType = body.getTargetType() != null ? body.getTargetType() : "movie";
        Map<String, Object> payload = Map.of("query", query, "targetType", targetType);
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_TMDB, payload);
        return Map.of("message", "TMDB fetch queued: " + query + " → " + targetType);
    }

    @PostMapping("/fetch/anilist")
    public Map<String, String> triggerAniList(@Valid @RequestBody TriggerSearchReq body) {
        String query = body.getQuery();
        String targetType = body.getTargetType() != null ? body.getTargetType() : "anime";
        Map<String, Object> payload = Map.of("query", query, "targetType", targetType);
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_ANILIST, payload);
        return Map.of("message", "AniList fetch queued: " + query + " → " + targetType);
    }

    @PostMapping("/fetch/bangumi")
    public Map<String, String> triggerBangumi(@Valid @RequestBody TriggerSearchReq body) {
        String query = body.getQuery();
        String targetType = body.getTargetType() != null ? body.getTargetType() : "anime";
        Map<String, Object> payload = Map.of("query", query, "targetType", targetType);
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_BANGUMI, payload);
        return Map.of("message", "Bangumi fetch queued: " + query + " → " + targetType);
    }

    @PostMapping("/fetch/tmdb-tv")
    public Map<String, String> triggerTMDBTV(@Valid @RequestBody TriggerSearchReq body) {
        String query = body.getQuery();
        String targetType = body.getTargetType() != null ? body.getTargetType() : "anime";
        Map<String, Object> payload = Map.of("query", query, "targetType", targetType);
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_TMDB_TV, payload);
        return Map.of("message", "TMDB TV fetch queued: " + query + " → " + targetType);
    }

    @PostMapping("/fetch/itunes")
    public Map<String, String> triggerItunes(@Valid @RequestBody TriggerSearchReq body) {
        String query = body.getQuery();
        String targetType = body.getTargetType() != null ? body.getTargetType() : "music";
        Map<String, Object> payload = Map.of("query", query, "targetType", targetType);
        rabbitTemplate.convertAndSend("", RabbitMQConfig.QUEUE_ITUNES, payload);
        return Map.of("message", "iTunes fetch queued: " + query + " → " + targetType);
    }

    @PostMapping("/fetch/igdb")
    public Map<String, String> triggerIGDB(@Valid @RequestBody TriggerIGDBReq body) {
        String query = body.getQuery();
        String endpoint = body.getEndpoint();
        int limit = body.getLimit();
        String targetType = body.getTargetType();
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
        itemMapper.deleteByIds(ids);
        return Map.of("message", "已批量拒绝 " + ids.size() + " 条");
    }

    // ========== App Settings (API keys) ==========

    @GetMapping("/settings")
    public Map<String, Object> getAllSettings() {
        List<AppSetting> rows = appSettingMapper.selectList(null);
        Map<String, Object> result = new LinkedHashMap<>();
        for (AppSetting row : rows) result.put(row.getSettingKey(), row.getSettingValue());
        // Ensure all known keys exist with defaults
        for (String key : List.of("STEAMGRIDDB_API_KEY", "STEAM_API_KEY", "TMDB_API_KEY", "IGDB_CLIENT_ID", "IGDB_CLIENT_SECRET",
                "TURNSTILE_SITE_KEY", "TURNSTILE_SECRET_KEY", "INVITE_ONLY")) {
            result.putIfAbsent(key, "");
        }
        return result;
    }

    @PutMapping("/settings/{key}")
    public void updateSetting(@PathVariable String key, @Valid @RequestBody SettingUpdateReq body) {
        String value = body.getValue() != null ? body.getValue() : "";
        AppSetting as = new AppSetting();
        as.setSettingKey(key);
        as.setSettingValue(value);
        if (appSettingMapper.selectById(key) != null) {
            appSettingMapper.updateById(as);
        } else {
            appSettingMapper.insert(as);
        }
    }

    // ========== SteamGridDB poster fetch ==========

    @PostMapping("/items/{id}/fetch-sgdb-poster")
    public Map<String, Object> fetchSgdbPoster(@PathVariable Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null) return Map.of("success", false, "message", "内容不存在");
        String extId = item.getExternalId();
        if (extId == null || extId.isBlank()) return Map.of("success", false, "message", "该内容没有 Steam AppID，无法拉取封面");
        Long appId;
        try { appId = Long.valueOf(extId); } catch (NumberFormatException e) { return Map.of("success", false, "message", "External ID 不是有效的 Steam AppID"); }
        // Check if API key is configured
        String posterUrl = steamGridDBService.findPosterUrl(appId);
        if (posterUrl == null) return Map.of("success", false, "message", "未找到封面（请确认已配置 SteamGridDB API Key 且该游戏有竖版封面）");
        item.setPosterUrl(posterUrl);
        itemMapper.updateById(item);
        return Map.of("success", true, "posterUrl", posterUrl, "message", "封面已更新");
    }

    // ========== Invite codes ==========

    @GetMapping("/invite-codes")
    public List<Map<String, Object>> listInviteCodes() {
        return inviteCodeMapper.selectWithUsers();
    }

    @PostMapping("/invite-codes/generate")
    public Map<String, Object> generateInviteCodes(@Valid @RequestBody InviteCodeGenerateReq body) {
        int count = body.getCount() != null ? body.getCount() : 1;
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            InviteCode ic = new InviteCode();
            ic.setCode(code);
            inviteCodeMapper.insert(ic);
            codes.add(code);
        }
        return Map.of("codes", codes, "message", "已生成 " + count + " 个邀请码");
    }

    // ========== User management ==========

    @GetMapping("/users")
    public List<Map<String, Object>> listUsers() {
        return userMapper.selectUserList();
    }

    @PutMapping("/users/{id}/ban")
    public void toggleBan(@PathVariable Long id, @Valid @RequestBody ToggleBanReq body, Authentication auth) {
        Long currentUserId = (Long) auth.getPrincipal();
        if (id.equals(currentUserId)) throw new BusinessException("不能封禁自己");
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(HttpStatus.NOT_FOUND, "用户不存在");
        if ("ADMIN".equals(user.getRole())) throw new BusinessException("不能封禁管理员");
        int enabled = body.getEnabled() != null ? body.getEnabled() : 1;
        user.setEnabled(enabled);
        userMapper.updateById(user);
    }

    @PutMapping("/users/{id}/reset-password")
    public Map<String, String> resetPassword(@PathVariable Long id) {
        String newPassword = UUID.randomUUID().toString().substring(0, 10);
        String hash = passwordEncoder.encode(newPassword);
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPasswordHash(hash);
            userMapper.updateById(user);
        }
        return Map.of("message", "密码已重置为: " + newPassword + " (请将此密码复制发送给用户，窗口关闭后将无法再次查看)");
    }
}
