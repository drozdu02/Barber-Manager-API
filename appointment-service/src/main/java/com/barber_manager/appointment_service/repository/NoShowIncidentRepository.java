package com.barber_manager.appointment_service.repository;

import com.barber_manager.appointment_service.entity.NoShowIncident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoShowIncidentRepository extends JpaRepository<NoShowIncident, Long> {

    boolean existsByAppointmentId(Long appointmentId);

    List<NoShowIncident> findAllByPhoneNumberOrderByRegisteredAtDesc(String phoneNumber);
}
