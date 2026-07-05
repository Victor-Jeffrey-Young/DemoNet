package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.entity.Item;
import com.example.demonet.mapper.ItemMapper;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemMapper itemMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /** Types visible in the frontend */
    @Cacheable(value = "visibleTypes")
    public Set<String> visibleTypes() {
        List<String> types = jdbcTemplate.queryForList(
                "SELECT type FROM category_settings WHERE visible = 1", String.class);
        return types.isEmpty() ? Set.of() : Set.copyOf(types);
    }

    /** Filter by type, respecting visibility: if type is hidden → no results */
    private void filterByType(LambdaQueryWrapper<Item> wrapper, String type) {
        if (type != null && !type.isBlank()) {
            if (!visibleTypes().contains(type)) {
                wrapper.eq(Item::getType, "__nonexistent__");
            } else {
                wrapper.eq(Item::getType, type);
            }
        } else {
            wrapper.in(Item::getType, visibleTypes());
        }
    }

    public IPage<Item> listItems(Integer page, Integer size, String type, String keyword, List<String> tags) {
        Page<Item> p = new Page<>(page != null ? page : 1, size != null ? size : 12);
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getStatus, 1);
        filterByType(wrapper, type);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Item::getTitle, keyword).or().like(Item::getDescription, keyword));
        }
        if (tags != null && !tags.isEmpty()) {
            List<Long> itemIds = getItemIdsByTags(tags);
            if (itemIds.isEmpty()) return new Page<>(p.getCurrent(), p.getSize(), 0);
            wrapper.in(Item::getId, itemIds);
        }
        wrapper.orderByDesc(Item::getCreatedAt);
        return itemMapper.selectPage(p, wrapper);
    }

    public Item getBySlug(String slug) {
        return itemMapper.selectOne(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getSlug, slug)
                        .eq(Item::getStatus, 1)
        );
    }

    @CacheEvict(value = {"hotItems", "featured", "recommended"}, allEntries = true)
    public Item createItem(Item item) {
        itemMapper.insert(item);
        return item;
    }

    @CacheEvict(value = {"hotItems", "featured", "recommended"}, allEntries = true)
    public Item updateItem(Long id, Item item) {
        item.setId(id);
        itemMapper.updateById(item);
        return itemMapper.selectById(id);
    }

    @CacheEvict(value = {"hotItems", "featured", "recommended"}, allEntries = true)
    public void deleteItem(Long id) {
        itemMapper.deleteById(id);
    }

    /** Merge fresh API infoJson into existing, keeping manual fields */
    @SuppressWarnings("unchecked")
    private String mergeInfoJson(String existingJson, String freshJson) {
        try {
            Map<String, Object> e = objectMapper.readValue(
                    existingJson != null && !existingJson.isBlank() ? existingJson : "{}", Map.class);
            Map<String, Object> f = objectMapper.readValue(
                    freshJson != null && !freshJson.isBlank() ? freshJson : "{}", Map.class);

            String[] apiFields = {"developer","publisher","genre","platform","price",
                    "release_date","languages","screenshots","dlc","features",
                    "min_requirements","rec_requirements","free","is_dlc"};
            for (String field : apiFields) {
                if (f.containsKey(field)) e.put(field, f.get(field));
            }

            // videos: only update steam, keep youtube/bilibili
            Map<String, Object> fv = (Map<String, Object>) f.get("videos");
            Map<String, Object> ev = (Map<String, Object>) e.get("videos");
            if (ev == null) ev = new java.util.LinkedHashMap<>();
            if (fv != null && fv.get("steam") != null) ev.put("steam", fv.get("steam"));
            ev.putIfAbsent("youtube", "");
            ev.putIfAbsent("bilibili", "");
            e.put("videos", ev);

            return objectMapper.writeValueAsString(e);
        } catch (Exception ex) {
            return freshJson != null ? freshJson : existingJson;
        }
    }

    /** Update an existing item by slug (for re-fetch from external sources) */
    @CacheEvict(value = {"hotItems", "featured", "recommended"}, allEntries = true)
    public void updateBySlug(Item fresh) {
        // Find by slug regardless of status (may be status=0 still pending approval)
        Item existing = itemMapper.selectOne(
                new LambdaQueryWrapper<Item>().eq(Item::getSlug, fresh.getSlug()));
        if (existing == null) return;
        // Update fields that may have changed
        if (fresh.getCoverUrl() != null && !fresh.getCoverUrl().isBlank())
            existing.setCoverUrl(fresh.getCoverUrl());
        // Preserve manually-set poster URL (e.g. SteamGridDB, upload) — only overwrite if blank or old CDN auto-generated
        String existingPoster = existing.getPosterUrl();
        boolean isAutoPoster = existingPoster == null || existingPoster.isBlank()
                || existingPoster.contains("/library_600x900") || existingPoster.contains("/header");
        if (fresh.getPosterUrl() != null && !fresh.getPosterUrl().isBlank() && isAutoPoster)
            existing.setPosterUrl(fresh.getPosterUrl());
        if (fresh.getDescription() != null && !fresh.getDescription().isBlank())
            existing.setDescription(fresh.getDescription());
        if (fresh.getExternalLink() != null && !fresh.getExternalLink().isBlank())
            existing.setExternalLink(fresh.getExternalLink());

        // Merge infoJson: keep manual fields (YouTube, B站, demo_url etc) and only update API fields
        existing.setInfoJson(mergeInfoJson(existing.getInfoJson(), fresh.getInfoJson()));
        existing.setStatus(0); // Re-queue for approval
        if (fresh.getRecommendations() != null && fresh.getRecommendations() > 0)
            existing.setRecommendations(fresh.getRecommendations());
        itemMapper.updateById(existing);
    }

    @Cacheable(value = "hotItems", key = "#type + '_' + #limit")
    public List<Item> listHotItems(String type, Integer limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT i.id, i.type, i.title, i.slug, i.cover_url, i.wide_cover_url, i.poster_url, " +
                "i.description, i.info_json, i.media_url, i.external_link, i.external_id, i.source, " +
                "i.carousel_order, i.recommendations, i.hot_boost, i.created_at, i.updated_at, i.status, " +
                "COUNT(r.id) AS recent_reviews " +
                "FROM items i LEFT JOIN reviews r ON i.id = r.item_id AND r.created_at > DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                "WHERE i.status = 1");
        List<Object> params = new java.util.ArrayList<>();
        if (type != null && !type.isBlank()) {
            sql.append(" AND i.type = ?");
            params.add(type);
        }
        sql.append(" GROUP BY i.id ");
        sql.append(" ORDER BY (LN(i.recommendations + 1) * 100 + i.hot_boost + COUNT(r.id) * 500) / GREATEST(DATEDIFF(NOW(), i.created_at), 1) DESC LIMIT ?");
        params.add(limit != null ? limit : 6);

        return jdbcTemplate.query(sql.toString(), ps -> {
            for (int j = 0; j < params.size(); j++) {
                ps.setObject(j + 1, params.get(j));
            }
        }, (rs, rowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setType(rs.getString("type"));
            item.setTitle(rs.getString("title"));
            item.setSlug(rs.getString("slug"));
            item.setCoverUrl(rs.getString("cover_url"));
            item.setWideCoverUrl(rs.getString("wide_cover_url"));
            item.setPosterUrl(rs.getString("poster_url"));
            item.setDescription(rs.getString("description"));
            item.setInfoJson(rs.getString("info_json"));
            item.setMediaUrl(rs.getString("media_url"));
            item.setExternalLink(rs.getString("external_link"));
            item.setExternalId(rs.getString("external_id"));
            item.setSource(rs.getString("source"));
            item.setStatus(rs.getInt("status"));
            item.setCarouselOrder(rs.getObject("carousel_order") != null ? rs.getInt("carousel_order") : null);
            item.setRecommendations(rs.getInt("recommendations"));
            item.setHotBoost(rs.getInt("hot_boost"));
            item.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            item.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return item;
        });
    }

    @Cacheable(value = "featured", key = "#type != null ? #type : 'all'")
    public List<Item> listFeatured(String type) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Item.class, info -> !info.getColumn().equals("description") && !info.getColumn().equals("info_json"));
        wrapper.eq(Item::getStatus, 1);
        wrapper.isNotNull(Item::getCarouselOrder);
        filterByType(wrapper, type);
        wrapper.orderByAsc(Item::getCarouselOrder);
        return itemMapper.selectList(wrapper);
    }

    public List<Item> listByType(String type) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Item.class, info -> !info.getColumn().equals("description") && !info.getColumn().equals("info_json"));
        wrapper.eq(Item::getStatus, 1);
        filterByType(wrapper, type);
        wrapper.orderByDesc(Item::getCreatedAt);
        return itemMapper.selectList(wrapper);
    }

    @Cacheable(value = "recommended", key = "#type + '_' + #limit")
    public List<Item> listRecommended(String type, Integer limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT i.id, i.type, i.title, i.slug, i.cover_url, i.wide_cover_url, i.poster_url, " +
                "i.description, i.info_json, i.external_id, i.source, " +
                "i.recommendations, i.hot_boost, i.created_at, i.updated_at, i.status FROM items i LEFT JOIN " +
                "(SELECT item_id, COUNT(*) AS cnt FROM user_items GROUP BY item_id) ui " +
                "ON i.id = ui.item_id WHERE i.status = 1");
        boolean hasType = type != null && !type.isBlank();
        if (hasType) {
            if (!visibleTypes().contains(type)) return List.of();
            sql.append(" AND i.type = ?");
        } else {
            sql.append(" AND i.type IN (SELECT type FROM category_settings WHERE visible = 1)");
        }
        sql.append(" ORDER BY IFNULL(ui.cnt, 0) DESC, i.created_at DESC LIMIT ?");
        return jdbcTemplate.query(sql.toString(), ps -> {
            int idx = 1;
            if (hasType) ps.setString(idx++, type);
            ps.setInt(idx, limit != null ? limit : 6);
        }, (rs, rowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setType(rs.getString("type"));
            item.setTitle(rs.getString("title"));
            item.setSlug(rs.getString("slug"));
            item.setCoverUrl(rs.getString("cover_url"));
            item.setWideCoverUrl(rs.getString("wide_cover_url"));
            item.setDescription(rs.getString("description"));
            item.setInfoJson(rs.getString("info_json"));
            item.setExternalId(rs.getString("external_id"));
            item.setSource(rs.getString("source"));
            item.setStatus(rs.getInt("status"));
            item.setCreatedAt(rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime() : null);
            item.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                    rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return item;
        });
    }

    private List<Long> getItemIdsByTags(List<String> tagNames) {
        if (tagNames.isEmpty()) return Collections.emptyList();
        String placeholders = String.join(",", Collections.nCopies(tagNames.size(), "?"));
        String sql = "SELECT DISTINCT itm.item_id FROM item_tag_mapping itm " +
                     "JOIN tags t ON t.id = itm.tag_id WHERE t.name IN (" + placeholders + ")";
        return jdbcTemplate.queryForList(sql, Long.class, tagNames.toArray());
    }
}
