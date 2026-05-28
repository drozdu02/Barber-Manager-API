package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.dto.admin.BlockPhoneRequest;
import com.barber_manager.appointment_service.entity.BlockedPhoneNumber;
import com.barber_manager.appointment_service.repository.BlockedPhoneNumberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/blocked-phones")
@RequiredArgsConstructor
public class AdminBlockedPhoneController {

    private final BlockedPhoneNumberRepository repository;

    @PostMapping
    public ResponseEntity<BlockedPhoneNumber> block(@Valid @RequestBody BlockPhoneRequest request) {
        BlockedPhoneNumber b = new BlockedPhoneNumber();
        b.setPhoneNumber(request.phoneNumber());
        b.setReason(request.reason());
        b.setActive(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(b));
    }

    @DeleteMapping("/{phoneNumber}")
    public ResponseEntity<Void> unblock(@PathVariable String phoneNumber) {
        repository.findByPhoneNumberAndActiveTrue(phoneNumber).ifPresent(b -> {
            b.setActive(false);
            repository.save(b);
        });
        return ResponseEntity.noContent().build();
    }
}

