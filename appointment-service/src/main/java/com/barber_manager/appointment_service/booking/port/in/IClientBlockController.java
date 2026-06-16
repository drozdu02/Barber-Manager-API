package com.barber_manager.appointment_service.booking.port.in;

import com.barber_manager.appointment_service.dto.admin.BlockPhoneRequest;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneResponse;

public interface IClientBlockController {

    BlockPhoneResponse blockPhone(BlockPhoneRequest request);

    void unblockPhone(String phoneNumber);
}
