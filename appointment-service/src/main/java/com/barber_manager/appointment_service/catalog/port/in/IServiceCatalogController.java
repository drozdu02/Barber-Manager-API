package com.barber_manager.appointment_service.catalog.port.in;

import com.barber_manager.appointment_service.dto.ServiceResponse;
import com.barber_manager.appointment_service.entity.Service;

import java.util.List;

public interface IServiceCatalogController {

    List<ServiceResponse> list();

    Service create(Service request);

    Service update(Long id, Service request);

    void delete(Long id);
}
