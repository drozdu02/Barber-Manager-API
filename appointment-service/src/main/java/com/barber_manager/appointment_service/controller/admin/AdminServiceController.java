package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.catalog.port.in.IServiceCatalogController;
import com.barber_manager.appointment_service.entity.Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/services")
@RequiredArgsConstructor
public class AdminServiceController {

    private final IServiceCatalogController serviceCatalogController;

    @PostMapping
    public ResponseEntity<Service> create(@Valid @RequestBody Service request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceCatalogController.create(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Service> update(
            @PathVariable Long id,
            @RequestBody Service request
    ) {
        return ResponseEntity.ok(serviceCatalogController.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        serviceCatalogController.delete(id);
        return ResponseEntity.noContent().build();
    }
}
