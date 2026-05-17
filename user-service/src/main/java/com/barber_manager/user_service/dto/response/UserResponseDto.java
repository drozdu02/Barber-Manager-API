package com.barber_manager.user_service.dto.response;


import java.time.LocalDateTime;

public record UserResponseDto(
    Long id,
    String email,
    String firstName,
    String lastName
) {

}
