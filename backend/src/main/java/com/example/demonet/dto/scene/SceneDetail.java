package com.example.demonet.dto.scene;

import lombok.Data;

import java.util.List;

@Data
public class SceneDetail extends SceneSummary {
    private List<SceneItemSummary> items;
}
