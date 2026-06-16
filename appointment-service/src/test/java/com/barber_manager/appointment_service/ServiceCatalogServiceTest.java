package com.barber_manager.appointment_service;

import com.barber_manager.appointment_service.catalog.port.out.IServiceCatalogRepository;
import com.barber_manager.appointment_service.dto.ServiceResponse;
import com.barber_manager.appointment_service.entity.Service;
import com.barber_manager.appointment_service.service.ServiceCatalogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceCatalogServiceTest {

    @InjectMocks
    private ServiceCatalogService serviceCatalogService;

    @Mock
    private IServiceCatalogRepository serviceCatalogRepository;

    @Test
    void shouldListServices() {
        Service haircut = new Service(1L, "Haircut", new BigDecimal("60.00"), 2);
        when(serviceCatalogRepository.findAll()).thenReturn(List.of(haircut));

        List<ServiceResponse> services = serviceCatalogService.list();

        assertEquals(1, services.size());
        assertEquals("Haircut", services.getFirst().name());
        assertEquals(new BigDecimal("60.00"), services.getFirst().price());
        assertEquals(2, services.getFirst().slotCount());
    }
}
