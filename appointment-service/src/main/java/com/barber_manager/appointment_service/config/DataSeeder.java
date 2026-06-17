package com.barber_manager.appointment_service.config;

import com.barber_manager.appointment_service.entity.Service;
import com.barber_manager.appointment_service.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final ServiceRepository serviceRepository;

    @Bean
    public CommandLineRunner seedServices() {
        return args -> {
            if (serviceRepository.count() > 0) {
                return;
            }

            serviceRepository.save(new Service(null, "Haircut", new BigDecimal("60.00"), 2));
            serviceRepository.save(new Service(null, "Beard trim", new BigDecimal("40.00"), 1));
            serviceRepository.save(new Service(null, "Haircut + Beard", new BigDecimal("90.00"), 3));
        };
    }
}
