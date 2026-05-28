package com.barber_manager.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.refresh-cookie")
public record RefreshCookieProperties(
        String name,
        String path,
        boolean secure,
        String sameSite
) {
}

