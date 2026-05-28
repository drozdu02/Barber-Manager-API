package com.barber_manager.appointment_service.dto;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long serviceId,
        Long barberId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reservationCode,
        boolean canceled,
        String status
) {
}

