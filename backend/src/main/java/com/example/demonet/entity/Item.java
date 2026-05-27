package com.example.demonet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("items")
public class Item {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;
    private String title;
    private String slug;
    private String coverUrl;
    private String wideCoverUrl;
    private String description;
    private String infoJson;
    private String externalId;
    private String source;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
