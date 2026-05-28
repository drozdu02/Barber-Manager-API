package com.barber_manager.appointment_service.repository;

import com.barber_manager.appointment_service.entity.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {
}

