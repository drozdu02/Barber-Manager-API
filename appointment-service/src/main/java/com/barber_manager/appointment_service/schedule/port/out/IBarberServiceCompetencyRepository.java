package com.barber_manager.appointment_service.schedule.port.out;

import com.barber_manager.appointment_service.entity.BarberServiceCompetency;

import java.util.List;

public interface IBarberServiceCompetencyRepository {

    boolean existsByServiceId(Long serviceId);

    boolean existsByBarberIdAndServiceId(Long barberId, Long serviceId);

    List<Long> findBarberIdsByServiceId(Long serviceId);

    BarberServiceCompetency save(BarberServiceCompetency competency);

    void deleteByBarberIdAndServiceId(Long barberId, Long serviceId);
}
