package com.barber_manager.appointment_service.dto.admin;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record UpdateWorkScheduleRequest(
        @NotNull LocalTime openTime,
        @NotNull LocalTime closeTime
) {
}
