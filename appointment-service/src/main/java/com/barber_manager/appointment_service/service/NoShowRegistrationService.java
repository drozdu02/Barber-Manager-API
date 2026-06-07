package com.barber_manager.appointment_service.service;

import com.barber_manager.appointment_service.dto.admin.ClientPhoneProfileResponse;
import com.barber_manager.appointment_service.dto.admin.NoShowIncidentResponse;
import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.entity.ClientPhoneProfile;
import com.barber_manager.appointment_service.entity.NoShowIncident;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.repository.ClientPhoneProfileRepository;
import com.barber_manager.appointment_service.repository.NoShowIncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoShowRegistrationService {

    private final ClientPhoneProfileRepository clientPhoneProfileRepository;
    private final NoShowIncidentRepository noShowIncidentRepository;
    private final ClientBlockService clientBlockService;

    @Transactional
    public void registerNoShow(Appointment appointment) {
        if (noShowIncidentRepository.existsByAppointmentId(appointment.getId())) {
            return;
        }

        String phoneNumber = appointment.getPhoneNumber();

        ClientPhoneProfile profile = clientPhoneProfileRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    ClientPhoneProfile created = new ClientPhoneProfile();
                    created.setPhoneNumber(phoneNumber);
                    created.setNoShowCount(0);
                    return created;
                });

        profile.setNoShowCount(profile.getNoShowCount() + 1);
        clientPhoneProfileRepository.save(profile);

        NoShowIncident incident = new NoShowIncident();
        incident.setPhoneNumber(phoneNumber);
        incident.setAppointmentId(appointment.getId());
        incident.setAppointmentStartTime(appointment.getStartTime());
        noShowIncidentRepository.save(incident);

        clientBlockService.applyBlockIfNoShowThresholdExceeded(phoneNumber, profile.getNoShowCount());
    }

    public ClientPhoneProfileResponse getPhoneProfile(String phoneNumber) {
        ClientPhoneProfile profile = clientPhoneProfileRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("Phone profile not found."));

        List<NoShowIncidentResponse> incidents = noShowIncidentRepository
                .findAllByPhoneNumberOrderByRegisteredAtDesc(phoneNumber).stream()
                .map(this::toIncidentResponse)
                .toList();

        return new ClientPhoneProfileResponse(
                profile.getPhoneNumber(),
                profile.getNoShowCount(),
                clientBlockService.getNoShowThreshold(),
                clientBlockService.isBlocked(profile.getPhoneNumber()),
                profile.getCreatedAt(),
                profile.getUpdatedAt(),
                incidents
        );
    }

    private NoShowIncidentResponse toIncidentResponse(NoShowIncident incident) {
        return new NoShowIncidentResponse(
                incident.getId(),
                incident.getAppointmentId(),
                incident.getAppointmentStartTime(),
                incident.getRegisteredAt()
        );
    }
}
