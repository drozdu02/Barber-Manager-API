package com.barber_manager.auth_service;

import com.barber_manager.auth_service.config.RefreshCookieProperties;
import com.barber_manager.auth_service.web.RefreshTokenCookieService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenCookieServiceTest {

    private RefreshTokenCookieService cookieService;

    @BeforeEach
    void setUp() {
        RefreshCookieProperties properties = new RefreshCookieProperties(
                "refreshToken",
                "/auth",
                false,
                "None"
        );
        cookieService = new RefreshTokenCookieService(properties);
    }

    @Test
    void shouldExtractRefreshTokenFromCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "abc123"));

        Optional<String> token = cookieService.extractRefreshToken(request);

        assertTrue(token.isPresent());
        assertEquals("abc123", token.get());
    }

    @Test
    void shouldReturnEmptyWhenCookieMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertTrue(cookieService.extractRefreshToken(request).isEmpty());
    }

    @Test
    void shouldIgnoreBlankCookieValue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "   "));

        assertTrue(cookieService.extractRefreshToken(request).isEmpty());
    }

    @Test
    void shouldSetRefreshTokenCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieService.setRefreshToken(response, "token-value", Duration.ofDays(7));

        String header = response.getHeader("Set-Cookie");
        assertNotNull(header);
        assertTrue(header.contains("refreshToken=token-value"));
        assertTrue(header.contains("Path=/auth"));
        assertTrue(header.contains("HttpOnly"));
        assertTrue(header.contains("SameSite=None"));
        assertFalse(header.contains("Secure"));
    }

    @Test
    void shouldClearRefreshTokenCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        cookieService.clearRefreshToken(response);

        String header = response.getHeader("Set-Cookie");
        assertNotNull(header);
        assertTrue(header.contains("refreshToken="));
        assertTrue(header.contains("Max-Age=0"));
    }
}
