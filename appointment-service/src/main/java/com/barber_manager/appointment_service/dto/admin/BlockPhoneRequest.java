package com.barber_manager.appointment_service.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BlockPhoneRequest(
        @NotBlank
        @Size(min = 9, max = 9)
        @Pattern(regexp = "^[0-9]*$", message = "Phone number must contain only digits.")
        String phoneNumber,
        String reason
) {
}

