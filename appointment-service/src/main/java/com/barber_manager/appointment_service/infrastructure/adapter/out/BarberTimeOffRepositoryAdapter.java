package com.barber_manager.appointment_service.infrastructure.adapter.out;

import com.barber_manager.appointment_service.entity.BarberTimeOff;
import com.barber_manager.appointment_service.repository.BarberTimeOffRepository;
import com.barber_manager.appointment_service.schedule.port.out.IBarberTimeOffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BarberTimeOffRepositoryAdapter implements IBarberTimeOffRepository {

    private final BarberTimeOffRepository timeOffRepository;

    @Override
    public List<BarberTimeOff> findAllByBarberIdOrderByStartDateAsc(Long barberId) {
        return timeOffRepository.findAllByBarberIdOrderByStartDateAsc(barberId);
    }

    @Override
    public List<BarberTimeOff> findActiveOnDate(Long barberId, LocalDate date) {
        return timeOffRepository.findActiveOnDate(barberId, date);
    }

    @Override
    public BarberTimeOff save(BarberTimeOff timeOff) {
        return timeOffRepository.save(timeOff);
    }

    @Override
    public boolean existsById(Long id) {
        return timeOffRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        timeOffRepository.deleteById(id);
    }
}
