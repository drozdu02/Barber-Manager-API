package com.barber_manager.appointment_service.integration;

import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.entity.Service;
import com.barber_manager.appointment_service.enums.AppointmentStatus;
import com.barber_manager.appointment_service.repository.AppointmentRepository;
import com.barber_manager.appointment_service.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("integration")
@DisplayName("Integracja: AppointmentRepository (adapter JPA)")
class AppointmentRepositoryIntegrationTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    private Service haircut;

    @BeforeEach
    void setUp() {
        haircut = new Service();
        haircut.setName("Strzyżenie");
        haircut.setPrice(new BigDecimal("50.00"));
        haircut.setSlotCount(1);
        haircut = serviceRepository.save(haircut);
    }

    @Test
    void shouldFindAppointmentByBookingToken() {
        appointmentRepository.save(sampleAppointment("token-abc", 10L,
                LocalDateTime.of(2026, 6, 15, 10, 0),
                LocalDateTime.of(2026, 6, 15, 10, 30)));

        Appointment found = appointmentRepository.findByBookingToken("token-abc").orElseThrow();

        assertEquals("123456789", found.getPhoneNumber());
        assertEquals(AppointmentStatus.BOOKED, found.getStatus());
    }

    @Test
    void shouldFindOverlappingAppointmentsForBarber() {
        appointmentRepository.save(sampleAppointment("token-1", 5L,
                LocalDateTime.of(2026, 6, 15, 10, 0),
                LocalDateTime.of(2026, 6, 15, 10, 30)));
        appointmentRepository.save(sampleAppointment("token-2", 5L,
                LocalDateTime.of(2026, 6, 15, 11, 0),
                LocalDateTime.of(2026, 6, 15, 11, 30)));

        List<Appointment> overlaps = appointmentRepository.findOverlapping(
                5L,
                LocalDateTime.of(2026, 6, 15, 10, 15),
                LocalDateTime.of(2026, 6, 15, 10, 45)
        );

        assertEquals(1, overlaps.size());
        assertEquals("token-1", overlaps.get(0).getBookingToken());
    }

    @Test
    void shouldFindFutureAppointmentsByPhoneNumber() {
        appointmentRepository.save(sampleAppointment("token-future", 7L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusMinutes(30)));
        appointmentRepository.save(sampleAppointment("token-past", 7L,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(2).plusMinutes(30)));

        List<Appointment> future = appointmentRepository.findAllByPhoneNumberAndCanceledFalseAndStartTimeAfter(
                "123456789",
                LocalDateTime.now()
        );

        assertEquals(1, future.size());
        assertEquals("token-future", future.get(0).getBookingToken());
        assertTrue(future.stream().noneMatch(a -> "token-past".equals(a.getBookingToken())));
    }

    private Appointment sampleAppointment(
            String bookingToken,
            Long barberId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        Appointment appointment = new Appointment();
        appointment.setBarberId(barberId);
        appointment.setService(haircut);
        appointment.setStartTime(start);
        appointment.setEndTime(end);
        appointment.setFirstName("Jan");
        appointment.setLastName("Klient");
        appointment.setPhoneNumber("123456789");
        appointment.setEmail("jan@klient.pl");
        appointment.setBookingToken(bookingToken);
        appointment.setCanceled(false);
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setReminderSent(false);
        return appointment;
    }
}
