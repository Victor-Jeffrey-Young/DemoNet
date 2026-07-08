package com.example.demonet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demonet.entity.UserItem;
import com.example.demonet.mapper.UserItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class UserItemService {

    private final UserItemMapper userItemMapper;
    private final JdbcTemplate jdbcTemplate;

    public UserItem saveOrUpdate(Long userId, Long itemId, String status) {
        UserItem existing = userItemMapper.selectOne(
                new LambdaQueryWrapper<UserItem>()
                        .eq(UserItem::getUserId, userId)
                        .eq(UserItem::getItemId, itemId));

        if (existing != null) {
            existing.setStatus(status);
            userItemMapper.updateById(existing);
            return existing;
        }

        UserItem ui = new UserItem();
        ui.setUserId(userId);
        ui.setItemId(itemId);
        ui.setStatus(status);
        ui.setCreatedAt(LocalDateTime.now());
        userItemMapper.insert(ui);
        return ui;
    }

    public List<Map<String, Object>> listByUser(Long userId, String status) {
        StringBuilder sql = new StringBuilder(
                "SELECT ui.id, ui.item_id, ui.status, ui.created_at, i.title, i.slug, i.type, i.cover_url, i.poster_url " +
                "FROM user_items ui JOIN items i ON i.id = ui.item_id " +
                "WHERE ui.user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        if (status != null && !status.isBlank()) {
            sql.append(" AND ui.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY ui.created_at DESC");

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    public void remove(Long userId, Long itemId) {
        userItemMapper.delete(
                new LambdaQueryWrapper<UserItem>()
                        .eq(UserItem::getUserId, userId)
                        .eq(UserItem::getItemId, itemId));
    }

    public UserItem getStatus(Long userId, Long itemId) {
        return userItemMapper.selectOne(
                new LambdaQueryWrapper<UserItem>()
                        .eq(UserItem::getUserId, userId)
                        .eq(UserItem::getItemId, itemId));
    }
}
