package com.example.test.config;

import java.util.Arrays;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProductionStartupValidator implements ApplicationRunner {
    private final AppProperties properties;
    private final Environment environment;

    public ProductionStartupValidator(AppProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean prod = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (!prod) {
            return;
        }
        String baseUrl = properties.frontendBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("운영 환경에서는 APP_BASE_URL을 설정해야 합니다.");
        }
        if (baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1")) {
            throw new IllegalStateException("운영 환경 APP_BASE_URL은 localhost가 아니라 공개 도메인이어야 합니다.");
        }
        if (!baseUrl.startsWith("https://")) {
            throw new IllegalStateException("운영 환경 APP_BASE_URL은 HTTPS 주소여야 합니다. 예: https://order.example.com");
        }
    }
}
