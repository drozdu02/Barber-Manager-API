package com.barber_manager.appointment_service.repository;

import com.barber_manager.appointment_service.entity.BlockedPhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockedPhoneNumberRepository extends JpaRepository<BlockedPhoneNumber, Long> {
    Optional<BlockedPhoneNumber> findByPhoneNumber(String phoneNumber);
    Optional<BlockedPhoneNumber> findByPhoneNumberAndActiveTrue(String phoneNumber);
}

