package com.barber_manager.appointment_service.dto.admin;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateTimeOffRequest(
        @NotNull Long barberId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String reason
) {
}
