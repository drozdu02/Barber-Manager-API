package com.barber_manager.appointment_service.booking.port.out;

import com.barber_manager.appointment_service.entity.BlockedPhoneNumber;

import java.util.Optional;

public interface IBlockedPhoneNumberRepository {

    BlockedPhoneNumber save(BlockedPhoneNumber block);

    Optional<BlockedPhoneNumber> findByPhoneNumberAndActiveTrue(String phoneNumber);

    Optional<BlockedPhoneNumber> findByPhoneNumber(String phoneNumber);
}
