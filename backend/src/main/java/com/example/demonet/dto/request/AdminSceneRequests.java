package com.example.demonet.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

public class AdminSceneRequests {

    @Data
    public static class SceneSaveReq {
        @NotBlank(message = "场景 slug 不能为空")
        @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*", message = "场景 slug 只能使用小写字母、数字和连字符")
        @Size(max = 100, message = "场景 slug 不能超过 100 个字符")
        private String slug;
        @NotBlank(message = "场景标题不能为空")
        @Size(max = 120, message = "场景标题不能超过 120 个字符")
        private String title;
        @NotBlank(message = "场景简介不能为空")
        @Size(max = 500, message = "场景简介不能超过 500 个字符")
        private String description;
        @Size(max = 512, message = "封面链接不能超过 512 个字符")
        private String coverUrl;
        private String constraintsJson;
        @NotNull(message = "可见状态不能为空")
        @Min(value = 0, message = "可见状态只能为 0 或 1")
        @Max(value = 1, message = "可见状态只能为 0 或 1")
        private Integer visible;
        @NotNull(message = "排序不能为空")
        @Min(value = 0, message = "排序不能小于 0")
        private Integer displayOrder;
    }

    @Data
    public static class SceneItemsSaveReq {
        @NotNull(message = "场景条目列表不能为空")
        private List<@Valid SceneItemReq> items;
    }

    @Data
    public static class SceneItemReq {
        @NotNull(message = "内容 ID 不能为空")
        private Long itemId;
        @NotBlank(message = "入选理由不能为空")
        @Size(max = 300, message = "入选理由不能超过 300 个字符")
        private String reason;
        @Size(max = 5000, message = "编辑备注不能超过 5000 个字符")
        private String editorNote;
    }
}
