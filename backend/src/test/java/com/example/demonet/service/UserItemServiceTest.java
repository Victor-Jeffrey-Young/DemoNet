package com.example.demonet.service;

import com.example.demonet.entity.UserItem;
import com.example.demonet.mapper.UserItemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserItemService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserItemServiceTest {

    @InjectMocks
    private UserItemService userItemService;

    @Mock private UserItemMapper userItemMapper;
    @Mock private JdbcTemplate jdbcTemplate;

    @Test
    void saveOrUpdate_new() {
        when(userItemMapper.selectOne(any())).thenReturn(null);
        when(userItemMapper.insert((UserItem) any())).thenReturn(1);

        UserItem result = userItemService.saveOrUpdate(1L, 100L, "want_to_play");

        assertThat(result.getStatus()).isEqualTo("want_to_play");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getItemId()).isEqualTo(100L);
        verify(userItemMapper).insert((UserItem) any());
    }

    @Test
    void saveOrUpdate_update() {
        UserItem existing = new UserItem();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setItemId(100L);
        existing.setStatus("played");
        when(userItemMapper.selectOne(any())).thenReturn(existing);

        UserItem result = userItemService.saveOrUpdate(1L, 100L, "loved");

        assertThat(result.getStatus()).isEqualTo("loved");
        verify(userItemMapper).updateById(existing);
        verify(userItemMapper, never()).insert((UserItem) any());
    }

    @Test
    void remove_success() {
        when(userItemMapper.delete(any())).thenReturn(1);

        userItemService.remove(1L, 100L);

        verify(userItemMapper).delete(any());
    }

    @Test
    void getStatus_found() {
        UserItem ui = new UserItem();
        ui.setId(1L);
        ui.setUserId(1L);
        ui.setItemId(100L);
        ui.setStatus("played");
        when(userItemMapper.selectOne(any())).thenReturn(ui);

        UserItem result = userItemService.getStatus(1L, 100L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("played");
    }

    @Test
    void getStatus_notFound() {
        when(userItemMapper.selectOne(any())).thenReturn(null);

        UserItem result = userItemService.getStatus(1L, 999L);

        assertThat(result).isNull();
    }
}
