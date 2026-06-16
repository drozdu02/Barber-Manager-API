package com.barber_manager.appointment_service.tdd;

import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.entity.Service;
import com.barber_manager.appointment_service.enums.AppointmentStatus;
import com.barber_manager.appointment_service.events.AppointmentCanceledEvent;
import com.barber_manager.appointment_service.events.CancellationSource;
import com.barber_manager.appointment_service.events.ClientBlockedEvent;
import com.barber_manager.appointment_service.events.DomainEventPublisher;
import com.barber_manager.appointment_service.events.handlers.ClientBlockedPolicy;
import com.barber_manager.appointment_service.booking.port.out.IAppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@DisplayName("TDD: ClientBlockedPolicy — anulowanie wizyt po blokadzie klienta")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientBlockedPolicyTddTest {

    private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 10, 12, 0);

    @InjectMocks
    private ClientBlockedPolicy clientBlockedPolicy;

    @Mock
    private IAppointmentRepository appointmentRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @BeforeEach
    void setUp() {
        lenient().when(domainEventPublisher.newEventId()).thenReturn(EVENT_ID);
        lenient().when(domainEventPublisher.now()).thenReturn(NOW);
    }

    @Test
    @Order(1)
    @DisplayName("Krok 1 (Red→Green): aktywna wizyta jest anulowana po zdarzeniu blokady")
    void step1_activeAppointmentShouldBeCanceledOnClientBlocked() {
        Appointment appointment = activeAppointment(10L);
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        ClientBlockedEvent event = new ClientBlockedEvent(
                EVENT_ID, NOW, 1L, "123456789", "Manual block", false, List.of(10L)
        );

        clientBlockedPolicy.onClientBlocked(event);

        assertTrue(appointment.isCanceled());
        assertEquals(AppointmentStatus.CANCELED, appointment.getStatus());
    }

    @Test
    @Order(2)
    @DisplayName("Krok 2 (Refactor): już anulowana wizyta pozostaje bez zmian")
    void step2_alreadyCanceledAppointmentShouldBeSkipped() {
        Appointment appointment = activeAppointment(11L);
        appointment.setCanceled(true);
        appointment.setStatus(AppointmentStatus.CANCELED);
        when(appointmentRepository.findById(11L)).thenReturn(Optional.of(appointment));

        ClientBlockedEvent event = new ClientBlockedEvent(
                EVENT_ID, NOW, 2L, "123456789", "Manual block", false, List.of(11L)
        );

        clientBlockedPolicy.onClientBlocked(event);

        verify(appointmentRepository, never()).save(any());
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    @Order(3)
    @DisplayName("Krok 3 (Green): anulowanie publikuje AppointmentCanceledEvent ze źródłem CLIENT_BLOCK_CASCADE")
    void step3_cancelShouldPublishAppointmentCanceledEvent() {
        Appointment appointment = activeAppointment(12L);
        when(appointmentRepository.findById(12L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        ClientBlockedEvent event = new ClientBlockedEvent(
                EVENT_ID, NOW, 3L, "123456789", "Auto block", true, List.of(12L)
        );

        clientBlockedPolicy.onClientBlocked(event);

        ArgumentCaptor<AppointmentCanceledEvent> captor = ArgumentCaptor.forClass(AppointmentCanceledEvent.class);
        verify(domainEventPublisher).publish(captor.capture());
        assertEquals(12L, captor.getValue().appointmentId());
        assertEquals(CancellationSource.CLIENT_BLOCK_CASCADE, captor.getValue().source());
    }

    private static Appointment activeAppointment(long id) {
        Service service = new Service();
        service.setId(1L);

        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setService(service);
        appointment.setBarberId(5L);
        appointment.setPhoneNumber("123456789");
        appointment.setBookingToken("token-" + id);
        appointment.setStartTime(LocalDateTime.of(2026, 6, 15, 10, 0));
        appointment.setEndTime(LocalDateTime.of(2026, 6, 15, 10, 30));
        appointment.setCanceled(false);
        appointment.setStatus(AppointmentStatus.BOOKED);
        return appointment;
    }
}
