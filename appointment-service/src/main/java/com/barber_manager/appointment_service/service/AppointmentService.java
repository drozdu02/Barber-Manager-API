package com.barber_manager.appointment_service.service;

import com.barber_manager.appointment_service.dto.AppointmentResponse;
import com.barber_manager.appointment_service.dto.CreateAppointmentRequest;
import com.barber_manager.appointment_service.dto.StaffAppointmentResponse;
import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.entity.ServiceOffering;
import com.barber_manager.appointment_service.enums.AppointmentStatus;
import com.barber_manager.appointment_service.exception.BusinessRuleException;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.BlockedPhoneNumberRepository;
import com.barber_manager.appointment_service.repository.ServiceOfferingRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final int SLOT_MINUTES = 30;
    private static final Duration CANCEL_DEADLINE = Duration.ofHours(12);
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 10;

    private final AppointmentRepository appointmentRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final BlockedPhoneNumberRepository blockedPhoneNumberRepository;
    private final MailService mailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) throws MessagingException {
        blockedPhoneNumberRepository.findByPhoneNumberAndActiveTrue(request.phoneNumber())
                .ifPresent(b -> {
                    throw new BusinessRuleException("Phone number is blocked.");
                });

        ServiceOffering service = serviceOfferingRepository.findById(request.serviceId())
                .orElseThrow(() -> new NotFoundException("Service offering not found."));

        LocalDateTime start = request.startTime();
        LocalDateTime end = start.plusMinutes((long) service.getSlotCount() * SLOT_MINUTES);

        Long barberId = resolveBarberId(request, end);
        if (barberId != null) {
            ensureNoOverlap(barberId, start, end);
        }

        Appointment appointment = new Appointment();
        appointment.setServiceOffering(service);
        appointment.setBarberId(barberId);
        appointment.setStartTime(start);
        appointment.setEndTime(end);
        appointment.setFirstName(request.firstName());
        appointment.setLastName(request.lastName());
        appointment.setPhoneNumber(request.phoneNumber());
        appointment.setEmail(request.email());
        String code = generateUniqueReservationCode();
        appointment.setReservationCode(code);
        appointment.setCanceled(false);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment saved = appointmentRepository.save(appointment);
        mailService.sendConfirmationEmail(
                request.email(),
                request.firstName(),
                code,
                request.startTime().toString()
        );
        return toResponse(saved);
    }

    @Transactional
    public void cancelByReservationCode(String reservationCode) {
        Appointment appointment = appointmentRepository.findByReservationCode(reservationCode)
                .orElseThrow(() -> new NotFoundException("Reservation code not found."));

        if (appointment.isCanceled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (appointment.getStartTime().isBefore(now.plus(CANCEL_DEADLINE))) {
            throw new BusinessRuleException("Appointment can be canceled up to 12h before start time.");
        }

        appointment.setCanceled(true);
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public StaffAppointmentResponse updateStatus(Long appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found."));

        appointment.setStatus(status);
        appointment.setCanceled(status == AppointmentStatus.CANCELED);
        Appointment saved = appointmentRepository.save(appointment);
        return toStaffResponse(saved);
    }

    public StaffAppointmentResponse getStaffDetails(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found."));
        return toStaffResponse(appointment);
    }

    public List<StaffAppointmentResponse> getBarberCalendar(Long barberId, LocalDateTime from, LocalDateTime to) {
        return appointmentRepository.findAllByBarberIdAndStartTimeBetweenAndCanceledFalse(barberId, from, to).stream()
                .map(this::toStaffResponse)
                .toList();
    }


    private void ensureNoOverlap(Long barberId, LocalDateTime start, LocalDateTime end) {
        if (!appointmentRepository.findOverlapping(barberId, start, end).isEmpty()) {
            throw new BusinessRuleException("Selected time slot is not available.");
        }
    }

    private Long resolveBarberId(CreateAppointmentRequest request, LocalDateTime end) {
        if (request.barberId() != null) return request.barberId();

        List<Long> candidates = request.anyBarberIds();
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        LocalDateTime start = request.startTime();
        for (Long candidate : candidates) {
            if (appointmentRepository.findOverlapping(candidate, start, end).isEmpty()) {
                return candidate;
            }
        }
        throw new BusinessRuleException("No available barber for selected time slot.");
    }

    private String generateUniqueReservationCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String code = randomCode();
            if (appointmentRepository.findByReservationCode(code).isEmpty()) {
                return code;
            }
        }
        throw new IllegalStateException("Unable to generate unique reservation code.");
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int idx = secureRandom.nextInt(CODE_ALPHABET.length());
            sb.append(CODE_ALPHABET.charAt(idx));
        }
        return sb.toString();
    }

    private AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getServiceOffering().getId(),
                a.getBarberId(),
                a.getStartTime(),
                a.getEndTime(),
                a.getReservationCode(),
                a.isCanceled(),
                a.getStatus() != null ? a.getStatus().name() : null
        );
    }

    private StaffAppointmentResponse toStaffResponse(Appointment a) {
        return new StaffAppointmentResponse(
                a.getId(),
                a.getServiceOffering().getId(),
                a.getServiceOffering().getName(),
                a.getBarberId(),
                a.getStartTime(),
                a.getEndTime(),
                a.getFirstName(),
                a.getLastName(),
                a.getPhoneNumber(),
                a.getEmail(),
                a.getReservationCode(),
                a.isCanceled(),
                a.getStatus() != null ? a.getStatus().name() : null
        );
    }

}

