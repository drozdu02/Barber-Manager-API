package com.barber_manager.user_service.dto.request;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        String phoneNumber
) {
}
