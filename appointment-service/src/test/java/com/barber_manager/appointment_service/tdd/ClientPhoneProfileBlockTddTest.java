package com.barber_manager.appointment_service.tdd;

import com.barber_manager.appointment_service.config.ClientBlockProperties;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneRequest;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneResponse;
import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.entity.ClientPhoneProfile;
import com.barber_manager.appointment_service.events.ClientBlockedEvent;
import com.barber_manager.appointment_service.events.DomainEvent;
import com.barber_manager.appointment_service.events.DomainEventPublisher;
import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.ClientPhoneProfileRepository;
import com.barber_manager.appointment_service.service.NoShowRegistrationService;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@DisplayName("TDD: ClientPhoneProfile — blokada klienta w agregacie")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientPhoneProfileBlockTddTest {

    private static final String PHONE = "123456789";
    private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 10, 10, 0);

    @InjectMocks
    private NoShowRegistrationService noShowRegistrationService;

    @Mock
    private ClientPhoneProfileRepository clientPhoneProfileRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ClientBlockProperties clientBlockProperties;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @BeforeEach
    void setUp() {
        lenient().when(clientBlockProperties.getNoShowThreshold()).thenReturn(3);
        lenient().when(domainEventPublisher.newEventId()).thenReturn(EVENT_ID);
        lenient().when(domainEventPublisher.now()).thenReturn(NOW);
        lenient().when(appointmentRepository.findAllByPhoneNumberAndCanceledFalseAndStartTimeAfter(any(), any()))
                .thenReturn(List.of());
    }

    @Test
    @Order(1)
    @DisplayName("Krok 1 (Red→Green): profil bez blokady nie jest zablokowany")
    void step1_profileWithoutBlockShouldNotBeBlocked() {
        ClientPhoneProfile profile = new ClientPhoneProfile();
        profile.setPhoneNumber(PHONE);
        profile.setBlocked(false);

        assertFalse(profile.isBlocked());
    }

    @Test
    @Order(2)
    @DisplayName("Krok 2 (Red→Green): block() ustawia flagę i rejestruje ClientBlockedEvent w agregacie")
    void step2_blockShouldSetFlagAndRegisterDomainEvent() {
        ClientPhoneProfile profile = new ClientPhoneProfile();
        profile.setId(42L);
        profile.setPhoneNumber(PHONE);

        profile.block(EVENT_ID, NOW, "Manual block", false, List.of());

        assertTrue(profile.isBlocked());
        List<DomainEvent> events = profile.pullDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.getFirst() instanceof ClientBlockedEvent);
        assertEquals(PHONE, ((ClientBlockedEvent) events.getFirst()).phoneNumber());
    }

    @Test
    @Order(3)
    @DisplayName("Krok 3 (Red→Green): ręczna blokada przez serwis publikuje zdarzenie z agregatu")
    void step3_manualBlockThroughServiceShouldPublishEvent() {
        ClientPhoneProfile profile = new ClientPhoneProfile();
        profile.setPhoneNumber(PHONE);
        when(clientPhoneProfileRepository.findByPhoneNumber(PHONE)).thenReturn(Optional.empty());
        when(clientPhoneProfileRepository.save(any(ClientPhoneProfile.class))).thenAnswer(invocation -> {
            ClientPhoneProfile saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        BlockPhoneResponse response = noShowRegistrationService.blockPhone(
                new BlockPhoneRequest(PHONE, "Powtarzające się no-show")
        );

        assertEquals(PHONE, response.phoneNumber());
        assertTrue(response.active());

        ArgumentCaptor<ClientBlockedEvent> eventCaptor = ArgumentCaptor.forClass(ClientBlockedEvent.class);
        verify(domainEventPublisher).publish(eventCaptor.capture());
        assertEquals(PHONE, eventCaptor.getValue().phoneNumber());
        assertFalse(eventCaptor.getValue().automatic());
    }

    @Test
    @Order(4)
    @DisplayName("Krok 4 (Refactor): odblokowanie zeruje flagę blocked")
    void step4_unblockShouldClearBlockedFlag() {
        ClientPhoneProfile profile = new ClientPhoneProfile();
        profile.setPhoneNumber(PHONE);
        profile.setBlocked(true);
        when(clientPhoneProfileRepository.findByPhoneNumber(PHONE)).thenReturn(Optional.of(profile));

        noShowRegistrationService.unblockPhone(PHONE);

        assertFalse(profile.isBlocked());
        verify(clientPhoneProfileRepository).save(profile);
    }

    @Test
    @Order(5)
    @DisplayName("Krok 5 (Refactor): shouldAutoBlock nie uruchamia się poniżej progu")
    void step5_shouldNotAutoBlockBelowThreshold() {
        ClientPhoneProfile profile = new ClientPhoneProfile();
        profile.setPhoneNumber(PHONE);
        profile.setNoShowCount(2);

        assertFalse(profile.shouldAutoBlock(3));
        verify(domainEventPublisher, never()).publish(any());
    }
}
