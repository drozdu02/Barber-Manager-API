package com.barber_manager.user_service.controller;

import com.barber_manager.user_service.dto.response.BarberResponseDto;
import com.barber_manager.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/barbers")
@RequiredArgsConstructor
public class BarberController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<BarberResponseDto>> getBarbers() {
        return ResponseEntity.ok(userService.getBarbers());
    }
}
