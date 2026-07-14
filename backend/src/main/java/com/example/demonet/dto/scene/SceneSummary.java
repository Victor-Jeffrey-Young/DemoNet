package com.example.demonet.dto.scene;

import lombok.Data;

@Data
public class SceneSummary {
    private String slug;
    private String title;
    private String description;
    private String coverUrl;
    private String constraintsJson;
}
