package com.example.demonet.dto.scene;

import lombok.Data;

@Data
public class AdminSceneItem {
    private Long id;
    private String type;
    private String title;
    private String slug;
    private String coverUrl;
    private Integer status;
    private String reason;
    private String editorNote;
}
