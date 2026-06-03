package com.barber_manager.auth_service.dto.response;

public record StaffAccountResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role
) {
}
