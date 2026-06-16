package com.barber_manager.appointment_service.service;

import com.barber_manager.appointment_service.catalog.port.in.IServiceCatalogController;
import com.barber_manager.appointment_service.catalog.port.out.IServiceCatalogRepository;
import com.barber_manager.appointment_service.dto.ServiceResponse;
import com.barber_manager.appointment_service.entity.Service;
import com.barber_manager.appointment_service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceCatalogService implements IServiceCatalogController {

    private final IServiceCatalogRepository serviceCatalogRepository;

    @Override
    public List<ServiceResponse> list() {
        return serviceCatalogRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Service create(Service request) {
        request.setId(null);
        return serviceCatalogRepository.save(request);
    }

    @Override
    public Service update(Long id, Service request) {
        Service existing = serviceCatalogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service not found."));

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getPrice() != null) {
            existing.setPrice(request.getPrice());
        }
        if (request.getSlotCount() != null) {
            existing.setSlotCount(request.getSlotCount());
        }

        return serviceCatalogRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        serviceCatalogRepository.deleteById(id);
    }

    private ServiceResponse toResponse(Service service) {
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getPrice(),
                service.getSlotCount()
        );
    }
}
