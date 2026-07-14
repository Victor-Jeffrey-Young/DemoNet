package com.example.demonet.controller;

import com.example.demonet.entity.CurationCollection;
import com.example.demonet.service.AdminSceneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminSceneControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminSceneService adminSceneService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminSceneController(adminSceneService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listIncludesHiddenScenesForCuration() throws Exception {
        CurationCollection hidden = new CurationCollection();
        hidden.setId(7L);
        hidden.setSlug("solo-quick-start");
        hidden.setVisible(0);
        when(adminSceneService.listCollections()).thenReturn(List.of(hidden));

        mockMvc.perform(get("/api/admin/scenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("solo-quick-start"))
                .andExpect(jsonPath("$[0].visible").value(0));
    }
}
