package com.barber_manager.appointment_service.controller;

import com.barber_manager.appointment_service.dto.AppointmentResponse;
import com.barber_manager.appointment_service.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/barber/calendar")
@RequiredArgsConstructor
public class BarberCalendarController {

    private final AppointmentService service;

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> calendar(
            @RequestParam Long barberId,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to
    ) {
        return ResponseEntity.ok(service.getBarberCalendar(barberId, from, to));
    }
}

