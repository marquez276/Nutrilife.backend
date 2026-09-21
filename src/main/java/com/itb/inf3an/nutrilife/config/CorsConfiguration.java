package com.itb.inf3an.nutrilife.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    @Value("${app.cors-enabled:true}")
    private boolean corsEnabled;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (corsEnabled) {
            registry.addMapping("/**")
                    .allowedOrigins("http://localhost:8686", "http://localhost:5173", "http://localhost:5174", "http://localhost:3000", "http://localhost:8080")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "TRACE", "CONNECT")
                    .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*", "http://[::1]:*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                    .allowedHeaders("*")
                    .allowCredentials(true);
        }
    }
}

