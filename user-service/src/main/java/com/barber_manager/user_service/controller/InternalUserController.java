package com.barber_manager.user_service.controller;

import com.barber_manager.user_service.dto.request.CreateUserRequest;
import com.barber_manager.user_service.dto.request.RegisterRequestDto;
import com.barber_manager.user_service.dto.response.UserCredentialDto;
import com.barber_manager.user_service.dto.response.UserResponseDto;
import com.barber_manager.user_service.entity.User;
import com.barber_manager.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {
    private final UserService userService;

    @GetMapping("/credentials/email/{email}")
    public ResponseEntity<UserCredentialDto> getByEmail(
            @PathVariable String email
    ){
        return ResponseEntity.ok(userService.getCredentialsByEmail(email));
    }

    @GetMapping("/credentials/id/{id}")
    public ResponseEntity<UserCredentialDto> getById(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(userService.getCredentialsById(id));
    }

    @PostMapping
    public ResponseEntity<UserCredentialDto> createUser(
            @Valid @RequestBody RegisterRequestDto registerRequestDto
            ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUserFromAuth(registerRequestDto));
    }


}
