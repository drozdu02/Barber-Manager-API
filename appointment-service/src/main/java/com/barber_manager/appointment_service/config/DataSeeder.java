package com.barber_manager.appointment_service.config;

import com.barber_manager.appointment_service.catalog.port.out.IServiceCatalogRepository;
import com.barber_manager.appointment_service.entity.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final IServiceCatalogRepository serviceCatalogRepository;

    @Bean
    public CommandLineRunner seedServices() {
        return args -> {
            if (serviceCatalogRepository.count() > 0) {
                return;
            }

            serviceCatalogRepository.save(new Service(null, "Haircut", new BigDecimal("60.00"), 2));
            serviceCatalogRepository.save(new Service(null, "Beard trim", new BigDecimal("40.00"), 1));
            serviceCatalogRepository.save(new Service(null, "Haircut + Beard", new BigDecimal("90.00"), 3));
        };
    }
}
