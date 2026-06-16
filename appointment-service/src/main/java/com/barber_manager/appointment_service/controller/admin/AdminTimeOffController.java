package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.dto.admin.CreateTimeOffRequest;
import com.barber_manager.appointment_service.dto.admin.TimeOffResponse;
import com.barber_manager.appointment_service.schedule.port.in.IWorkScheduleController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/time-off")
@RequiredArgsConstructor
public class AdminTimeOffController {

    private final IWorkScheduleController workScheduleController;

    @GetMapping
    public ResponseEntity<List<TimeOffResponse>> list(@RequestParam Long barberId) {
        return ResponseEntity.ok(workScheduleController.listTimeOff(barberId));
    }

    @PostMapping
    public ResponseEntity<TimeOffResponse> create(
            @Valid @RequestBody CreateTimeOffRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workScheduleController.createTimeOff(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workScheduleController.deleteTimeOff(id);
        return ResponseEntity.noContent().build();
    }
}
