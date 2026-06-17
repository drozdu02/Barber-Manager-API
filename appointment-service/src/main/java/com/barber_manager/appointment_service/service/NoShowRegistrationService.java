package com.barber_manager.appointment_service.service;

import com.barber_manager.appointment_service.booking.port.in.IClientPhoneProfileController;
import com.barber_manager.appointment_service.config.ClientBlockProperties;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneRequest;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneResponse;
import com.barber_manager.appointment_service.dto.admin.ClientPhoneProfileResponse;
import com.barber_manager.appointment_service.dto.admin.NoShowIncidentResponse;
import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.entity.ClientPhoneProfile;
import com.barber_manager.appointment_service.entity.NoShowIncident;
import com.barber_manager.appointment_service.events.DomainEvent;
import com.barber_manager.appointment_service.events.DomainEventPublisher;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.ClientPhoneProfileRepository;
import com.barber_manager.appointment_service.repository.NoShowIncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoShowRegistrationService implements IClientPhoneProfileController {

    private final ClientPhoneProfileRepository clientPhoneProfileRepository;
    private final NoShowIncidentRepository noShowIncidentRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClientBlockProperties clientBlockProperties;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    @Transactional
    public void registerNoShow(Appointment appointment) {
        if (noShowIncidentRepository.existsByAppointmentId(appointment.getId())) {
            return;
        }

        String phoneNumber = appointment.getPhoneNumber();
        ClientPhoneProfile profile = findOrCreate(phoneNumber);

        profile.registerNoShow();
        clientPhoneProfileRepository.save(profile);

        NoShowIncident incident = new NoShowIncident();
        incident.setPhoneNumber(phoneNumber);
        incident.setAppointmentId(appointment.getId());
        incident.setAppointmentStartTime(appointment.getStartTime());
        noShowIncidentRepository.save(incident);

        if (profile.shouldAutoBlock(clientBlockProperties.getNoShowThreshold())) {
            List<Long> futureAppointmentIds = findFutureAppointmentIds(phoneNumber);
            String reason = "Automatic block: no-show threshold exceeded ("
                    + profile.getNoShowCount() + " incidents, limit "
                    + clientBlockProperties.getNoShowThreshold() + ").";
            profile.block(
                    domainEventPublisher.newEventId(),
                    domainEventPublisher.now(),
                    reason,
                    true,
                    futureAppointmentIds
            );
            clientPhoneProfileRepository.save(profile);
        }

        publishDomainEvents(profile);
    }

    @Override
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
                clientBlockProperties.getNoShowThreshold(),
                profile.isBlocked(),
                profile.getCreatedAt(),
                profile.getUpdatedAt(),
                incidents
        );
    }

    @Override
    @Transactional
    public BlockPhoneResponse blockPhone(BlockPhoneRequest request) {
        ClientPhoneProfile profile = clientPhoneProfileRepository.save(findOrCreate(request.phoneNumber()));
        List<Long> futureAppointmentIds = findFutureAppointmentIds(request.phoneNumber());

        profile.block(
                domainEventPublisher.newEventId(),
                domainEventPublisher.now(),
                request.reason(),
                false,
                futureAppointmentIds
        );
        ClientPhoneProfile saved = clientPhoneProfileRepository.save(profile);
        publishDomainEvents(saved);

        return new BlockPhoneResponse(
                saved.getId(),
                saved.getPhoneNumber(),
                saved.getBlockReason(),
                saved.isBlocked(),
                futureAppointmentIds.size(),
                false
        );
    }

    @Override
    @Transactional
    public void unblockPhone(String phoneNumber) {
        ClientPhoneProfile profile = clientPhoneProfileRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("Phone profile not found."));
        profile.unblock();
        clientPhoneProfileRepository.save(profile);
    }

    private ClientPhoneProfile findOrCreate(String phoneNumber) {
        return clientPhoneProfileRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    ClientPhoneProfile created = new ClientPhoneProfile();
                    created.setPhoneNumber(phoneNumber);
                    created.setNoShowCount(0);
                    created.setBlocked(false);
                    return created;
                });
    }

    private List<Long> findFutureAppointmentIds(String phoneNumber) {
        return appointmentRepository
                .findAllByPhoneNumberAndCanceledFalseAndStartTimeAfter(phoneNumber, LocalDateTime.now())
                .stream()
                .map(Appointment::getId)
                .toList();
    }

    private void publishDomainEvents(ClientPhoneProfile profile) {
        for (DomainEvent event : profile.pullDomainEvents()) {
            domainEventPublisher.publish(event);
        }
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
