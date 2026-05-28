package com.barber_manager.appointment_service.repository;

import com.barber_manager.appointment_service.entity.BarberBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BarberBreakRepository extends JpaRepository<BarberBreak, Long> {

    @Query("""
            SELECT b FROM BarberBreak b
            WHERE b.barberId = :barberId
              AND b.startTime < :end
              AND b.endTime > :start
            """)
    List<BarberBreak> findOverlapping(
            @Param("barberId") Long barberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}

