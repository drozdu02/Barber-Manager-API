package com.barber_manager.appointment_service.dto.admin;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record WorkScheduleResponse(
        Long id,
        Long barberId,
        DayOfWeek dayOfWeek,
        LocalTime openTime,
        LocalTime closeTime
) {
}
