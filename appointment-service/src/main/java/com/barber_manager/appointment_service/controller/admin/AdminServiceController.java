package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.entity.Service;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.repository.ServiceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/services")
@RequiredArgsConstructor
public class AdminServiceController {

    private final ServiceRepository serviceRepository;

    @PostMapping
    public ResponseEntity<Service> create(@Valid @RequestBody Service request) {
        request.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceRepository.save(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Service> update(
            @PathVariable Long id,
            @RequestBody Service request
    ) {
        Service existing = serviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service not found."));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getPrice() != null) existing.setPrice(request.getPrice());
        if (request.getSlotCount() != null) existing.setSlotCount(request.getSlotCount());

        return ResponseEntity.ok(serviceRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        serviceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
