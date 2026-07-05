package com.example.demonet.common;

import org.springframework.http.HttpStatus;

/**
 * 业务异常基类（审计报告 QUAL-1）。
 * 由 GlobalExceptionHandler 捕获并按 {@link #status} 返回对应的 HTTP 状态码，
 * 区别于系统级 RuntimeException（统一返回 500）。
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public BusinessException(String message) {
        this(HttpStatus.BAD_REQUEST, message);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
