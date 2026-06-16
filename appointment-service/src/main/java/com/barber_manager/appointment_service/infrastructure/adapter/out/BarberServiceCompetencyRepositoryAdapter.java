package com.barber_manager.appointment_service.infrastructure.adapter.out;

import com.barber_manager.appointment_service.entity.BarberServiceCompetency;
import com.barber_manager.appointment_service.repository.BarberServiceCompetencyRepository;
import com.barber_manager.appointment_service.schedule.port.out.IBarberServiceCompetencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BarberServiceCompetencyRepositoryAdapter implements IBarberServiceCompetencyRepository {

    private final BarberServiceCompetencyRepository competencyRepository;

    @Override
    public boolean existsByServiceId(Long serviceId) {
        return competencyRepository.existsByServiceId(serviceId);
    }

    @Override
    public boolean existsByBarberIdAndServiceId(Long barberId, Long serviceId) {
        return competencyRepository.existsByBarberIdAndServiceId(barberId, serviceId);
    }

    @Override
    public List<Long> findBarberIdsByServiceId(Long serviceId) {
        return competencyRepository.findBarberIdsByServiceId(serviceId);
    }

    @Override
    public BarberServiceCompetency save(BarberServiceCompetency competency) {
        return competencyRepository.save(competency);
    }

    @Override
    public void deleteByBarberIdAndServiceId(Long barberId, Long serviceId) {
        competencyRepository.deleteByBarberIdAndServiceId(barberId, serviceId);
    }
}
