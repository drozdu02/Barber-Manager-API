package com.barber_manager.appointment_service.repository;

import com.barber_manager.appointment_service.entity.BarberServiceCompetency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BarberServiceCompetencyRepository extends JpaRepository<BarberServiceCompetency, Long> {

    boolean existsByServiceId(Long serviceId);

    boolean existsByBarberIdAndServiceId(Long barberId, Long serviceId);

    void deleteByBarberIdAndServiceId(Long barberId, Long serviceId);

    @Query("SELECT c.barberId FROM BarberServiceCompetency c WHERE c.serviceId = :serviceId")
    List<Long> findBarberIdsByServiceId(@Param("serviceId") Long serviceId);
}
