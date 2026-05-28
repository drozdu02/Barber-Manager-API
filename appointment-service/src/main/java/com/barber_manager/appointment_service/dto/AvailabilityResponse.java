package com.barber_manager.appointment_service.dto;

import java.time.LocalDate;
import java.util.List;

public record AvailabilityResponse(
        LocalDate date,
        Long serviceId,
        Long barberId,
        boolean any,
        List<AvailabilitySlotResponse> slots
) {
}

