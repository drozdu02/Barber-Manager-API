package com.barber_manager.appointment_service.events;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {

    UUID eventId();

    LocalDateTime occurredAt();
}
