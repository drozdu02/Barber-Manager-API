package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.booking.port.in.IClientPhoneProfileController;
import com.barber_manager.appointment_service.dto.admin.ClientPhoneProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/phone-profiles")
@RequiredArgsConstructor
public class ClientPhoneProfileController {

    private final IClientPhoneProfileController clientPhoneProfileController;

    @GetMapping("/{phoneNumber}")
    public ResponseEntity<ClientPhoneProfileResponse> getProfile(
            @PathVariable String phoneNumber
    ) {
        return ResponseEntity.ok(clientPhoneProfileController.getPhoneProfile(phoneNumber));
    }
}
