package com.barber_manager.user_service.dto.response;


import com.barber_manager.user_service.enums.Role;

public record UserCredentialDto(
    Long id,
    String email,
    String password,
    Role role
) {
}
