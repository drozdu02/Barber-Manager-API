package com.barber_manager.appointment_service.infrastructure.adapter.out;

import com.barber_manager.appointment_service.booking.port.out.IClientPhoneProfileRepository;
import com.barber_manager.appointment_service.entity.ClientPhoneProfile;
import com.barber_manager.appointment_service.repository.ClientPhoneProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClientPhoneProfileRepositoryAdapter implements IClientPhoneProfileRepository {

    private final ClientPhoneProfileRepository clientPhoneProfileRepository;

    @Override
    public ClientPhoneProfile save(ClientPhoneProfile profile) {
        return clientPhoneProfileRepository.save(profile);
    }

    @Override
    public Optional<ClientPhoneProfile> findByPhoneNumber(String phoneNumber) {
        return clientPhoneProfileRepository.findByPhoneNumber(phoneNumber);
    }
}
