package com.barber_manager.appointment_service.controller;

import com.barber_manager.appointment_service.dto.AppointmentResponse;
import com.barber_manager.appointment_service.dto.CreateAppointmentRequest;
import com.barber_manager.appointment_service.dto.StaffAppointmentResponse;
import com.barber_manager.appointment_service.dto.UpdateAppointmentStatusRequest;
import com.barber_manager.appointment_service.enums.AppointmentStatus;
import com.barber_manager.appointment_service.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService service;

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(
            @Valid @RequestBody CreateAppointmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PostMapping("/cancel/{bookingToken}")
    public ResponseEntity<Void> cancel(
            @PathVariable String bookingToken
    ) {
        service.cancelByBookingToken(bookingToken);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffAppointmentResponse> details(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getStaffDetails(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<StaffAppointmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        AppointmentStatus status = AppointmentStatus.valueOf(request.status());
        return ResponseEntity.ok(service.updateStatus(id, status));
    }
}

