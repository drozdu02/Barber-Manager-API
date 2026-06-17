package com.barber_manager.appointment_service.infrastructure.adapter.out;

import com.barber_manager.appointment_service.entity.BarberBreak;
import com.barber_manager.appointment_service.repository.BarberBreakRepository;
import com.barber_manager.appointment_service.schedule.port.out.IBarberBreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BarberBreakRepositoryAdapter implements IBarberBreakRepository {

    private final BarberBreakRepository barberBreakRepository;

    @Override
    public BarberBreak save(BarberBreak barberBreak) {
        return barberBreakRepository.save(barberBreak);
    }

    @Override
    public Optional<BarberBreak> findById(Long id) {
        return barberBreakRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        barberBreakRepository.deleteById(id);
    }

    @Override
    public List<BarberBreak> findOverlapping(Long barberId, LocalDateTime start, LocalDateTime end) {
        return barberBreakRepository.findOverlapping(barberId, start, end);
    }
}
