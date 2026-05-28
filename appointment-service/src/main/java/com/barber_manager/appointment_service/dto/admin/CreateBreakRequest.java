package com.barber_manager.appointment_service.dto.admin;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateBreakRequest(
        @NotNull Long barberId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
) {
}

