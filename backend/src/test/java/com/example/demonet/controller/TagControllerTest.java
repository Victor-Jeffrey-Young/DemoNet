package com.example.demonet.controller;

import com.example.demonet.entity.Tag;
import com.example.demonet.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TagController 单元测试（Standalone MockMvc）。
 * 覆盖公开和写入端点。
 */
@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TagService tagService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TagController(tagService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listAll_success() throws Exception {
        Tag t1 = new Tag();
        t1.setId(1L);
        t1.setName("action");
        Tag t2 = new Tag();
        t2.setId(2L);
        t2.setName("rpg");
        when(tagService.listAll()).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("action"));
    }

    @Test
    void getTagsForItem_success() throws Exception {
        Tag t = new Tag();
        t.setId(1L);
        t.setName("action");
        when(tagService.getTagsForItem(100L)).thenReturn(List.of(t));

        mockMvc.perform(get("/api/tags/items/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("action"));
    }

    @Test
    void create_success() throws Exception {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("newtag");
        when(tagService.create("newtag")).thenReturn(tag);

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"newtag\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("newtag"));
    }

    @Test
    void delete_success() throws Exception {
        doNothing().when(tagService).delete(1L);

        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isOk());

        verify(tagService).delete(1L);
    }

    @Test
    void associateTags_success() throws Exception {
        doNothing().when(tagService).associateItem(eq(100L), anyList());

        mockMvc.perform(post("/api/tags/items/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tagIds\":[1,2,3]}"))
                .andExpect(status().isOk());

        verify(tagService).associateItem(eq(100L), argThat(ids -> ids.size() == 3));
    }
}
