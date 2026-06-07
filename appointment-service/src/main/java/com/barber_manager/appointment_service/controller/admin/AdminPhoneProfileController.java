package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.dto.admin.ClientPhoneProfileResponse;
import com.barber_manager.appointment_service.service.NoShowRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/phone-profiles")
@RequiredArgsConstructor
public class AdminPhoneProfileController {

    private final NoShowRegistrationService noShowRegistrationService;

    @GetMapping("/{phoneNumber}")
    public ResponseEntity<ClientPhoneProfileResponse> getProfile(
            @PathVariable String phoneNumber
    ) {
        return ResponseEntity.ok(noShowRegistrationService.getPhoneProfile(phoneNumber));
    }
}
