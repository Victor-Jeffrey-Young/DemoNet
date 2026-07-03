package com.example.demonet.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("app_settings")
public class AppSetting {
    @TableId
    private String settingKey;
    private String settingValue;
}
