package com.barber_manager.appointment_service.dto.admin;

import jakarta.validation.constraints.NotNull;

public record AssignBarberCompetencyRequest(
        @NotNull Long barberId,
        @NotNull Long serviceId
) {
}
