package com.barber_manager.appointment_service.service;

import com.barber_manager.appointment_service.booking.port.in.IAppointmentController;
import com.barber_manager.appointment_service.booking.port.in.IClientPhoneProfileController;
import com.barber_manager.appointment_service.booking.port.out.IAppointmentRepository;
import com.barber_manager.appointment_service.booking.port.out.IBlockedPhoneNumberRepository;
import com.barber_manager.appointment_service.catalog.port.out.IServiceCatalogRepository;
import com.barber_manager.appointment_service.dto.AppointmentResponse;
import com.barber_manager.appointment_service.dto.BarberAssignment;
import com.barber_manager.appointment_service.dto.CreateAppointmentRequest;
import com.barber_manager.appointment_service.dto.StaffAppointmentResponse;
import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.entity.Service;
import com.barber_manager.appointment_service.enums.AppointmentStatus;
import com.barber_manager.appointment_service.events.AppointmentBookedEvent;
import com.barber_manager.appointment_service.events.AppointmentCanceledEvent;
import com.barber_manager.appointment_service.events.CancellationSource;
import com.barber_manager.appointment_service.events.DomainEventPublisher;
import com.barber_manager.appointment_service.exception.BusinessRuleException;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.schedule.domain.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AppointmentService implements IAppointmentController {

    private static final int SLOT_MINUTES = 30;
    private static final Duration CANCEL_DEADLINE = Duration.ofHours(12);
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 10;

    private final IAppointmentRepository appointmentRepository;
    private final IServiceCatalogRepository serviceCatalogRepository;
    private final AvailabilityService availabilityService;
    private final IBlockedPhoneNumberRepository blockedPhoneNumberRepository;
    private final IClientPhoneProfileController clientPhoneProfileController;
    private final DomainEventPublisher domainEventPublisher;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) {
        blockedPhoneNumberRepository.findByPhoneNumberAndActiveTrue(request.phoneNumber())
                .ifPresent(b -> {
                    throw new BusinessRuleException("Phone number is blocked.");
                });

        Service service = serviceCatalogRepository.findById(request.serviceId())
                .orElseThrow(() -> new NotFoundException("Service not found."));

        BarberAssignment assignment = resolveAssignment(request, service);
        Long barberId = assignment.barberId();
        LocalDateTime start = assignment.startTime();
        LocalDateTime end = assignment.endTime();

        if (barberId != null) {
            ensureNoOverlap(barberId, start, end);
        }

        Appointment appointment = new Appointment();
        appointment.setService(service);
        appointment.setBarberId(barberId);
        appointment.setStartTime(start);
        appointment.setEndTime(end);
        appointment.setFirstName(request.firstName());
        appointment.setLastName(request.lastName());
        appointment.setPhoneNumber(request.phoneNumber());
        appointment.setEmail(request.email());
        String token = generateUniqueBookingToken();
        appointment.setBookingToken(token);
        appointment.setCanceled(false);
        appointment.setStatus(AppointmentStatus.BOOKED);

        Appointment saved = appointmentRepository.save(appointment);
        publishBooked(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void cancelByBookingToken(String bookingToken) {
        Appointment appointment = appointmentRepository.findByBookingToken(bookingToken)
                .orElseThrow(() -> new NotFoundException("Booking token not found."));

        if (appointment.isCanceled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (appointment.getStartTime().isBefore(now.plus(CANCEL_DEADLINE))) {
            throw new BusinessRuleException("Appointment can be canceled up to 12h before start time.");
        }

        appointment.setCanceled(true);
        appointment.setStatus(AppointmentStatus.CANCELED);
        Appointment saved = appointmentRepository.save(appointment);
        publishCanceled(saved, CancellationSource.CLIENT_TOKEN);
    }

    @Override
    @Transactional
    public StaffAppointmentResponse updateStatus(Long appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found."));

        if (status == AppointmentStatus.NO_SHOW && appointment.getStatus() != AppointmentStatus.NO_SHOW) {
            clientPhoneProfileController.registerNoShow(appointment);
        }

        boolean wasCanceled = appointment.isCanceled();
        appointment.setStatus(status);
        appointment.setCanceled(status == AppointmentStatus.CANCELED);
        Appointment saved = appointmentRepository.save(appointment);

        if (status == AppointmentStatus.CANCELED && !wasCanceled) {
            publishCanceled(saved, CancellationSource.STAFF_STATUS);
        }

        return toStaffResponse(saved);
    }

    @Override
    public StaffAppointmentResponse getStaffDetails(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found."));
        return toStaffResponse(appointment);
    }

    @Override
    public List<StaffAppointmentResponse> getBarberCalendar(Long barberId, LocalDateTime from, LocalDateTime to) {
        return appointmentRepository.findAllByBarberIdAndStartTimeBetweenAndCanceledFalse(barberId, from, to).stream()
                .map(this::toStaffResponse)
                .toList();
    }

    private void publishBooked(Appointment appointment) {
        domainEventPublisher.publish(new AppointmentBookedEvent(
                domainEventPublisher.newEventId(),
                domainEventPublisher.now(),
                appointment.getId(),
                appointment.getEmail(),
                appointment.getFirstName(),
                appointment.getBookingToken(),
                appointment.getStartTime()
        ));
    }

    private void publishCanceled(Appointment appointment, CancellationSource source) {
        domainEventPublisher.publish(new AppointmentCanceledEvent(
                domainEventPublisher.newEventId(),
                domainEventPublisher.now(),
                appointment.getId(),
                appointment.getBarberId(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getPhoneNumber(),
                appointment.getBookingToken(),
                source
        ));
    }

    private void ensureNoOverlap(Long barberId, LocalDateTime start, LocalDateTime end) {
        if (!appointmentRepository.findOverlapping(barberId, start, end).isEmpty()) {
            throw new BusinessRuleException("Selected time slot is not available.");
        }
    }

    private BarberAssignment resolveAssignment(CreateAppointmentRequest request, Service service) {
        int durationMinutes = service.getSlotCount() * SLOT_MINUTES;

        if (request.barberId() != null) {
            LocalDateTime start = requireStartTime(request);
            availabilityService.filterCompetentBarbers(request.serviceId(), List.of(request.barberId()))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleException("Barber cannot perform this service."));
            return new BarberAssignment(
                    request.barberId(),
                    start,
                    start.plusMinutes(durationMinutes)
            );
        }

        List<Long> candidates = request.anyBarberIds();
        if (candidates == null || candidates.isEmpty()) {
            throw new BusinessRuleException("barberId or anyBarberIds is required.");
        }

        if (Boolean.TRUE.equals(request.assignEarliestSlot())) {
            return availabilityService.findEarliestAssignment(
                            request.serviceId(),
                            candidates,
                            request.startTime()
                    )
                    .orElseThrow(() -> new BusinessRuleException("No available barber found."));
        }

        LocalDateTime start = requireStartTime(request);
        LocalDateTime end = start.plusMinutes(durationMinutes);
        Long barberId = availabilityService.resolveBarberForSlot(
                        request.serviceId(),
                        candidates,
                        start,
                        end
                )
                .orElseThrow(() -> new BusinessRuleException("No available barber for selected time slot."));
        return new BarberAssignment(barberId, start, end);
    }

    private LocalDateTime requireStartTime(CreateAppointmentRequest request) {
        if (request.startTime() == null) {
            throw new BusinessRuleException("startTime is required.");
        }
        return request.startTime();
    }

    private String generateUniqueBookingToken() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String token = randomToken();
            if (appointmentRepository.findByBookingToken(token).isEmpty()) {
                return token;
            }
        }
        throw new IllegalStateException("Unable to generate unique booking token.");
    }

    private String randomToken() {
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
                a.getService().getId(),
                a.getBarberId(),
                a.getStartTime(),
                a.getEndTime(),
                a.getBookingToken(),
                a.isCanceled(),
                a.getStatus() != null ? a.getStatus().name() : null
        );
    }

    private StaffAppointmentResponse toStaffResponse(Appointment a) {
        return new StaffAppointmentResponse(
                a.getId(),
                a.getService().getId(),
                a.getService().getName(),
                a.getBarberId(),
                a.getStartTime(),
                a.getEndTime(),
                a.getFirstName(),
                a.getLastName(),
                a.getPhoneNumber(),
                a.getEmail(),
                a.getBookingToken(),
                a.isCanceled(),
                a.getStatus() != null ? a.getStatus().name() : null
        );
    }
}
