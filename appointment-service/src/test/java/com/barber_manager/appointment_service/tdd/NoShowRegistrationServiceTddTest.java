package com.barber_manager.appointment_service.tdd;

import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.entity.ClientPhoneProfile;
import com.barber_manager.appointment_service.entity.NoShowIncident;
import com.barber_manager.appointment_service.entity.Service;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.booking.port.out.IClientPhoneProfileRepository;
import com.barber_manager.appointment_service.booking.port.out.INoShowIncidentRepository;
import com.barber_manager.appointment_service.service.ClientBlockService;
import com.barber_manager.appointment_service.service.NoShowRegistrationService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@DisplayName("TDD: NoShowRegistrationService — rejestracja no-show")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoShowRegistrationServiceTddTest {

    private static final String PHONE = "987654321";

    @InjectMocks
    private NoShowRegistrationService noShowRegistrationService;

    @Mock
    private IClientPhoneProfileRepository clientPhoneProfileRepository;

    @Mock
    private INoShowIncidentRepository noShowIncidentRepository;

    @Mock
    private ClientBlockService clientBlockService;

    @Test
    @Order(1)
    @DisplayName("Krok 1 (Red→Green): pierwszy no-show tworzy profil i inkrementuje licznik")
    void step1_firstNoShowShouldCreateProfileAndIncrementCounter() {
        Appointment appointment = sampleAppointment(1L);
        when(noShowIncidentRepository.existsByAppointmentId(1L)).thenReturn(false);
        when(clientPhoneProfileRepository.findByPhoneNumber(PHONE)).thenReturn(Optional.empty());

        noShowRegistrationService.registerNoShow(appointment);

        ArgumentCaptor<ClientPhoneProfile> profileCaptor = ArgumentCaptor.forClass(ClientPhoneProfile.class);
        verify(clientPhoneProfileRepository).save(profileCaptor.capture());
        assertEquals(1, profileCaptor.getValue().getNoShowCount());
        assertEquals(PHONE, profileCaptor.getValue().getPhoneNumber());

        verify(noShowIncidentRepository).save(any(NoShowIncident.class));
        verify(clientBlockService).applyBlockIfNoShowThresholdExceeded(PHONE, 1);
    }

    @Test
    @Order(2)
    @DisplayName("Krok 2 (Refactor): ponowna rejestracja tej samej wizyty jest ignorowana (idempotencja)")
    void step2_duplicateNoShowForSameAppointmentShouldBeIgnored() {
        Appointment appointment = sampleAppointment(5L);
        when(noShowIncidentRepository.existsByAppointmentId(5L)).thenReturn(true);

        noShowRegistrationService.registerNoShow(appointment);

        verify(clientPhoneProfileRepository, never()).save(any());
        verify(noShowIncidentRepository, never()).save(any());
        verify(clientBlockService, never()).applyBlockIfNoShowThresholdExceeded(any(), eq(0));
    }

    @Test
    @Order(3)
    @DisplayName("Krok 3 (Refactor): brak profilu przy odczycie zwraca NotFoundException")
    void step3_missingProfileShouldThrowNotFound() {
        when(clientPhoneProfileRepository.findByPhoneNumber(PHONE)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> noShowRegistrationService.getPhoneProfile(PHONE));
    }

    @Test
    @Order(4)
    @DisplayName("Krok 4 (Green): profil zwraca liczbę no-show i status blokady")
    void step4_existingProfileShouldExposeNoShowCountAndBlockStatus() {
        ClientPhoneProfile profile = new ClientPhoneProfile();
        profile.setPhoneNumber(PHONE);
        profile.setNoShowCount(2);
        when(clientPhoneProfileRepository.findByPhoneNumber(PHONE)).thenReturn(Optional.of(profile));
        when(noShowIncidentRepository.findAllByPhoneNumberOrderByRegisteredAtDesc(PHONE)).thenReturn(List.of());
        when(clientBlockService.getNoShowThreshold()).thenReturn(3);
        when(clientBlockService.isBlocked(PHONE)).thenReturn(false);

        var response = noShowRegistrationService.getPhoneProfile(PHONE);

        assertEquals(2, response.noShowCount());
        assertEquals(3, response.noShowThreshold());
        assertEquals(false, response.blocked());
    }

    private static Appointment sampleAppointment(long id) {
        Service service = new Service();
        service.setId(1L);

        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setService(service);
        appointment.setPhoneNumber(PHONE);
        appointment.setStartTime(LocalDateTime.of(2026, 6, 10, 9, 0));
        appointment.setEndTime(LocalDateTime.of(2026, 6, 10, 9, 30));
        return appointment;
    }
}
