package com.barber_manager.appointment_service.tdd;

import com.barber_manager.appointment_service.config.ClientBlockProperties;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneRequest;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneResponse;
import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.entity.BlockedPhoneNumber;
import com.barber_manager.appointment_service.events.ClientBlockedEvent;
import com.barber_manager.appointment_service.events.DomainEventPublisher;
import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.BlockedPhoneNumberRepository;
import com.barber_manager.appointment_service.service.ClientBlockService;
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


@DisplayName("TDD: ClientBlockService — blokada numeru telefonu")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientBlockServiceTddTest {

    private static final String PHONE = "123456789";
    private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 10, 10, 0);

    @InjectMocks
    private ClientBlockService clientBlockService;

    @Mock
    private BlockedPhoneNumberRepository blockedPhoneNumberRepository;

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
    @DisplayName("Krok 1 (Red→Green): numer bez aktywnej blokady nie jest zablokowany")
    void step1_phoneWithoutBlockShouldNotBeBlocked() {
        when(blockedPhoneNumberRepository.findByPhoneNumberAndActiveTrue(PHONE)).thenReturn(Optional.empty());

        boolean blocked = clientBlockService.isBlocked(PHONE);

        assertFalse(blocked);
    }

    @Test
    @Order(2)
    @DisplayName("Krok 2 (Red→Green): ręczna blokada zapisuje numer i publikuje ClientBlockedEvent")
    void step2_manualBlockShouldPersistAndPublishEvent() {
        when(blockedPhoneNumberRepository.findByPhoneNumber(PHONE)).thenReturn(Optional.empty());
        when(blockedPhoneNumberRepository.save(any(BlockedPhoneNumber.class))).thenAnswer(invocation -> {
            BlockedPhoneNumber block = invocation.getArgument(0);
            block.setId(42L);
            return block;
        });

        BlockPhoneResponse response = clientBlockService.blockPhone(
                new BlockPhoneRequest(PHONE, "Powtarzające się no-show")
        );

        assertEquals(PHONE, response.phoneNumber());
        assertTrue(response.active());
        assertFalse(response.automatic());

        ArgumentCaptor<ClientBlockedEvent> eventCaptor = ArgumentCaptor.forClass(ClientBlockedEvent.class);
        verify(domainEventPublisher).publish(eventCaptor.capture());
        assertEquals(PHONE, eventCaptor.getValue().phoneNumber());
        assertFalse(eventCaptor.getValue().automatic());
    }

    @Test
    @Order(3)
    @DisplayName("Krok 3 (Red→Green): auto-blokada nie uruchamia się poniżej progu no-show")
    void step3_autoBlockShouldNotTriggerBelowThreshold() {
        Optional<BlockPhoneResponse> result = clientBlockService.applyBlockIfNoShowThresholdExceeded(PHONE, 2);

        assertTrue(result.isEmpty());
        verify(blockedPhoneNumberRepository, never()).save(any());
    }

    @Test
    @Order(4)
    @DisplayName("Krok 4 (Refactor): auto-blokada uruchamia się po przekroczeniu progu no-show")
    void step4_autoBlockShouldTriggerAtThreshold() {
        when(blockedPhoneNumberRepository.findByPhoneNumberAndActiveTrue(PHONE)).thenReturn(Optional.empty());
        when(blockedPhoneNumberRepository.findByPhoneNumber(PHONE)).thenReturn(Optional.empty());
        when(blockedPhoneNumberRepository.save(any(BlockedPhoneNumber.class))).thenAnswer(invocation -> {
            BlockedPhoneNumber block = invocation.getArgument(0);
            block.setId(7L);
            return block;
        });
        Appointment future = new Appointment();
        future.setId(100L);
        when(appointmentRepository.findAllByPhoneNumberAndCanceledFalseAndStartTimeAfter(any(), any()))
                .thenReturn(List.of(future));

        Optional<BlockPhoneResponse> result = clientBlockService.applyBlockIfNoShowThresholdExceeded(PHONE, 3);

        assertTrue(result.isPresent());
        assertTrue(result.get().automatic());
        assertEquals(1, result.get().canceledAppointments());
    }

    @Test
    @Order(5)
    @DisplayName("Krok 5 (Refactor): odblokowanie dezaktywuje istniejącą blokadę")
    void step5_unblockShouldDeactivateExistingBlock() {
        BlockedPhoneNumber block = new BlockedPhoneNumber();
        block.setId(1L);
        block.setPhoneNumber(PHONE);
        block.setActive(true);
        when(blockedPhoneNumberRepository.findByPhoneNumberAndActiveTrue(PHONE)).thenReturn(Optional.of(block));

        clientBlockService.unblockPhone(PHONE);

        assertFalse(block.isActive());
        verify(blockedPhoneNumberRepository).save(block);
    }
}
