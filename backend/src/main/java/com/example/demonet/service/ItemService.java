package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.entity.Item;
import com.example.demonet.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemMapper itemMapper;
    private final JdbcTemplate jdbcTemplate;

    public IPage<Item> listItems(Integer page, Integer size, String type, String keyword, List<String> tags) {
        Page<Item> p = new Page<>(page != null ? page : 1, size != null ? size : 12);
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getStatus, 1);
        if (type != null && !type.isBlank()) {
            wrapper.eq(Item::getType, type);
        }
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

    public Item createItem(Item item) {
        itemMapper.insert(item);
        return item;
    }

    public Item updateItem(Long id, Item item) {
        item.setId(id);
        itemMapper.updateById(item);
        return itemMapper.selectById(id);
    }

    public void deleteItem(Long id) {
        itemMapper.deleteById(id);
    }

    public List<Item> listHotItems(String type, Integer limit) {
        Page<Item> p = new Page<>(1, limit != null ? limit : 6);
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getStatus, 1);
        if (type != null && !type.isBlank()) {
            wrapper.eq(Item::getType, type);
        }
        wrapper.orderByDesc(Item::getCreatedAt);
        return itemMapper.selectPage(p, wrapper).getRecords();
    }

    public List<Item> listFeatured(String type) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getStatus, 1);
        wrapper.isNotNull(Item::getCarouselOrder);
        if (type != null && !type.isBlank()) {
            wrapper.eq(Item::getType, type);
        }
        wrapper.orderByAsc(Item::getCarouselOrder);
        return itemMapper.selectList(wrapper);
    }

    public List<Item> listByType(String type) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getStatus, 1);
        if (type != null && !type.isBlank()) {
            wrapper.eq(Item::getType, type);
        }
        wrapper.orderByDesc(Item::getCreatedAt);
        return itemMapper.selectList(wrapper);
    }

    public List<Item> listRecommended(String type, Integer limit) {
        String sql = "SELECT i.* FROM items i LEFT JOIN " +
                     "(SELECT item_id, COUNT(*) AS cnt FROM user_items GROUP BY item_id) ui " +
                     "ON i.id = ui.item_id WHERE i.status = 1";
        if (type != null && !type.isBlank()) {
            sql += " AND i.type = '" + type.replace("'", "''") + "'";
        }
        sql += " ORDER BY IFNULL(ui.cnt, 0) DESC, i.created_at DESC LIMIT " +
               (limit != null ? limit : 6);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
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
        String placeholders = String.join(",", tagNames.stream().map(s -> "'" + s.replace("'", "''") + "'").toList());
        String sql = "SELECT DISTINCT itm.item_id FROM item_tag_mapping itm " +
                     "JOIN tags t ON t.id = itm.tag_id WHERE t.name IN (" + placeholders + ")";
        return jdbcTemplate.queryForList(sql, Long.class);
    }
}
