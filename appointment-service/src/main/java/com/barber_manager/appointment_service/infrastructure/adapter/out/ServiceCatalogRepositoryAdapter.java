package com.barber_manager.appointment_service.infrastructure.adapter.out;

import com.barber_manager.appointment_service.catalog.port.out.IServiceCatalogRepository;
import com.barber_manager.appointment_service.entity.Service;
import com.barber_manager.appointment_service.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ServiceCatalogRepositoryAdapter implements IServiceCatalogRepository {

    private final ServiceRepository serviceRepository;

    @Override
    public List<Service> findAll() {
        return serviceRepository.findAll();
    }

    @Override
    public Optional<Service> findById(Long id) {
        return serviceRepository.findById(id);
    }

    @Override
    public Service save(Service service) {
        return serviceRepository.save(service);
    }

    @Override
    public void deleteById(Long id) {
        serviceRepository.deleteById(id);
    }

    @Override
    public long count() {
        return serviceRepository.count();
    }
}
