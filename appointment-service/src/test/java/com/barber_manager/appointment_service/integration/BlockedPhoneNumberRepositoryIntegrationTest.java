package com.barber_manager.appointment_service.integration;

import com.barber_manager.appointment_service.entity.BlockedPhoneNumber;
import com.barber_manager.appointment_service.repository.BlockedPhoneNumberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("integration")
@DisplayName("Integracja: BlockedPhoneNumberRepository (adapter JPA)")
class BlockedPhoneNumberRepositoryIntegrationTest {

    @Autowired
    private BlockedPhoneNumberRepository blockedPhoneNumberRepository;

    @Test
    void shouldPersistAndFindActiveBlockByPhoneNumber() {
        BlockedPhoneNumber block = new BlockedPhoneNumber();
        block.setPhoneNumber("123456789");
        block.setReason("No-show policy");
        block.setActive(true);
        blockedPhoneNumberRepository.save(block);

        BlockedPhoneNumber found = blockedPhoneNumberRepository
                .findByPhoneNumberAndActiveTrue("123456789")
                .orElseThrow();

        assertEquals("No-show policy", found.getReason());
        assertTrue(found.isActive());
    }

    @Test
    void shouldNotReturnInactiveBlockAsActive() {
        BlockedPhoneNumber block = new BlockedPhoneNumber();
        block.setPhoneNumber("987654321");
        block.setReason("Old block");
        block.setActive(false);
        blockedPhoneNumberRepository.save(block);

        assertTrue(blockedPhoneNumberRepository.findByPhoneNumberAndActiveTrue("987654321").isEmpty());
        assertTrue(blockedPhoneNumberRepository.findByPhoneNumber("987654321").isPresent());
    }
}
