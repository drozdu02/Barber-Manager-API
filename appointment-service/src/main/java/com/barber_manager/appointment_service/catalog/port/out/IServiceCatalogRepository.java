package com.barber_manager.appointment_service.catalog.port.out;

import com.barber_manager.appointment_service.entity.Service;

import java.util.List;
import java.util.Optional;

public interface IServiceCatalogRepository {

    List<Service> findAll();

    Optional<Service> findById(Long id);

    Service save(Service service);

    void deleteById(Long id);

    long count();
}
