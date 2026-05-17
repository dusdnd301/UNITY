package com.example.test.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String frontendBaseUrl,
        List<String> corsAllowedOrigins,
        String qrOutputDir,
        String menuImageOutputDir,
        boolean adminCookieSecure,
        Jwt jwt,
        Toss toss
) {
    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Toss(String secretKey, String clientKey, String confirmUrl) {
    }
}
