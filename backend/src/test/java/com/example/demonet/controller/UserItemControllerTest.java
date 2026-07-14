package com.example.demonet.controller;

import com.example.demonet.entity.UserItem;
import com.example.demonet.service.UserItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.withSettings;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static com.example.demonet.test.TestAuthentication.*; // 测试认证工具

/**
 * UserItemController 单元测试。
 * 通过 MockHttpServletRequest principal 注入 Authentication 参数。
 */
@ExtendWith(MockitoExtension.class)
class UserItemControllerTest {

    private MockMvc mockMvc;
    private Authentication auth;

    @Mock
    private UserItemService userItemService;

    @BeforeEach
    void setUp() {
        auth = withUserId(1L); // 使用标准认证工具

        mockMvc = MockMvcBuilders.standaloneSetup(new UserItemController(userItemService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void save_success() throws Exception {
        UserItem ui = new UserItem();
        ui.setId(1L);
        ui.setUserId(1L);
        ui.setItemId(100L);
        ui.setStatus("want_to_play");
        when(userItemService.saveOrUpdate(eq(1L), eq(100L), eq("want_to_play"))).thenReturn(ui);

        mockMvc.perform(post("/api/user/items")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":100,\"status\":\"want_to_play\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("want_to_play"));
    }

    @Test
    void save_invalidStatus_returns400() throws Exception {
        mockMvc.perform(post("/api/user/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":100,\"status\":\"invalid_status\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void save_missingItemId_returns400() throws Exception {
        mockMvc.perform(post("/api/user/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"played\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listByUser_success() throws Exception {
        when(userItemService.listByUser(eq(1L), eq("played")))
                .thenReturn(List.of(Map.of("itemId", 100L, "status", "played")));

        mockMvc.perform(get("/api/user/items")
                        .principal(auth)
                        .param("status", "played"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemId").value(100));
    }

    @Test
    void getOne_success() throws Exception {
        UserItem ui = new UserItem();
        ui.setId(1L);
        ui.setUserId(1L);
        ui.setItemId(100L);
        ui.setStatus("played");
        when(userItemService.getStatus(1L, 100L)).thenReturn(ui);

        mockMvc.perform(get("/api/user/items/100").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("played"));
    }

    @Test
    void getOne_notFound() throws Exception {
        when(userItemService.getStatus(1L, 999L)).thenReturn(null);

        mockMvc.perform(get("/api/user/items/999").principal(auth))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void remove_success() throws Exception {
        doNothing().when(userItemService).remove(1L, 100L);

        mockMvc.perform(delete("/api/user/items/100").principal(auth))
                .andExpect(status().isOk());

        verify(userItemService).remove(1L, 100L);
    }
}
