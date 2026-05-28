package com.barber_manager.appointment_service.controller;

import com.barber_manager.appointment_service.dto.ServiceOfferingResponse;
import com.barber_manager.appointment_service.service.ServiceOfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceOfferingController {

    private final ServiceOfferingService service;

    @GetMapping
    public ResponseEntity<List<ServiceOfferingResponse>> list() {
        return ResponseEntity.ok(service.list());
    }
}

