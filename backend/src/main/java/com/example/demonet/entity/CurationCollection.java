package com.example.demonet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("curation_collections")
public class CurationCollection {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String slug;
    private String title;
    private String description;
    private String coverUrl;
    private String constraintsJson;
    private Integer visible;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
