package com.barber_manager.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    @NotBlank
    String firstName,

    @NotBlank
    String lastName,

    @NotBlank
    String email
) {
}
