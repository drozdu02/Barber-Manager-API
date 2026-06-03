package com.barber_manager.appointment_service.repository;

import com.barber_manager.appointment_service.entity.BarberWorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface BarberWorkScheduleRepository extends JpaRepository<BarberWorkSchedule, Long> {

    List<BarberWorkSchedule> findAllByBarberIdOrderByDayOfWeekAsc(Long barberId);

    Optional<BarberWorkSchedule> findByBarberIdAndDayOfWeek(Long barberId, DayOfWeek dayOfWeek);

    void deleteAllByBarberId(Long barberId);
}
