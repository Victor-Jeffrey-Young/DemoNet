package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.entity.Tag;
import com.example.demonet.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;
    private final JdbcTemplate jdbcTemplate;

    public List<Tag> listAll() {
        return tagMapper.selectList(new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getId));
    }

    public IPage<Tag> listPaginated(int page, int size, String keyword) {
        Page<Tag> p = new Page<>(page, size);
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<Tag>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Tag::getName, keyword);
        }
        wrapper.orderByAsc(Tag::getId);
        return tagMapper.selectPage(p, wrapper);
    }

    public Tag create(String name) {
        if (name == null || name.isBlank()) {
            throw new RuntimeException("标签名不能为空");
        }
        Long exists = tagMapper.selectCount(
                new LambdaQueryWrapper<Tag>().eq(Tag::getName, name));
        if (exists != null && exists > 0) {
            throw new RuntimeException("标签「" + name + "」已存在");
        }
        Tag tag = new Tag();
        tag.setName(name);
        tagMapper.insert(tag);
        return tag;
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM item_tag_mapping WHERE tag_id = ?", id);
        tagMapper.deleteById(id);
    }

    @Transactional
    public void associateItem(Long itemId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            jdbcTemplate.update(
                    "INSERT IGNORE INTO item_tag_mapping (item_id, tag_id) VALUES (?, ?)",
                    itemId, tagId);
        }
    }

    /** Get or create a tag by name, return its ID */
    public Long ensureTag(String name) {
        var existing = tagMapper.selectOne(
                new LambdaQueryWrapper<Tag>().eq(Tag::getName, name));
        if (existing != null) return existing.getId();
        Tag tag = new Tag();
        tag.setName(name);
        tagMapper.insert(tag);
        return tag.getId();
    }

    /** Associate a tag by name with an item */
    public void addItemTag(Long itemId, String tagName) {
        Long tagId = ensureTag(tagName);
        jdbcTemplate.update(
                "INSERT IGNORE INTO item_tag_mapping (item_id, tag_id) VALUES (?, ?)",
                itemId, tagId);
    }

    public void removeItemTag(Long itemId, Long tagId) {
        jdbcTemplate.update("DELETE FROM item_tag_mapping WHERE item_id = ? AND tag_id = ?", itemId, tagId);
    }

    public List<Long> getItemIdsByTagNames(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(tagNames.size(), "?"));
        String sql = "SELECT DISTINCT itm.item_id FROM item_tag_mapping itm " +
                     "JOIN tags t ON t.id = itm.tag_id WHERE t.name IN (" + placeholders + ")";
        return jdbcTemplate.queryForList(sql, Long.class, tagNames.toArray());
    }

    public List<Tag> getTagsForItem(Long itemId) {
        String sql = "SELECT t.* FROM tags t " +
                     "JOIN item_tag_mapping itm ON t.id = itm.tag_id " +
                     "WHERE itm.item_id = ? ORDER BY t.id";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    Tag t = new Tag();
                    t.setId(rs.getLong("id"));
                    t.setName(rs.getString("name"));
                    return t;
                }, itemId);
    }
}
