package com.barber_manager.appointment_service.schedule.port.out;

import com.barber_manager.appointment_service.entity.BarberTimeOff;

import java.time.LocalDate;
import java.util.List;

public interface IBarberTimeOffRepository {

    List<BarberTimeOff> findAllByBarberIdOrderByStartDateAsc(Long barberId);

    List<BarberTimeOff> findActiveOnDate(Long barberId, LocalDate date);

    BarberTimeOff save(BarberTimeOff timeOff);

    boolean existsById(Long id);

    void deleteById(Long id);
}
