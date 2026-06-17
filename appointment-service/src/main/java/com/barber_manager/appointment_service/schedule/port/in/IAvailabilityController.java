package com.barber_manager.appointment_service.schedule.port.in;

import com.barber_manager.appointment_service.dto.AvailabilityResponse;

import java.time.LocalDate;
import java.util.List;

public interface IAvailabilityController {

    AvailabilityResponse getAvailability(
            LocalDate date,
            Long serviceId,
            Long barberId,
            boolean anyAvailable,
            List<Long> anyAvailableBarberIds
    );
}
