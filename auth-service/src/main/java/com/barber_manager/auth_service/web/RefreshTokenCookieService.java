package com.barber_manager.auth_service.web;

import com.barber_manager.auth_service.config.RefreshCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenCookieService {

    private final RefreshCookieProperties props;

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        return Arrays.stream(cookies)
                .filter(c -> props.name().equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst();
    }

    public void setRefreshToken(HttpServletResponse response, String refreshToken, Duration maxAge) {
        response.addHeader("Set-Cookie", buildCookie(refreshToken, maxAge));
    }

    public void clearRefreshToken(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildCookie("", Duration.ZERO));
    }

    private String buildCookie(String value, Duration maxAge) {
        StringBuilder sb = new StringBuilder();
        sb.append(props.name()).append("=").append(value).append("; Path=").append(props.path()).append("; Max-Age=").append(maxAge.toSeconds());
        sb.append("; HttpOnly");
        sb.append("; SameSite=").append(props.sameSite());
        if (props.secure()) sb.append("; Secure");
        return sb.toString();
    }
}

