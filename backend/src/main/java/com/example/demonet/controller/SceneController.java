package com.example.demonet.controller;

import com.example.demonet.common.BusinessException;
import com.example.demonet.dto.scene.SceneDetail;
import com.example.demonet.dto.scene.SceneSummary;
import com.example.demonet.service.SceneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/scenes")
@RequiredArgsConstructor
public class SceneController {

    private static final int MAX_LIMIT = 6;

    private final SceneService sceneService;

    @GetMapping
    public List<SceneSummary> list(@RequestParam(defaultValue = "4") int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "limit 必须在 1 到 6 之间");
        }
        return sceneService.listVisibleScenes(limit);
    }

    @GetMapping("/{slug}")
    public SceneDetail detail(@PathVariable String slug) {
        return sceneService.getVisibleScene(slug);
    }
}
