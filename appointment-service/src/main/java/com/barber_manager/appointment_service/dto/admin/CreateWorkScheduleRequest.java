package com.barber_manager.appointment_service.dto.admin;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record CreateWorkScheduleRequest(
        @NotNull Long barberId,
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime openTime,
        @NotNull LocalTime closeTime
) {
}
