package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.booking.port.in.IClientBlockController;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneRequest;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/blocked-phones")
@RequiredArgsConstructor
public class AdminBlockedPhoneController {

    private final IClientBlockController clientBlockController;

    @PostMapping
    public ResponseEntity<BlockPhoneResponse> block(@Valid @RequestBody BlockPhoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientBlockController.blockPhone(request));
    }

    @DeleteMapping("/{phoneNumber}")
    public ResponseEntity<Void> unblock(@PathVariable String phoneNumber) {
        clientBlockController.unblockPhone(phoneNumber);
        return ResponseEntity.noContent().build();
    }
}
