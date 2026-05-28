package com.barber_manager.auth_service.dto.response;

import com.barber_manager.auth_service.enums.Role;

public record UserCredentialDto(
    Long id,
    String email,
    String password,
    Role role
) {
}
