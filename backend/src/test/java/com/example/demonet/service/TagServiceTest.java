package com.example.demonet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.entity.Tag;
import com.example.demonet.mapper.TagMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TagService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @InjectMocks
    private TagService tagService;

    @Mock private TagMapper tagMapper;
    @Mock private JdbcTemplate jdbcTemplate;

    @Test
    void listAll_success() {
        Tag t1 = new Tag();
        t1.setId(1L);
        t1.setName("action");
        Tag t2 = new Tag();
        t2.setId(2L);
        t2.setName("rpg");
        when(tagMapper.selectList(any())).thenReturn(List.of(t1, t2));

        List<Tag> result = tagService.listAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("action");
    }

    @Test
    void create_success() {
        when(tagMapper.selectCount(any())).thenReturn(0L);
        when(tagMapper.insert(any(Tag.class))).thenReturn(1);

        Tag result = tagService.create("newtag");

        assertThat(result.getName()).isEqualTo("newtag");
        verify(tagMapper).insert(any(Tag.class));
    }

    @Test
    void create_emptyName() {
        assertThatThrownBy(() -> tagService.create(""))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("不能为空");
    }

    @Test
    void create_nullName() {
        assertThatThrownBy(() -> tagService.create(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("不能为空");
    }

    @Test
    void create_duplicate() {
        when(tagMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> tagService.create("existing"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("已存在");
    }

    @Test
    void ensureTag_existing() {
        Tag existing = new Tag();
        existing.setId(5L);
        existing.setName("action");
        when(tagMapper.selectOne(any())).thenReturn(existing);

        Long tagId = tagService.ensureTag("action");

        assertThat(tagId).isEqualTo(5L);
        verify(tagMapper, never()).insert(any());
    }

    @Test
    void ensureTag_new() {
        when(tagMapper.selectOne(any())).thenReturn(null);
        when(tagMapper.insert(any(Tag.class))).thenAnswer(invocation -> {
            Tag t = invocation.getArgument(0);
            t.setId(10L);
            return 1;
        });

        Long tagId = tagService.ensureTag("newtag");

        assertThat(tagId).isEqualTo(10L);
        verify(tagMapper).insert(any(Tag.class));
    }

    @Test
    void delete_success() {
        when(jdbcTemplate.update(anyString(), eq(1L))).thenReturn(1);
        when(tagMapper.deleteById(1L)).thenReturn(1);

        tagService.delete(1L);

        verify(jdbcTemplate).update(anyString(), eq(1L));
        verify(tagMapper).deleteById(1L);
    }

    @Test
    void listPaginated_withKeyword() {
        Page<Tag> mockPage = new Page<>();
        Tag t = new Tag();
        t.setId(1L);
        t.setName("action");
        mockPage.setRecords(List.of(t));
        mockPage.setTotal(1);
        when(tagMapper.selectPage(any(), any())).thenReturn(mockPage);

        var result = tagService.listPaginated(1, 10, "act");

        assertThat(result.getRecords()).hasSize(1);
    }
}
