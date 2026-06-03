package com.barber_manager.appointment_service.repository;

import com.barber_manager.appointment_service.entity.BarberTimeOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BarberTimeOffRepository extends JpaRepository<BarberTimeOff, Long> {

    List<BarberTimeOff> findAllByBarberIdOrderByStartDateAsc(Long barberId);

    @Query("""
            SELECT t FROM BarberTimeOff t
            WHERE t.barberId = :barberId
              AND t.startDate <= :date
              AND t.endDate >= :date
            """)
    List<BarberTimeOff> findActiveOnDate(
            @Param("barberId") Long barberId,
            @Param("date") LocalDate date
    );
}
