package com.example.demonet.dto.scene;

import lombok.Data;

@Data
public class SceneItemSummary {
    private Long id;
    private String type;
    private String title;
    private String slug;
    private String coverUrl;
    private String wideCoverUrl;
    private String description;
    private String reason;
}
