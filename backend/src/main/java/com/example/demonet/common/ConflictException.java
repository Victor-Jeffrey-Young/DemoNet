package com.example.demonet.common;

import org.springframework.http.HttpStatus;

/**
 * 资源冲突异常（审计报告 QUAL-1）。
 * 用于"用户名已存在""邮箱已被注册"等场景，对应 HTTP 409 Conflict。
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
