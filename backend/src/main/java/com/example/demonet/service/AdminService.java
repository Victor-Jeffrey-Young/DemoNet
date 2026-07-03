package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.entity.Item;
import com.example.demonet.entity.Tag;
import com.example.demonet.entity.UserItem;
import com.example.demonet.mapper.ItemMapper;
import com.example.demonet.mapper.UserItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ItemMapper itemMapper;
    private final UserItemMapper userItemMapper;
    private final TagService tagService;
    private final JdbcTemplate jdbcTemplate;

    public IPage<Item> listAllItems(Integer page, Integer size, String type, String keyword, Integer status) {
        Page<Item> p = new Page<>(page != null ? page : 1, size != null ? size : 12);
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isBlank()) {
            wrapper.eq(Item::getType, type);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Item::getTitle, keyword).or().like(Item::getDescription, keyword));
        }
        if (status != null) {
            wrapper.eq(Item::getStatus, status);
        }
        wrapper.orderByDesc(Item::getCreatedAt);
        return itemMapper.selectPage(p, wrapper);
    }

    public Map<String, Object> getItemWithTags(Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null) return null;
        List<Tag> tags = tagService.getTagsForItem(id);
        Map<String, Object> result = new HashMap<>();
        result.put("item", item);
        result.put("tags", tags);
        return result;
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

    @Transactional
    public void deleteItem(Long id) {
        userItemMapper.delete(new LambdaQueryWrapper<UserItem>().eq(UserItem::getItemId, id));
        jdbcTemplate.update("DELETE FROM item_tag_mapping WHERE item_id = ?", id);
        itemMapper.deleteById(id);
    }

    public Item updateStatus(Long id, Integer status) {
        Item item = itemMapper.selectById(id);
        if (item != null) {
            item.setStatus(status);
            itemMapper.updateById(item);
        }
        return item;
    }

    public Item toggleFeatured(Long id) {
        Item item = itemMapper.selectById(id);
        if (item != null) {
            if (item.getCarouselOrder() != null) {
                item.setCarouselOrder(null);
            } else {
                Long max = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(carousel_order), -1) FROM items WHERE type = ?",
                    Long.class, item.getType());
                item.setCarouselOrder(max != null ? (int)(max + 1) : 0);
            }
            itemMapper.updateById(item);
        }
        return item;
    }

    public int batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        return itemMapper.deleteByIds(ids);
    }

    public int batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) return 0;
        int count = 0;
        for (Long id : ids) {
            Item item = itemMapper.selectById(id);
            if (item != null) {
                item.setStatus(status);
                itemMapper.updateById(item);
                count++;
            }
        }
        return count;
    }

    public List<Item> getCarouselItems(String type) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getType, type);
        wrapper.eq(Item::getStatus, 1);
        wrapper.isNotNull(Item::getCarouselOrder);
        wrapper.orderByAsc(Item::getCarouselOrder);
        return itemMapper.selectList(wrapper);
    }

    @Transactional
    public void saveCarouselOrder(String type, List<Long> itemIds) {
        jdbcTemplate.update("UPDATE items SET carousel_order = NULL WHERE type = ? AND carousel_order IS NOT NULL", type);
        for (int i = 0; i < itemIds.size(); i++) {
            jdbcTemplate.update("UPDATE items SET carousel_order = ? WHERE id = ?", i, itemIds.get(i));
        }
    }

    public void removeFromCarousel(Long id) {
        jdbcTemplate.update("UPDATE items SET carousel_order = NULL WHERE id = ?", id);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        Long total = itemMapper.selectCount(null);
        stats.put("total", total);

        Long pending = itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getStatus, 0));
        stats.put("pending", pending);

        Long online = itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getStatus, 1));
        stats.put("online", online);

        List<Map<String, Object>> byType = jdbcTemplate.queryForList(
                "SELECT type, COUNT(*) as count FROM items GROUP BY type ORDER BY count DESC");
        stats.put("byType", byType);

        Long tagCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tags", Long.class);
        stats.put("tagCount", tagCount);

        return stats;
    }
}
