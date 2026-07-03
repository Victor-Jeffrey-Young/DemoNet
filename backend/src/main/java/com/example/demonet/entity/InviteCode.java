package com.example.demonet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("invite_codes")
public class InviteCode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private Long usedBy;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
}
