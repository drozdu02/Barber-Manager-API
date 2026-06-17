package com.barber_manager.appointment_service.infrastructure.adapter.out;

import com.barber_manager.appointment_service.booking.port.out.INoShowIncidentRepository;
import com.barber_manager.appointment_service.entity.NoShowIncident;
import com.barber_manager.appointment_service.repository.NoShowIncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NoShowIncidentRepositoryAdapter implements INoShowIncidentRepository {

    private final NoShowIncidentRepository noShowIncidentRepository;

    @Override
    public boolean existsByAppointmentId(Long appointmentId) {
        return noShowIncidentRepository.existsByAppointmentId(appointmentId);
    }

    @Override
    public NoShowIncident save(NoShowIncident incident) {
        return noShowIncidentRepository.save(incident);
    }

    @Override
    public List<NoShowIncident> findAllByPhoneNumberOrderByRegisteredAtDesc(String phoneNumber) {
        return noShowIncidentRepository.findAllByPhoneNumberOrderByRegisteredAtDesc(phoneNumber);
    }
}
