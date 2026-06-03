package com.barber_manager.appointment_service.service;

import com.barber_manager.appointment_service.dto.admin.BlockPhoneRequest;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneResponse;
import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.entity.BlockedPhoneNumber;
import com.barber_manager.appointment_service.enums.AppointmentStatus;
import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.BlockedPhoneNumberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientBlockService {

    private final BlockedPhoneNumberRepository blockedPhoneNumberRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public BlockPhoneResponse blockPhone(BlockPhoneRequest request) {
        BlockedPhoneNumber block = blockedPhoneNumberRepository.findByPhoneNumber(request.phoneNumber())
                .orElseGet(BlockedPhoneNumber::new);
        block.setPhoneNumber(request.phoneNumber());
        block.setReason(request.reason());
        block.setActive(true);

        BlockedPhoneNumber saved = blockedPhoneNumberRepository.save(block);
        int canceledCount = cancelFutureAppointments(request.phoneNumber());

        return new BlockPhoneResponse(
                saved.getId(),
                saved.getPhoneNumber(),
                saved.getReason(),
                saved.isActive(),
                canceledCount
        );
    }

    @Transactional
    public void unblockPhone(String phoneNumber) {
        blockedPhoneNumberRepository.findByPhoneNumberAndActiveTrue(phoneNumber).ifPresent(block -> {
            block.setActive(false);
            blockedPhoneNumberRepository.save(block);
        });
    }

    private int cancelFutureAppointments(String phoneNumber) {
        List<Appointment> futureAppointments = appointmentRepository
                .findAllByPhoneNumberAndCanceledFalseAndStartTimeAfter(phoneNumber, LocalDateTime.now());

        for (Appointment appointment : futureAppointments) {
            appointment.setCanceled(true);
            appointment.setStatus(AppointmentStatus.CANCELED);
        }
        appointmentRepository.saveAll(futureAppointments);
        return futureAppointments.size();
    }
}
