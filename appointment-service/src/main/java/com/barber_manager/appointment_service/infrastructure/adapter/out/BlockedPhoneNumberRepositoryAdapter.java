package com.barber_manager.appointment_service.infrastructure.adapter.out;

import com.barber_manager.appointment_service.booking.port.out.IBlockedPhoneNumberRepository;
import com.barber_manager.appointment_service.entity.BlockedPhoneNumber;
import com.barber_manager.appointment_service.repository.BlockedPhoneNumberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BlockedPhoneNumberRepositoryAdapter implements IBlockedPhoneNumberRepository {

    private final BlockedPhoneNumberRepository blockedPhoneNumberRepository;

    @Override
    public BlockedPhoneNumber save(BlockedPhoneNumber block) {
        return blockedPhoneNumberRepository.save(block);
    }

    @Override
    public Optional<BlockedPhoneNumber> findByPhoneNumberAndActiveTrue(String phoneNumber) {
        return blockedPhoneNumberRepository.findByPhoneNumberAndActiveTrue(phoneNumber);
    }

    @Override
    public Optional<BlockedPhoneNumber> findByPhoneNumber(String phoneNumber) {
        return blockedPhoneNumberRepository.findByPhoneNumber(phoneNumber);
    }
}
