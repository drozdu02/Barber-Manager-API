package com.barber_manager.appointment_service.service;

import com.barber_manager.appointment_service.dto.ServiceResponse;
import com.barber_manager.appointment_service.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;

    public List<ServiceResponse> list() {
        return serviceRepository.findAll().stream()
                .map(s -> new ServiceResponse(s.getId(), s.getName(), s.getPrice(), s.getSlotCount()))
                .toList();
    }
}
