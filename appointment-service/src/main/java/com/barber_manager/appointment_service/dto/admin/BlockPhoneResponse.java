package com.barber_manager.appointment_service.dto.admin;

public record BlockPhoneResponse(
        Long id,
        String phoneNumber,
        String reason,
        boolean active,
        int canceledAppointments,
        boolean automatic
) {
}
