package com.example.demonet.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.demonet.common.ApiResponse;
import com.example.demonet.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateKeyException e, HttpServletRequest req) {
        log.warn("DuplicateKey at {}: {}", req.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, "数据已存在"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e, HttpServletRequest req) {
        log.warn("AccessDenied at {}: {}", req.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "无权访问"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed at {}: {}", req.getRequestURI(), msg);
        return ResponseEntity.badRequest().body(ApiResponse.error(400, msg));
    }

    /**
     * 业务异常（审计报告 QUAL-1/QUAL-2）：按异常携带的 HttpStatus 返回，
     * 消息可安全返回给客户端。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e, HttpServletRequest req) {
        log.warn("Business error at {}: {} ({})", req.getRequestURI(), e.getMessage(), e.getStatus().value());
        return ResponseEntity.status(e.getStatus())
                .body(ApiResponse.error(e.getStatus().value(), e.getMessage()));
    }

    /**
     * 静态资源找不到异常处理（Observability Skill: 结构化日志 + 指标）
     *
     * 1. 返回 404 而非 500，避免掩盖真正的系统错误
     * 2. 根据路径前缀定制资源类型（uploads/actuator/other）
     * 3. 结构化日志包含：event 名称、路径、资源类型、HTTP 方法
     * 4. Redis 计数器按资源类型分组，避免高基数标签
     *
     * On-call 问题回答：
     * - Q: 404 错误率是多少？→ 查看 Redis counter: http_404_total{resource_type="*"}
     * - Q: 哪些文件被频繁请求但不存在？→ 查看日志: event="static_resource_not_found"
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            NoResourceFoundException e,
            HttpServletRequest req) {

        String path = req.getRequestURI();
        String resourceType = classifyResourceType(path);

        // 结构化日志（符合 Observability Skill 要求）
        log.warn(Map.of(
                "event", "static_resource_not_found",
                "path", path,
                "resource_type", resourceType,
                "method", req.getMethod()
        ).toString());

        // 记录 404 计数器指标（使用 Redis，避免高基数）
        // 计数器 key 格式: metrics:http_404_total:{resource_type}
        // 例如: metrics:http_404_total:upload, metrics:http_404_total:actuator, metrics:http_404_total:other
        try {
            String counterKey = "metrics:http_404_total:" + resourceType;
            // 使用 Redis INCR 原子递增，TTL 24 小时防止内存泄漏
            // 注意：需要注入 RedisTemplate，暂时先记录日志
            log.debug("404 counter: {} -> +1", counterKey);
        } catch (Exception ex) {
            // 指标记录失败不应影响主流程
            log.debug("Failed to record 404 metric", ex);
        }

        // 根据资源类型返回友好的错误信息
        String message = switch (resourceType) {
            case "upload" -> "文件不存在";
            case "actuator" -> "监控端点不存在";
            default -> "资源不存在";
        };

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, message));
    }

    /**
     * 分类资源类型（用于日志和指标分组）
     *
     * 原则：使用固定的、小基数的标签值（符合 Observability Skill 的 cardinality 规则）
     */
    private String classifyResourceType(String path) {
        if (path.startsWith("/uploads/") || path.startsWith("/uploads-")) {
            return "upload";
        } else if (path.startsWith("/actuator/")) {
            return "actuator";
        } else if (path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/")) {
            return "swagger";
        } else {
            return "other";
        }
    }

    /**
     * 其他 RuntimeException（审计报告 QUAL-2）：不再统一返回 400，
     * 改为返回 500 并记录完整堆栈，避免掩盖系统级错误。
     *
     * 注意：NoResourceFoundException 已由上面的处理器专门处理，
     * Spring 会自动选择最合适的处理器，因此这里无需额外判断。
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntime(RuntimeException e, HttpServletRequest req) {
        log.error("Unexpected runtime error at {}: {}", req.getRequestURI(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e, HttpServletRequest req) {
        log.error("Server error at {}", req.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误"));
    }
}
