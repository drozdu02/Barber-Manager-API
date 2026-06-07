package com.barber_manager.appointment_service.repository;

import com.barber_manager.appointment_service.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByBookingToken(String bookingToken);

    List<Appointment> findAllByBarberIdAndStartTimeBetweenAndCanceledFalse(Long barberId, LocalDateTime from, LocalDateTime to);

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

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.canceled = false
              AND a.status = com.barber_manager.appointment_service.enums.AppointmentStatus.BOOKED
              AND a.reminderSent = false
              AND a.startTime > :now
              AND a.startTime <= :sendBefore
            """)
    List<Appointment> findDueForReminder(
            @Param("now") LocalDateTime now,
            @Param("sendBefore") LocalDateTime sendBefore
    );

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.canceled = false
              AND (:barberId IS NULL OR a.barberId = :barberId)
              AND a.startTime < :end
              AND a.endTime > :start
            """)
    List<Appointment> findOverlapping(
            @Param("barberId") Long barberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}

