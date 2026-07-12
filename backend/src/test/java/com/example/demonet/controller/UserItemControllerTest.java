package com.example.demonet.controller;

import com.example.demonet.entity.UserItem;
import com.example.demonet.service.UserItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.withSettings;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserItemController 单元测试（Standalone MockMvc）。
 * 使用 SecurityContextHolder + 自定义 HandlerMethodArgumentResolver 解析 Authentication 参数。
 */
@ExtendWith(MockitoExtension.class)
class UserItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserItemService userItemService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(mockAuth(1L));

        mockMvc = MockMvcBuilders.standaloneSetup(new UserItemController(userItemService))
                .setCustomArgumentResolvers(new AuthResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Authentication mockAuth(Long userId) {
        Authentication auth = mock(Authentication.class, withSettings().lenient());
        when(auth.getPrincipal()).thenReturn(userId);
        return auth;
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

        mockMvc.perform(get("/api/user/items/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("played"));
    }

    @Test
    void getOne_notFound() throws Exception {
        when(userItemService.getStatus(1L, 999L)).thenReturn(null);

        mockMvc.perform(get("/api/user/items/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void remove_success() throws Exception {
        doNothing().when(userItemService).remove(1L, 100L);

        mockMvc.perform(delete("/api/user/items/100"))
                .andExpect(status().isOk());

        verify(userItemService).remove(1L, 100L);
    }

    /**
     * 自定义 HandlerMethodArgumentResolver：从 SecurityContextHolder 提供 Authentication 参数。
     */
    private static class AuthResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return Authentication.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return SecurityContextHolder.getContext().getAuthentication();
        }
    }
}
