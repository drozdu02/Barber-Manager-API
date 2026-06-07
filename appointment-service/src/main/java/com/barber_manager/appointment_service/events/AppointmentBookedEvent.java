package com.barber_manager.appointment_service.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentBookedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        Long appointmentId,
        String email,
        String firstName,
        String bookingToken,
        LocalDateTime startTime
) implements DomainEvent {
}
