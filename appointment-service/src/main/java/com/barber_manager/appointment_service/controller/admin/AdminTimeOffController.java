package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.dto.admin.CreateTimeOffRequest;
import com.barber_manager.appointment_service.dto.admin.TimeOffResponse;
import com.barber_manager.appointment_service.service.WorkScheduleService;
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

    private final WorkScheduleService workScheduleService;

    @GetMapping
    public ResponseEntity<List<TimeOffResponse>> list(@RequestParam Long barberId) {
        return ResponseEntity.ok(workScheduleService.listTimeOff(barberId));
    }

    @PostMapping
    public ResponseEntity<TimeOffResponse> create(
            @Valid @RequestBody CreateTimeOffRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workScheduleService.createTimeOff(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workScheduleService.deleteTimeOff(id);
        return ResponseEntity.noContent().build();
    }
}
