package com.barber_manager.appointment_service.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentCanceledEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        Long appointmentId,
        Long barberId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String phoneNumber,
        String bookingToken,
        CancellationSource source
) implements DomainEvent {
}
