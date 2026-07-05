package com.example.demonet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户收藏/标记 Item 的请求体（审计报告 QUAL-6）。
 * 替代原先直接从 Map 取值的无校验写法。
 */
@Data
public class UserItemSaveRequest {

    @NotNull(message = "itemId 不能为空")
    private Long itemId;

    @NotBlank(message = "status 不能为空")
    @Pattern(regexp = "want_to_play|played|loved|dropped",
             message = "status 必须是 want_to_play / played / loved / dropped 之一")
    private String status;
}
