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
        String absolutePath = new java.io.File(uploadDir).getAbsolutePath() + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath)
                .setCachePeriod(86400);
    }
}
