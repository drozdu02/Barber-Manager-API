package com.barber_manager.appointment_service.service;

import com.barber_manager.appointment_service.booking.port.in.IClientBlockController;
import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.BlockedPhoneNumberRepository;
import com.barber_manager.appointment_service.config.ClientBlockProperties;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneRequest;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneResponse;
import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.entity.BlockedPhoneNumber;
import com.barber_manager.appointment_service.events.ClientBlockedEvent;
import com.barber_manager.appointment_service.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientBlockService implements IClientBlockController {

    private final BlockedPhoneNumberRepository blockedPhoneNumberRepository;
    private final AppointmentRepository appointmentRepository;
    private final ClientBlockProperties clientBlockProperties;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    @Transactional
    public BlockPhoneResponse blockPhone(BlockPhoneRequest request) {
        return blockPhone(request.phoneNumber(), request.reason(), false);
    }

    @Transactional
    public Optional<BlockPhoneResponse> applyBlockIfNoShowThresholdExceeded(String phoneNumber, int noShowCount) {
        if (noShowCount < clientBlockProperties.getNoShowThreshold()) {
            return Optional.empty();
        }
        if (blockedPhoneNumberRepository.findByPhoneNumberAndActiveTrue(phoneNumber).isPresent()) {
            return Optional.empty();
        }

        String reason = "Automatic block: no-show threshold exceeded ("
                + noShowCount + " incidents, limit " + clientBlockProperties.getNoShowThreshold() + ").";
        return Optional.of(blockPhone(phoneNumber, reason, true));
    }

    @Override
    @Transactional
    public void unblockPhone(String phoneNumber) {
        blockedPhoneNumberRepository.findByPhoneNumberAndActiveTrue(phoneNumber).ifPresent(block -> {
            block.setActive(false);
            blockedPhoneNumberRepository.save(block);
        });
    }

    public boolean isBlocked(String phoneNumber) {
        return blockedPhoneNumberRepository.findByPhoneNumberAndActiveTrue(phoneNumber).isPresent();
    }

    public int getNoShowThreshold() {
        return clientBlockProperties.getNoShowThreshold();
    }

    private BlockPhoneResponse blockPhone(String phoneNumber, String reason, boolean automatic) {
        BlockedPhoneNumber block = blockedPhoneNumberRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(BlockedPhoneNumber::new);
        block.setPhoneNumber(phoneNumber);
        block.setReason(reason);
        block.setActive(true);

        BlockedPhoneNumber saved = blockedPhoneNumberRepository.save(block);
        List<Long> futureAppointmentIds = findFutureAppointmentIds(phoneNumber);

        domainEventPublisher.publish(new ClientBlockedEvent(
                domainEventPublisher.newEventId(),
                domainEventPublisher.now(),
                saved.getId(),
                phoneNumber,
                reason,
                automatic,
                futureAppointmentIds
        ));

        return new BlockPhoneResponse(
                saved.getId(),
                saved.getPhoneNumber(),
                saved.getReason(),
                saved.isActive(),
                futureAppointmentIds.size(),
                automatic
        );
    }

    private List<Long> findFutureAppointmentIds(String phoneNumber) {
        return appointmentRepository
                .findAllByPhoneNumberAndCanceledFalseAndStartTimeAfter(phoneNumber, LocalDateTime.now())
                .stream()
                .map(Appointment::getId)
                .toList();
    }
}
