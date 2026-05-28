package com.barber_manager.appointment_service.config;

import com.barber_manager.appointment_service.entity.ServiceOffering;
import com.barber_manager.appointment_service.repository.ServiceOfferingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final ServiceOfferingRepository serviceOfferingRepository;

    @Bean
    public CommandLineRunner seedServices() {
        return args -> {
            if (serviceOfferingRepository.count() > 0) return;

            serviceOfferingRepository.save(new ServiceOffering(null, "Haircut", new BigDecimal("60.00"), 2));
            serviceOfferingRepository.save(new ServiceOffering(null, "Beard trim", new BigDecimal("40.00"), 1));
            serviceOfferingRepository.save(new ServiceOffering(null, "Haircut + Beard", new BigDecimal("90.00"), 3));
        };
    }
}

