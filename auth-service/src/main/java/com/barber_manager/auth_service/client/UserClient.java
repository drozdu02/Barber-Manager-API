package com.barber_manager.auth_service.client;

import com.barber_manager.auth_service.dto.request.RegisterRequestDto;
import com.barber_manager.auth_service.dto.response.UserCredentialDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", path = "/internal/users")
public interface UserClient {

    @GetMapping("/credentials/id/{id}")
    UserCredentialDto getCredentialsById(
            @PathVariable Long id
    );

    @GetMapping("/credentials/email/{email}")
    UserCredentialDto getCredentialsByEmail(
            @PathVariable String email
    );

    @PostMapping
    UserCredentialDto createUser(
            @RequestBody RegisterRequestDto registerRequestDto
    );

}
