package com.barber_manager.appointment_service.infrastructure.adapter.out;

import com.barber_manager.appointment_service.entity.BarberWorkSchedule;
import com.barber_manager.appointment_service.repository.BarberWorkScheduleRepository;
import com.barber_manager.appointment_service.schedule.port.out.IWorkScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkScheduleRepositoryAdapter implements IWorkScheduleRepository {

    private final BarberWorkScheduleRepository workScheduleRepository;

    @Override
    public List<BarberWorkSchedule> findAllByBarberIdOrderByDayOfWeekAsc(Long barberId) {
        return workScheduleRepository.findAllByBarberIdOrderByDayOfWeekAsc(barberId);
    }

    @Override
    public Optional<BarberWorkSchedule> findById(Long id) {
        return workScheduleRepository.findById(id);
    }

    @Override
    public Optional<BarberWorkSchedule> findByBarberIdAndDayOfWeek(Long barberId, DayOfWeek dayOfWeek) {
        return workScheduleRepository.findByBarberIdAndDayOfWeek(barberId, dayOfWeek);
    }

    @Override
    public BarberWorkSchedule save(BarberWorkSchedule schedule) {
        return workScheduleRepository.save(schedule);
    }

    @Override
    public void delete(BarberWorkSchedule schedule) {
        workScheduleRepository.delete(schedule);
    }

    @Override
    public void deleteAllByBarberId(Long barberId) {
        workScheduleRepository.deleteAllByBarberId(barberId);
    }
}
