package com.barber_manager.appointment_service.controller;

import com.barber_manager.appointment_service.booking.port.in.IAppointmentController;
import com.barber_manager.appointment_service.dto.StaffAppointmentResponse;
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

    private final IAppointmentController appointmentController;

    @GetMapping
    public ResponseEntity<List<StaffAppointmentResponse>> calendar(
            @RequestParam Long barberId,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to
    ) {
        return ResponseEntity.ok(appointmentController.getBarberCalendar(barberId, from, to));
    }
}
