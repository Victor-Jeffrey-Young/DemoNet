package com.example.demonet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

public class AdminRequests {

    @Data
    public static class UpdateStatusReq {
        @NotNull(message = "状态不能为空")
        private Integer status;
    }

    @Data
    public static class BatchDeleteReq {
        @NotEmpty(message = "ID列表不能为空")
        private List<Long> ids;
    }

    @Data
    public static class BatchStatusUpdateReq {
        @NotEmpty(message = "ID列表不能为空")
        private List<Long> ids;
        @NotNull(message = "状态不能为空")
        private Integer status;
    }

    @Data
    public static class CarouselSaveReq {
        @NotNull(message = "ID列表不能为Null")
        private List<Long> itemIds;
    }

    @Data
    public static class TagCreateReq {
        @NotBlank(message = "标签名称不能为空")
        private String name;
    }

    @Data
    public static class TriggerSteamReq {
        @NotEmpty(message = "App ID不能为空")
        private List<Integer> appIds;
        private String targetType = "game";
    }

    @Data
    public static class TriggerSearchReq {
        @NotBlank(message = "搜索关键词不能为空")
        private String query;
        private String targetType;
        private String externalId;
    }

    @Data
    public static class TriggerIGDBReq {
        private String query;
        private String endpoint = "search";
        private Integer limit = 10;
        private String targetType = "game";
    }

    @Data
    public static class SettingUpdateReq {
        private String value = "";
    }

    @Data
    public static class InviteCodeGenerateReq {
        private Integer count = 1;
    }

    @Data
    public static class ToggleBanReq {
        private Integer enabled = 1;
    }
}
