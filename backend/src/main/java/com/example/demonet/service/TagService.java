package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demonet.entity.Tag;
import com.example.demonet.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;
    private final JdbcTemplate jdbcTemplate;

    public List<Tag> listAll() {
        return tagMapper.selectList(new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getId));
    }

    public Tag create(String name) {
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

    public List<Long> getItemIdsByTagNames(List<String> tagNames) {
        String sql = "SELECT DISTINCT itm.item_id FROM item_tag_mapping itm " +
                     "JOIN tags t ON t.id = itm.tag_id WHERE t.name IN (" +
                     String.join(",", tagNames.stream().map(s -> "'" + s + "'").toList()) + ")";
        return jdbcTemplate.queryForList(sql, Long.class);
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
