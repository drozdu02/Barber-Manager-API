package com.barber_manager.appointment_service.controller;

import com.barber_manager.appointment_service.schedule.port.in.IAvailabilityController;
import com.barber_manager.appointment_service.dto.AvailabilityResponse;
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

    private final IAvailabilityController availabilityController;

    @GetMapping
    public ResponseEntity<AvailabilityResponse> availability(
            @RequestParam LocalDate date,
            @RequestParam Long serviceId,
            @RequestParam(required = false) Long barberId,
            @RequestParam(defaultValue = "false") boolean anyAvailable,
            @RequestParam(required = false) List<Long> anyAvailableBarberIds
    ) {
        return ResponseEntity.ok(
                availabilityController.getAvailability(date, serviceId, barberId, anyAvailable, anyAvailableBarberIds)
        );
    }
}
