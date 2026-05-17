package com.barber_manager.user_service.dto.internal;

import com.barber_manager.user_service.enums.Role;

public record UserInternalDto(
    Long id,
    String email,
    Role role
) {
}
