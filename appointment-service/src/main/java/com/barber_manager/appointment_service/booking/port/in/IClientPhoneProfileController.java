package com.barber_manager.appointment_service.booking.port.in;

import com.barber_manager.appointment_service.dto.admin.BlockPhoneRequest;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneResponse;
import com.barber_manager.appointment_service.dto.admin.ClientPhoneProfileResponse;
import com.barber_manager.appointment_service.entity.Appointment;

public interface IClientPhoneProfileController {

    void registerNoShow(Appointment appointment);

    ClientPhoneProfileResponse getPhoneProfile(String phoneNumber);

    BlockPhoneResponse blockPhone(BlockPhoneRequest request);

    void unblockPhone(String phoneNumber);
}
