package com.barber_manager.appointment_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "appointment.client-block")
public class ClientBlockProperties {

    /**
     * Number of registered no-shows that triggers an automatic client block.
     */
    private int noShowThreshold = 3;
}
