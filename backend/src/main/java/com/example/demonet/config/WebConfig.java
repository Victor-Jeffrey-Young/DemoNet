package com.example.demonet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // 解析为绝对路径，避免开发环境相对路径解析到 target/classes/
            String absolutePath = new java.io.File(uploadDir).getAbsolutePath();
            // 确保路径以 / 结尾
            if (!absolutePath.endsWith("/")) {
                absolutePath += "/";
            }
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:" + absolutePath)
                    .setCachePeriod(86400);
        } catch (Exception e) {
            // 降级处理：使用项目根目录下的 uploads
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:./uploads/")
                    .setCachePeriod(86400);
        }
    }
}
