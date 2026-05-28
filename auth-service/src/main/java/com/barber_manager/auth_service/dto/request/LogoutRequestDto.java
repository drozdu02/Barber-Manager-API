package com.barber_manager.auth_service.dto.request;

public record LogoutRequestDto(
        String refreshToken
) {}