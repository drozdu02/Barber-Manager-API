package com.barber_manager.appointment_service.repository;

import com.barber_manager.appointment_service.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}
