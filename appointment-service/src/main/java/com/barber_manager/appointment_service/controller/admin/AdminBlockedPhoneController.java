package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.dto.admin.BlockPhoneRequest;
import com.barber_manager.appointment_service.dto.admin.BlockPhoneResponse;
import com.barber_manager.appointment_service.service.ClientBlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/blocked-phones")
@RequiredArgsConstructor
public class AdminBlockedPhoneController {

    private final ClientBlockService clientBlockService;

    @PostMapping
    public ResponseEntity<BlockPhoneResponse> block(@Valid @RequestBody BlockPhoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientBlockService.blockPhone(request));
    }

    @DeleteMapping("/{phoneNumber}")
    public ResponseEntity<Void> unblock(@PathVariable String phoneNumber) {
        clientBlockService.unblockPhone(phoneNumber);
        return ResponseEntity.noContent().build();
    }
}
