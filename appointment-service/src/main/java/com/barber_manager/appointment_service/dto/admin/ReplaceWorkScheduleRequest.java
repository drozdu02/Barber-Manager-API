package com.barber_manager.appointment_service.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record ReplaceWorkScheduleRequest(
        @NotNull Long barberId,
        @NotEmpty List<@Valid DayScheduleEntry> entries
) {
    public record DayScheduleEntry(
            @NotNull DayOfWeek dayOfWeek,
            @NotNull LocalTime openTime,
            @NotNull LocalTime closeTime
    ) {
    }
}
