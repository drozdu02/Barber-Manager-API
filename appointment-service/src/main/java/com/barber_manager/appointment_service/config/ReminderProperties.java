package com.barber_manager.appointment_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "appointment.reminder")
public class ReminderProperties {

    private boolean enabled = true;

    private int hoursBefore = 24;

    private long fixedDelayMs = 900_000;
}
