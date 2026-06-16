package com.barber_manager.user_service.config;

import com.barber_manager.user_service.dto.request.RegisterRequestDto;
import com.barber_manager.user_service.enums.Role;
import com.barber_manager.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final UserService userService;
    private final UserSeedProperties seedProperties;

    @Bean
    public CommandLineRunner seedUsers() {
        return args -> {
            if (!seedProperties.isEnabled()) {
                return;
            }

            seedUser(
                    seedProperties.getAdminEmail(),
                    seedProperties.getAdminPassword(),
                    seedProperties.getAdminFirstName(),
                    seedProperties.getAdminLastName(),
                    seedProperties.getAdminPhoneNumber(),
                    Role.ADMIN
            );

            seedUser(
                    seedProperties.getBarberEmail(),
                    seedProperties.getBarberPassword(),
                    seedProperties.getBarberFirstName(),
                    seedProperties.getBarberLastName(),
                    seedProperties.getBarberPhoneNumber(),
                    Role.BARBER
            );
        };
    }

    private void seedUser(
            String email,
            String password,
            String firstName,
            String lastName,
            String phoneNumber,
            Role role
    ) {
        userService.seedUserIfAbsent(new RegisterRequestDto(
                firstName,
                lastName,
                email,
                password,
                phoneNumber,
                role
        ));
        log.info("Ensured {} account exists: email={}", role.name().toLowerCase(), email);
    }
}
