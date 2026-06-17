package com.barber_manager.appointment_service.events;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ClientBlockedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        Long profileId,
        String phoneNumber,
        String reason,
        boolean automatic,
        List<Long> futureAppointmentIds
) implements DomainEvent {
}
