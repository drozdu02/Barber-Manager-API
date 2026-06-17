package com.barber_manager.appointment_service.booking.port.out;

import com.barber_manager.appointment_service.entity.NoShowIncident;

import java.util.List;

public interface INoShowIncidentRepository {

    boolean existsByAppointmentId(Long appointmentId);

    NoShowIncident save(NoShowIncident incident);

    List<NoShowIncident> findAllByPhoneNumberOrderByRegisteredAtDesc(String phoneNumber);
}
