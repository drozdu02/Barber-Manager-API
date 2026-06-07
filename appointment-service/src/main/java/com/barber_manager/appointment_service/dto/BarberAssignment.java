package com.barber_manager.appointment_service.dto;

import java.time.LocalDateTime;

public record BarberAssignment(
        Long barberId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
