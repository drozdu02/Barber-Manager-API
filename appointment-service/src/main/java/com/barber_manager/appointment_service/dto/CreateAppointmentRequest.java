package com.barber_manager.appointment_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record CreateAppointmentRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank
        @Size(min = 9, max = 9)
        @Pattern(regexp = "^[0-9]*$", message = "Phone number must contain only digits.")
        String phoneNumber,
        @NotBlank @Email String email,

        @NotNull Long serviceId,

        LocalDateTime startTime,

        Long barberId,

        List<Long> anyBarberIds,

        /**
         * When true with anyBarberIds, the system picks the earliest available slot
         * among competent barbers. startTime is optional and acts as the search anchor.
         */
        Boolean assignEarliestSlot
) {
}

