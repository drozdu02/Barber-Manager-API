package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.entity.ServiceOffering;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.repository.ServiceOfferingRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/services")
@RequiredArgsConstructor
public class AdminServiceOfferingController {

    private final ServiceOfferingRepository repository;

    @PostMapping
    public ResponseEntity<ServiceOffering> create(@Valid @RequestBody ServiceOffering request) {
        request.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ServiceOffering> update(
            @PathVariable Long id,
            @RequestBody ServiceOffering request
    ) {
        ServiceOffering existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service offering not found."));

        if (request.getName() != null) existing.setName(request.getName());
        if (request.getPrice() != null) existing.setPrice(request.getPrice());
        if (request.getSlotCount() != null) existing.setSlotCount(request.getSlotCount());

        return ResponseEntity.ok(repository.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

