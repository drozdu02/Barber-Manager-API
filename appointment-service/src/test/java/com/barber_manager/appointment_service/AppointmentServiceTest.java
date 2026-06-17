package com.barber_manager.appointment_service;

import com.barber_manager.appointment_service.dto.AppointmentResponse;
import com.barber_manager.appointment_service.dto.BarberAssignment;
import com.barber_manager.appointment_service.dto.CreateAppointmentRequest;
import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.entity.ClientPhoneProfile;
import com.barber_manager.appointment_service.entity.Service;
import com.barber_manager.appointment_service.enums.AppointmentStatus;
import com.barber_manager.appointment_service.events.AppointmentBookedEvent;
import com.barber_manager.appointment_service.events.AppointmentCanceledEvent;
import com.barber_manager.appointment_service.events.CancellationSource;
import com.barber_manager.appointment_service.events.DomainEventPublisher;
import com.barber_manager.appointment_service.exception.BusinessRuleException;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.booking.port.in.IClientPhoneProfileController;
import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.ClientPhoneProfileRepository;
import com.barber_manager.appointment_service.repository.ServiceRepository;
import com.barber_manager.appointment_service.schedule.domain.AvailabilityService;
import com.barber_manager.appointment_service.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @InjectMocks
    private AppointmentService appointmentService;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private ClientPhoneProfileRepository clientPhoneProfileRepository;

    @Mock
    private IClientPhoneProfileController clientPhoneProfileController;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Test
    void shouldCreateAppointmentWithSpecificBarber() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 15, 10, 0);
        Service service = service(1L, 2);
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Jan",
                "Kowalski",
                "123456789",
                "jan@example.com",
                1L,
                start,
                10L,
                null,
                null
        );

        when(clientPhoneProfileRepository.findByPhoneNumber("123456789")).thenReturn(Optional.empty());
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(availabilityService.filterCompetentBarbers(1L, List.of(10L))).thenReturn(List.of(10L));
        when(appointmentRepository.findOverlapping(10L, start, start.plusMinutes(60))).thenReturn(List.of());
        when(appointmentRepository.findByBookingToken(any())).thenReturn(Optional.empty());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(domainEventPublisher.newEventId()).thenReturn(UUID.randomUUID());
        when(domainEventPublisher.now()).thenReturn(LocalDateTime.now());

        AppointmentResponse response = appointmentService.create(request);

        assertEquals(1L, response.id());
        assertEquals(10L, response.barberId());
        assertEquals("BOOKED", response.status());
        assertNotNull(response.bookingToken());

        ArgumentCaptor<AppointmentBookedEvent> eventCaptor = ArgumentCaptor.forClass(AppointmentBookedEvent.class);
        verify(domainEventPublisher).publish(eventCaptor.capture());
        assertEquals("jan@example.com", eventCaptor.getValue().email());
    }

    @Test
    void shouldCreateAppointmentWithAnyBarberAssignment() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 15, 10, 0);
        Service service = service(1L, 1);
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Jan",
                "Kowalski",
                "123456789",
                "jan@example.com",
                1L,
                start,
                null,
                List.of(10L, 11L),
                false
        );

        when(clientPhoneProfileRepository.findByPhoneNumber("123456789")).thenReturn(Optional.empty());
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(availabilityService.resolveBarberForSlot(1L, List.of(10L, 11L), start, start.plusMinutes(30)))
                .thenReturn(Optional.of(11L));
        when(appointmentRepository.findOverlapping(11L, start, start.plusMinutes(30))).thenReturn(List.of());
        when(appointmentRepository.findByBookingToken(any())).thenReturn(Optional.empty());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(domainEventPublisher.newEventId()).thenReturn(UUID.randomUUID());
        when(domainEventPublisher.now()).thenReturn(LocalDateTime.now());

        AppointmentResponse response = appointmentService.create(request);

        assertEquals(11L, response.barberId());
    }

    @Test
    void shouldCreateAppointmentWithEarliestSlotAssignment() {
        LocalDateTime assignedStart = LocalDateTime.of(2026, 6, 16, 9, 0);
        Service service = service(1L, 2);
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Jan",
                "Kowalski",
                "123456789",
                "jan@example.com",
                1L,
                null,
                null,
                List.of(10L),
                true
        );

        when(clientPhoneProfileRepository.findByPhoneNumber("123456789")).thenReturn(Optional.empty());
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(availabilityService.findEarliestAssignment(1L, List.of(10L), null))
                .thenReturn(Optional.of(new BarberAssignment(10L, assignedStart, assignedStart.plusMinutes(60))));
        when(appointmentRepository.findOverlapping(10L, assignedStart, assignedStart.plusMinutes(60))).thenReturn(List.of());
        when(appointmentRepository.findByBookingToken(any())).thenReturn(Optional.empty());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });
        when(domainEventPublisher.newEventId()).thenReturn(UUID.randomUUID());
        when(domainEventPublisher.now()).thenReturn(LocalDateTime.now());

        AppointmentResponse response = appointmentService.create(request);

        assertEquals(assignedStart, response.startTime());
        assertEquals(10L, response.barberId());
    }

    @Test
    void shouldRejectBlockedPhoneOnCreate() {
        ClientPhoneProfile blockedProfile = new ClientPhoneProfile();
        blockedProfile.setPhoneNumber("123456789");
        blockedProfile.setBlocked(true);
        when(clientPhoneProfileRepository.findByPhoneNumber("123456789"))
                .thenReturn(Optional.of(blockedProfile));

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Jan",
                "Kowalski",
                "123456789",
                "jan@example.com",
                1L,
                LocalDateTime.now().plusDays(2),
                10L,
                null,
                null
        );

        assertThrows(BusinessRuleException.class, () -> appointmentService.create(request));
    }

    @Test
    void shouldRejectCreateWhenServiceMissing() {
        when(clientPhoneProfileRepository.findByPhoneNumber(any())).thenReturn(Optional.empty());
        when(serviceRepository.findById(99L)).thenReturn(Optional.empty());

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Jan",
                "Kowalski",
                "123456789",
                "jan@example.com",
                99L,
                LocalDateTime.now().plusDays(2),
                10L,
                null,
                null
        );

        assertThrows(NotFoundException.class, () -> appointmentService.create(request));
    }

    @Test
    void shouldRejectCreateWhenSlotOverlaps() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 15, 10, 0);
        Service service = service(1L, 2);

        when(clientPhoneProfileRepository.findByPhoneNumber(any())).thenReturn(Optional.empty());
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(availabilityService.filterCompetentBarbers(1L, List.of(10L))).thenReturn(List.of(10L));
        when(appointmentRepository.findOverlapping(10L, start, start.plusMinutes(60)))
                .thenReturn(List.of(new Appointment()));

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Jan",
                "Kowalski",
                "123456789",
                "jan@example.com",
                1L,
                start,
                10L,
                null,
                null
        );

        assertThrows(BusinessRuleException.class, () -> appointmentService.create(request));
    }

    @Test
    void shouldCancelByBookingToken() {
        Appointment appointment = bookedAppointment(LocalDateTime.now().plusDays(2));
        when(appointmentRepository.findByBookingToken("TOKEN123")).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(domainEventPublisher.newEventId()).thenReturn(UUID.randomUUID());
        when(domainEventPublisher.now()).thenReturn(LocalDateTime.now());

        appointmentService.cancelByBookingToken("TOKEN123");

        assertTrue(appointment.isCanceled());
        assertEquals(AppointmentStatus.CANCELED, appointment.getStatus());

        ArgumentCaptor<AppointmentCanceledEvent> eventCaptor = ArgumentCaptor.forClass(AppointmentCanceledEvent.class);
        verify(domainEventPublisher).publish(eventCaptor.capture());
        assertEquals(CancellationSource.CLIENT_TOKEN, eventCaptor.getValue().source());
    }

    @Test
    void shouldIgnoreCancelWhenAlreadyCanceled() {
        Appointment appointment = bookedAppointment(LocalDateTime.now().plusDays(2));
        appointment.setCanceled(true);
        when(appointmentRepository.findByBookingToken("TOKEN123")).thenReturn(Optional.of(appointment));

        appointmentService.cancelByBookingToken("TOKEN123");

        verify(appointmentRepository, never()).save(any());
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void shouldRejectCancelWithinTwelveHours() {
        Appointment appointment = bookedAppointment(LocalDateTime.now().plusHours(6));
        when(appointmentRepository.findByBookingToken("TOKEN123")).thenReturn(Optional.of(appointment));

        assertThrows(BusinessRuleException.class, () -> appointmentService.cancelByBookingToken("TOKEN123"));
    }

    @Test
    void shouldRejectCancelWhenTokenMissing() {
        when(appointmentRepository.findByBookingToken("MISSING")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> appointmentService.cancelByBookingToken("MISSING"));
    }

    @Test
    void shouldUpdateStatusAndRegisterNoShow() {
        Appointment appointment = bookedAppointment(LocalDateTime.now().plusDays(1));
        appointment.setId(5L);
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        appointmentService.updateStatus(5L, AppointmentStatus.NO_SHOW);

        verify(clientPhoneProfileController).registerNoShow(appointment);
        assertEquals(AppointmentStatus.NO_SHOW, appointment.getStatus());
    }

    @Test
    void shouldPublishCanceledEventWhenStaffCancels() {
        Appointment appointment = bookedAppointment(LocalDateTime.now().plusDays(1));
        appointment.setId(6L);
        when(appointmentRepository.findById(6L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);
        when(domainEventPublisher.newEventId()).thenReturn(UUID.randomUUID());
        when(domainEventPublisher.now()).thenReturn(LocalDateTime.now());

        appointmentService.updateStatus(6L, AppointmentStatus.CANCELED);

        ArgumentCaptor<AppointmentCanceledEvent> eventCaptor = ArgumentCaptor.forClass(AppointmentCanceledEvent.class);
        verify(domainEventPublisher).publish(eventCaptor.capture());
        assertEquals(CancellationSource.STAFF_STATUS, eventCaptor.getValue().source());
    }

    @Test
    void shouldReturnStaffDetails() {
        Appointment appointment = bookedAppointment(LocalDateTime.now().plusDays(1));
        appointment.setId(7L);
        when(appointmentRepository.findById(7L)).thenReturn(Optional.of(appointment));

        var response = appointmentService.getStaffDetails(7L);

        assertEquals("Jan", response.firstName());
        assertEquals("jan@example.com", response.email());
    }

    @Test
    void shouldReturnBarberCalendar() {
        Appointment appointment = bookedAppointment(LocalDateTime.now().plusDays(1));
        appointment.setBarberId(10L);
        LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 30, 23, 59);
        when(appointmentRepository.findAllByBarberIdAndStartTimeBetweenAndCanceledFalse(10L, from, to))
                .thenReturn(List.of(appointment));

        var calendar = appointmentService.getBarberCalendar(10L, from, to);

        assertEquals(1, calendar.size());
        assertEquals(10L, calendar.getFirst().barberId());
    }

    private Service service(Long id, int slotCount) {
        Service service = new Service();
        service.setId(id);
        service.setSlotCount(slotCount);
        service.setName("Haircut");
        return service;
    }

    private Appointment bookedAppointment(LocalDateTime start) {
        Appointment appointment = new Appointment();
        appointment.setService(service(1L, 2));
        appointment.setStartTime(start);
        appointment.setEndTime(start.plusMinutes(60));
        appointment.setFirstName("Jan");
        appointment.setLastName("Kowalski");
        appointment.setPhoneNumber("123456789");
        appointment.setEmail("jan@example.com");
        appointment.setBookingToken("TOKEN123");
        appointment.setCanceled(false);
        appointment.setStatus(AppointmentStatus.BOOKED);
        return appointment;
    }
}
