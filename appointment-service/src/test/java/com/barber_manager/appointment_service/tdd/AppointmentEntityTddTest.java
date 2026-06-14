package com.barber_manager.appointment_service.tdd;

import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.enums.AppointmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDD — encja {@link Appointment}: domyślny stan nowej wizyty.
 */
@DisplayName("TDD: encja Appointment — stan początkowy i anulowanie")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AppointmentEntityTddTest {

    @Test
    @Order(1)
    @DisplayName("Krok 1 (Red→Green): nowa wizyta ma status BOOKED i nie jest anulowana")
    void step1_newAppointmentShouldDefaultToBookedAndActive() {
        // Given — tworzymy pustą encję (jak przy pierwszym teście przed pełną implementacją)
        Appointment appointment = new Appointment();

        // Then — kontrakt domeny
        assertEquals(AppointmentStatus.BOOKED, appointment.getStatus());
        assertFalse(appointment.isCanceled());
        assertFalse(appointment.isReminderSent());
    }

    @Test
    @Order(2)
    @DisplayName("Krok 2 (Refactor): anulowanie wizyty zmienia status na CANCELED")
    void step2_cancelingAppointmentShouldUpdateStatus() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.BOOKED);

        // When — reguła anulowania (później używana w serwisie i politykach zdarzeń)
        appointment.setCanceled(true);
        appointment.setStatus(AppointmentStatus.CANCELED);

        // Then
        assertTrue(appointment.isCanceled());
        assertEquals(AppointmentStatus.CANCELED, appointment.getStatus());
    }
}
