package com.barber_manager.appointment_service.dto.admin;

import java.time.LocalDateTime;

public record NoShowIncidentResponse(
        Long id,
        Long appointmentId,
        LocalDateTime appointmentStartTime,
        LocalDateTime registeredAt
) {
}
