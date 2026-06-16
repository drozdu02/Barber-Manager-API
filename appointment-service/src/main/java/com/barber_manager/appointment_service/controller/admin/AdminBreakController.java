package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.dto.admin.CreateBreakRequest;
import com.barber_manager.appointment_service.entity.BarberBreak;
import com.barber_manager.appointment_service.schedule.port.in.IWorkScheduleController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/breaks")
@RequiredArgsConstructor
public class AdminBreakController {

    private final IWorkScheduleController workScheduleController;

    @PostMapping
    public ResponseEntity<BarberBreak> create(@Valid @RequestBody CreateBreakRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workScheduleController.createBreak(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workScheduleController.deleteBreak(id);
        return ResponseEntity.noContent().build();
    }
}
