package com.barber_manager.appointment_service.schedule.port.out;

import com.barber_manager.appointment_service.entity.BarberBreak;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IBarberBreakRepository {

    BarberBreak save(BarberBreak barberBreak);

    Optional<BarberBreak> findById(Long id);

    void deleteById(Long id);

    List<BarberBreak> findOverlapping(Long barberId, LocalDateTime start, LocalDateTime end);
}
