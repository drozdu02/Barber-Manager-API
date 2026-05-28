package com.barber_manager.appointment_service.repository;

import com.barber_manager.appointment_service.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByReservationCode(String reservationCode);

    List<Appointment> findAllByBarberIdAndStartTimeBetweenAndCanceledFalse(Long barberId, LocalDateTime from, LocalDateTime to);

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

