package com.barber_manager.appointment_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AvailabilitySlotResponse(
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<Long> availableBarberIds
) {
}

