package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.dto.admin.CreateWorkScheduleRequest;
import com.barber_manager.appointment_service.dto.admin.ReplaceWorkScheduleRequest;
import com.barber_manager.appointment_service.dto.admin.UpdateWorkScheduleRequest;
import com.barber_manager.appointment_service.dto.admin.WorkScheduleResponse;
import com.barber_manager.appointment_service.service.WorkScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/work-schedules")
@RequiredArgsConstructor
public class AdminWorkScheduleController {

    private final WorkScheduleService workScheduleService;

    @GetMapping
    public ResponseEntity<List<WorkScheduleResponse>> list(@RequestParam Long barberId) {
        return ResponseEntity.ok(workScheduleService.listWorkSchedules(barberId));
    }

    @PostMapping
    public ResponseEntity<WorkScheduleResponse> create(
            @Valid @RequestBody CreateWorkScheduleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workScheduleService.createWorkSchedule(request));
    }

    @PutMapping
    public ResponseEntity<List<WorkScheduleResponse>> replace(
            @Valid @RequestBody ReplaceWorkScheduleRequest request
    ) {
        return ResponseEntity.ok(workScheduleService.replaceWorkSchedules(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkScheduleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkScheduleRequest request
    ) {
        return ResponseEntity.ok(workScheduleService.updateWorkSchedule(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workScheduleService.deleteWorkSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
