package com.example.demonet.controller;

import com.example.demonet.common.BusinessException;
import com.example.demonet.dto.scene.SceneDetail;
import com.example.demonet.dto.scene.SceneSummary;
import com.example.demonet.service.SceneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SceneControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SceneService sceneService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SceneController(sceneService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listReturnsVisibleSceneSummariesWithDefaultLimit() throws Exception {
        SceneSummary scene = new SceneSummary();
        scene.setSlug("solo-quick-start");
        scene.setTitle("一个人，30 分钟内，在线开始");
        when(sceneService.listVisibleScenes(4)).thenReturn(List.of(scene));

        mockMvc.perform(get("/api/scenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("solo-quick-start"))
                .andExpect(jsonPath("$[0].title").value("一个人，30 分钟内，在线开始"));
    }

    @Test
    void listRejectsLimitOutsidePublicContract() throws Exception {
        mockMvc.perform(get("/api/scenes").param("limit", "7"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void detailReturnsPublishedSceneBySlug() throws Exception {
        SceneDetail scene = new SceneDetail();
        scene.setSlug("friends-night");
        scene.setTitle("3–5 人的宿舍/朋友夜");
        when(sceneService.getVisibleScene("friends-night")).thenReturn(scene);

        mockMvc.perform(get("/api/scenes/friends-night"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("friends-night"))
                .andExpect(jsonPath("$.title").value("3–5 人的宿舍/朋友夜"));
    }

    @Test
    void detailReturnsNotFoundForHiddenOrMissingScene() throws Exception {
        when(sceneService.getVisibleScene("missing"))
                .thenThrow(new BusinessException(HttpStatus.NOT_FOUND, "场景不存在或未发布"));

        mockMvc.perform(get("/api/scenes/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
