package com.barber_manager.appointment_service.dto;

import java.math.BigDecimal;

public record ServiceOfferingResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer slotCount
) {
}

