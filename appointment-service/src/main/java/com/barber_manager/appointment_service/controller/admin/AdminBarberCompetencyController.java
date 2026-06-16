package com.barber_manager.appointment_service.controller.admin;

import com.barber_manager.appointment_service.catalog.port.out.IServiceCatalogRepository;
import com.barber_manager.appointment_service.dto.admin.AssignBarberCompetencyRequest;
import com.barber_manager.appointment_service.entity.BarberServiceCompetency;
import com.barber_manager.appointment_service.exception.BusinessRuleException;
import com.barber_manager.appointment_service.schedule.port.out.IBarberServiceCompetencyRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/barber-competencies")
@RequiredArgsConstructor
public class AdminBarberCompetencyController {

    private final IBarberServiceCompetencyRepository competencyRepository;
    private final IServiceCatalogRepository serviceCatalogRepository;

    @PostMapping
    public ResponseEntity<BarberServiceCompetency> assign(
            @Valid @RequestBody AssignBarberCompetencyRequest request
    ) {
        serviceCatalogRepository.findById(request.serviceId())
                .orElseThrow(() -> new BusinessRuleException("Service not found."));

        if (competencyRepository.existsByBarberIdAndServiceId(request.barberId(), request.serviceId())) {
            throw new BusinessRuleException("Competency already assigned.");
        }

        BarberServiceCompetency competency = new BarberServiceCompetency(
                null,
                request.barberId(),
                request.serviceId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(competencyRepository.save(competency));
    }

    @DeleteMapping
    public ResponseEntity<Void> remove(
            @RequestParam Long barberId,
            @RequestParam Long serviceId
    ) {
        competencyRepository.deleteByBarberIdAndServiceId(barberId, serviceId);
        return ResponseEntity.noContent().build();
    }
}
