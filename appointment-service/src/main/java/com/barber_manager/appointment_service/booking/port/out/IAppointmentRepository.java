package com.barber_manager.appointment_service.booking.port.out;

import com.barber_manager.appointment_service.entity.Appointment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IAppointmentRepository {

    Appointment save(Appointment appointment);

    Optional<Appointment> findById(Long id);

    Optional<Appointment> findByBookingToken(String bookingToken);

    List<Appointment> findAllByBarberIdAndStartTimeBetweenAndCanceledFalse(
            Long barberId,
            LocalDateTime from,
            LocalDateTime to
    );

    List<Appointment> findAllByBarberIdAndCanceledFalseAndStartTimeAfter(Long barberId, LocalDateTime after);

    List<Appointment> findAllByBarberIdAndCanceledFalseAndStartTimeBetween(
            Long barberId,
            LocalDateTime from,
            LocalDateTime to
    );

    List<Appointment> findAllByPhoneNumberAndCanceledFalseAndStartTimeAfter(
            String phoneNumber,
            LocalDateTime after
    );

    List<Appointment> findDueForReminder(LocalDateTime now, LocalDateTime sendBefore);

    List<Appointment> findOverlapping(Long barberId, LocalDateTime start, LocalDateTime end);
}
