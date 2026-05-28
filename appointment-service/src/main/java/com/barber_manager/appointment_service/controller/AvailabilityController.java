package com.barber_manager.appointment_service.controller;

import com.barber_manager.appointment_service.dto.AvailabilityResponse;
import com.barber_manager.appointment_service.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService service;

    @GetMapping
    public ResponseEntity<AvailabilityResponse> availability(
            @RequestParam LocalDate date,
            @RequestParam Long serviceId,
            @RequestParam(required = false) Long barberId,
            @RequestParam(defaultValue = "false") boolean any,
            @RequestParam(required = false) List<Long> barberIds
    ) {
        return ResponseEntity.ok(service.getAvailability(date, serviceId, barberId, any, barberIds));
    }
}

