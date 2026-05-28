package com.barber_manager.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank
    String firstName,

    @NotBlank
    String lastName,

    @NotBlank
    String email,

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters.")
    String password,

    @NotBlank(message = "Phone number is required.")
    @Size(min = 9, max = 9, message = "Phone number must be 9 digits.")
    @Pattern(regexp = "^[0-9]*$", message = "Phone number must contain only digits.")
    String phoneNumber

) {
}
