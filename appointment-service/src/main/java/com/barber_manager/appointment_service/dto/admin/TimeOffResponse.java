package com.barber_manager.appointment_service.dto.admin;

import java.time.LocalDate;

public record TimeOffResponse(
        Long id,
        Long barberId,
        LocalDate startDate,
        LocalDate endDate,
        String reason
) {
}
