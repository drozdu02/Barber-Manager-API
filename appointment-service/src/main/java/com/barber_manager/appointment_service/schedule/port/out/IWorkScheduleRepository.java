package com.barber_manager.appointment_service.schedule.port.out;

import com.barber_manager.appointment_service.entity.BarberWorkSchedule;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface IWorkScheduleRepository {

    List<BarberWorkSchedule> findAllByBarberIdOrderByDayOfWeekAsc(Long barberId);

    Optional<BarberWorkSchedule> findById(Long id);

    Optional<BarberWorkSchedule> findByBarberIdAndDayOfWeek(Long barberId, DayOfWeek dayOfWeek);

    BarberWorkSchedule save(BarberWorkSchedule schedule);

    void delete(BarberWorkSchedule schedule);

    void deleteAllByBarberId(Long barberId);
}
