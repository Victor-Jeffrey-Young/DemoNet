package com.example.demonet.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("category_settings")
public class CategorySetting {
    @TableId
    private String type;
    private Integer visible;
    private Integer sortOrder;
}
