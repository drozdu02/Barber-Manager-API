package com.barber_manager.auth_service.dto.response;

public record TokenResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
    public static TokenResponseDto of(String accessToken, String refreshToken) {
        return new TokenResponseDto(accessToken, refreshToken, "Bearer", 15 * 60L);
    }

    public static TokenResponseDto accessOnly(String accessToken) {
        return new TokenResponseDto(accessToken, null, "Bearer", 15 * 60L);
    }
}