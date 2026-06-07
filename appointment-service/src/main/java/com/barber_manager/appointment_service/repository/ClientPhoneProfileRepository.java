package com.barber_manager.appointment_service.repository;

import com.barber_manager.appointment_service.entity.ClientPhoneProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientPhoneProfileRepository extends JpaRepository<ClientPhoneProfile, Long> {

    Optional<ClientPhoneProfile> findByPhoneNumber(String phoneNumber);
}
