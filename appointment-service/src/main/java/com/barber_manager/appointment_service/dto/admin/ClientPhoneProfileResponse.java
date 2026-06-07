package com.barber_manager.appointment_service.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

public record ClientPhoneProfileResponse(
        String phoneNumber,
        int noShowCount,
        int noShowThreshold,
        boolean blocked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<NoShowIncidentResponse> incidents
) {
}
