package com.barber_manager.appointment_service.booking.port.out;

import com.barber_manager.appointment_service.entity.ClientPhoneProfile;

import java.util.Optional;

public interface IClientPhoneProfileRepository {

    ClientPhoneProfile save(ClientPhoneProfile profile);

    Optional<ClientPhoneProfile> findByPhoneNumber(String phoneNumber);
}
