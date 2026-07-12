package com.example.demonet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demonet.entity.Item;
import com.example.demonet.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ItemController 单元测试（Standalone MockMvc）。
 * 覆盖公开读取端点：列表 / 详情 / 热门 / 类型 / 推荐 / 特色。
 */
@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ItemController(itemService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void list_defaultPagination() throws Exception {
        Page<Item> page = new Page<>();
        page.setRecords(List.of(new Item()));
        page.setTotal(1);
        when(itemService.listItems(anyInt(), anyInt(), isNull(), isNull(), isNull())).thenReturn(page);

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records").isArray())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void list_withFilters() throws Exception {
        Page<Item> page = new Page<>();
        page.setRecords(List.of());
        page.setTotal(0);
        when(itemService.listItems(eq(1), eq(12), eq("game"), eq("hades"), any())).thenReturn(page);

        mockMvc.perform(get("/api/items")
                        .param("type", "game")
                        .param("keyword", "hades")
                        .param("tags", "action,rpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void detail_found() throws Exception {
        Item item = new Item();
        item.setId(1L);
        item.setSlug("hades-2");
        item.setTitle("Hades II");
        when(itemService.getBySlug("hades-2")).thenReturn(item);

        mockMvc.perform(get("/api/items/hades-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("hades-2"))
                .andExpect(jsonPath("$.title").value("Hades II"));
    }

    @Test
    void detail_notFound() throws Exception {
        when(itemService.getBySlug("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/api/items/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void hot_withType() throws Exception {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Hot Game");
        when(itemService.listHotItems("game", 6)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/items/hot")
                        .param("type", "game"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Hot Game"));
    }

    @Test
    void byType_success() throws Exception {
        Item item = new Item();
        item.setId(1L);
        when(itemService.listByType("movie")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/items/types")
                        .param("type", "movie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void recommended_success() throws Exception {
        when(itemService.listRecommended("game", 6)).thenReturn(List.of(new Item()));

        mockMvc.perform(get("/api/items/recommended")
                        .param("type", "game"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void featured_success() throws Exception {
        when(itemService.listFeatured("game")).thenReturn(List.of(new Item()));

        mockMvc.perform(get("/api/items/featured")
                        .param("type", "game"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
