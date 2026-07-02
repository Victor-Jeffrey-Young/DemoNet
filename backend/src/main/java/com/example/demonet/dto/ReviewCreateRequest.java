package com.example.demonet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewCreateRequest {
    @NotNull(message = "itemId 不能为空")
    private Long itemId;

    @Min(value = 1, message = "评分范围 1-5")
    @Max(value = 5, message = "评分范围 1-5")
    private Integer rating;

    @Size(max = 2000, message = "评论不能超过 2000 字")
    private String comment;
}