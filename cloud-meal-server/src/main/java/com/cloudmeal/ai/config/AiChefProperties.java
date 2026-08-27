package com.cloudmeal.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cloud-meal.ai-chef")
public class AiChefProperties {
    private String baseUrl = "http://127.0.0.1:8081";
    private String serviceToken = "";
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 70000;
}
