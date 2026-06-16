package com.barber_manager.user_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "user.seed")
public class UserSeedProperties {

    private boolean enabled = true;

    private String adminEmail = "admin@barber.local";
    private String adminPassword = "Admin1234";
    private String adminFirstName = "Jan";
    private String adminLastName = "Admin";
    private String adminPhoneNumber = "111111111";

    private String barberEmail = "barber@barber.local";
    private String barberPassword = "Barber1234";
    private String barberFirstName = "Adam";
    private String barberLastName = "Kowalski";
    private String barberPhoneNumber = "222222222";
}
