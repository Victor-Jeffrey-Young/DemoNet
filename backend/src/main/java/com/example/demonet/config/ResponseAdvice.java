package com.example.demonet.config;

import com.example.demonet.common.ApiResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.example.demonet.controller")
@RequiredArgsConstructor
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Skip if already ApiResponse
        return !returnType.getParameterType().equals(ApiResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // If the return type is String, we must serialize it manually because StringHttpMessageConverter
        // is used by Spring for String return types and it will throw ClassCastException if we return an Object.
        if (body instanceof String) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            try {
                return objectMapper.writeValueAsString(ApiResponse.success(body));
            } catch (JacksonException e) {
                // 显式捕获而非 @SneakyThrows（审计报告 QUAL-5）
                log.error("Failed to serialize String response body", e);
                return "{\"code\":500,\"message\":\"响应序列化失败\"}";
            }
        }
        
        // If it's already an ApiResponse (e.g. from GlobalExceptionHandler), don't wrap again
        if (body instanceof ApiResponse) {
            return body;
        }
        
        // Wrap everything else
        return ApiResponse.success(body);
    }
}
