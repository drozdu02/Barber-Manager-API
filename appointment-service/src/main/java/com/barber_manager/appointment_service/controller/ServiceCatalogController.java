package com.barber_manager.appointment_service.controller;

import com.barber_manager.appointment_service.catalog.port.in.IServiceCatalogController;
import com.barber_manager.appointment_service.dto.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceCatalogController {

    private final IServiceCatalogController serviceCatalogController;

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> list() {
        return ResponseEntity.ok(serviceCatalogController.list());
    }
}
