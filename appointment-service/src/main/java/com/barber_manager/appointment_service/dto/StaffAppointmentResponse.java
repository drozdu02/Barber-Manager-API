package com.barber_manager.appointment_service.dto;

import java.time.LocalDateTime;

public record StaffAppointmentResponse(
        Long id,
        Long serviceId,
        String serviceName,
        Long barberId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        String bookingToken,
        boolean canceled,
        String status
) {
}
