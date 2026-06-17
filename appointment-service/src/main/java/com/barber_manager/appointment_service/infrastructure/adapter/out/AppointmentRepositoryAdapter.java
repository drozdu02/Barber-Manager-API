package com.barber_manager.appointment_service.infrastructure.adapter.out;

import com.barber_manager.appointment_service.booking.port.out.IAppointmentRepository;
import com.barber_manager.appointment_service.entity.Appointment;
import com.barber_manager.appointment_service.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AppointmentRepositoryAdapter implements IAppointmentRepository {

    private final AppointmentRepository appointmentRepository;

    @Override
    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Override
    public Optional<Appointment> findByBookingToken(String bookingToken) {
        return appointmentRepository.findByBookingToken(bookingToken);
    }

    @Override
    public List<Appointment> findAllByBarberIdAndStartTimeBetweenAndCanceledFalse(
            Long barberId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return appointmentRepository.findAllByBarberIdAndStartTimeBetweenAndCanceledFalse(barberId, from, to);
    }

    @Override
    public List<Appointment> findAllByBarberIdAndCanceledFalseAndStartTimeAfter(Long barberId, LocalDateTime after) {
        return appointmentRepository.findAllByBarberIdAndCanceledFalseAndStartTimeAfter(barberId, after);
    }

    @Override
    public List<Appointment> findAllByBarberIdAndCanceledFalseAndStartTimeBetween(
            Long barberId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return appointmentRepository.findAllByBarberIdAndCanceledFalseAndStartTimeBetween(barberId, from, to);
    }

    @Override
    public List<Appointment> findAllByPhoneNumberAndCanceledFalseAndStartTimeAfter(
            String phoneNumber,
            LocalDateTime after
    ) {
        return appointmentRepository.findAllByPhoneNumberAndCanceledFalseAndStartTimeAfter(phoneNumber, after);
    }

    @Override
    public List<Appointment> findDueForReminder(LocalDateTime now, LocalDateTime sendBefore) {
        return appointmentRepository.findDueForReminder(now, sendBefore);
    }

    @Override
    public List<Appointment> findOverlapping(Long barberId, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findOverlapping(barberId, start, end);
    }
}
