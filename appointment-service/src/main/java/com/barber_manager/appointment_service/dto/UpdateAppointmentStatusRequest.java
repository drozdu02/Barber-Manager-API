package com.barber_manager.appointment_service.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAppointmentStatusRequest(
        @NotBlank String status
) {
}

