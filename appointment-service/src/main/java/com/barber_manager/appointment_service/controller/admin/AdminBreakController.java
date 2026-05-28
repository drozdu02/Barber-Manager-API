package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.dto.admin.CreateBreakRequest;
import com.barber_manager.appointment_service.entity.BarberBreak;
import com.barber_manager.appointment_service.exception.BusinessRuleException;
import com.barber_manager.appointment_service.repository.BarberBreakRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/breaks")
@RequiredArgsConstructor
public class AdminBreakController {

    private final BarberBreakRepository repository;

    @PostMapping
    public ResponseEntity<BarberBreak> create(@Valid @RequestBody CreateBreakRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessRuleException("endTime must be after startTime.");
        }
        BarberBreak b = new BarberBreak(null, request.barberId(), request.startTime(), request.endTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(b));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

