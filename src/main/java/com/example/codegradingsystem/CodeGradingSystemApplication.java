package com.example.codegradingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 编程作业智能批改系统 - 主应用类
 * 
 * 功能特性:
 * - Spring Boot 3.2 + Java 17
 * - Docker安全沙箱执行环境
 * - 多语言支持 (Java/Python/C++)
 * - AI智能分析和反馈
 * - 前后端分离架构
 */
@SpringBootApplication
public class CodeGradingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeGradingSystemApplication.class, args);
    }
    
    /**
     * 配置CORS跨域支持，允许前端开发服务器访问
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}