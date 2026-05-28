package com.barber_manager.appointment_service.service;

import com.barber_manager.appointment_service.dto.ServiceOfferingResponse;
import com.barber_manager.appointment_service.repository.ServiceOfferingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceOfferingService {

    private final ServiceOfferingRepository repository;

    public List<ServiceOfferingResponse> list() {
        return repository.findAll().stream()
                .map(s -> new ServiceOfferingResponse(s.getId(), s.getName(), s.getPrice(), s.getSlotCount()))
                .toList();
    }
}

