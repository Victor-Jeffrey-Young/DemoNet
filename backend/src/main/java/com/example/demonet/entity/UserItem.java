package com.example.demonet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_items")
public class UserItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long itemId;
    private String status;
    private Integer playedDuration;
    private String note;
    private LocalDateTime createdAt;
}
