package com.barber_manager.user_service;

import com.barber_manager.user_service.controller.InternalUserController;
import com.barber_manager.user_service.dto.request.RegisterRequestDto;
import com.barber_manager.user_service.dto.response.UserCredentialDto;
import com.barber_manager.user_service.enums.Role;
import com.barber_manager.user_service.error.GlobalExceptionHandler;
import com.barber_manager.user_service.exceptions.UserAlreadyExistsException;
import com.barber_manager.user_service.exceptions.UserNotFoundException;
import com.barber_manager.user_service.exceptions.UserServiceLogicException;
import com.barber_manager.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalUserController.class)
@Import(GlobalExceptionHandler.class)
class InternalUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnInternalUserByEmail() throws Exception {
        UserCredentialDto userCredentialDto = new UserCredentialDto(
                1L,
                "jan.kowalski@example.com",
                "encoded-password",
                Role.USER
        );
        when(userService.getCredentialsByEmail("jan.kowalski@example.com")).thenReturn(userCredentialDto);
        mockMvc.perform(get("/internal/users/credentials/email/{email}", "jan.kowalski@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jan.kowalski@example.com"))
                .andExpect(jsonPath("$.password").value("encoded-password"))
                .andExpect(jsonPath("$.role").value(Role.USER.toString()))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldReturn404WhenInternalUserNotFoundByEmail() throws Exception {
        when(userService.getCredentialsByEmail("missing@example.com"))
                .thenThrow(new UserNotFoundException("User does not exist with provided email."));

        mockMvc.perform(get("/internal/users/credentials/email/{email}", "missing@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnInternalUserById() throws Exception {
        UserCredentialDto userCredentialDto = new UserCredentialDto(
                1L,
                "jan.kowalski@example.com",
                "encoded-password",
                Role.USER
        );
        when(userService.getCredentialsById(1L)).thenReturn(userCredentialDto);

        mockMvc.perform(get("/internal/users/credentials/id/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jan.kowalski@example.com"))
                .andExpect(jsonPath("$.password").value("encoded-password"))
                .andExpect(jsonPath("$.role").value(Role.USER.toString()))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldReturn404WhenInternalUserNotFoundById() throws Exception {
        when(userService.getCredentialsById(99L))
                .thenThrow(new UserNotFoundException("User does not exist with provided ID."));

        mockMvc.perform(get("/internal/users/credentials/id/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateInternalUser() throws Exception {
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password123",
                "123456789",
                Role.BARBER
        );

        UserCredentialDto userCredentialDto = new UserCredentialDto(
                1L,
                "jan.kowalski@example.com",
                "encoded-password",
                Role.BARBER
        );

        when(userService.createUserFromAuth(registerRequestDto)).thenReturn(userCredentialDto);

        mockMvc.perform(post("/internal/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jan.kowalski@example.com"))
                .andExpect(jsonPath("$.password").value("encoded-password"))
                .andExpect(jsonPath("$.role").value(Role.BARBER.toString()))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldReturn400WhenInternalRegisterValidationFails() throws Exception {
        RegisterRequestDto invalid = new RegisterRequestDto(
                "",
                "Kowalski",
                "not-an-email",
                "short",
                "123",
                Role.BARBER
        );

        mockMvc.perform(post("/internal/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void shouldReturn409WhenInternalUserAlreadyExists() throws Exception {
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password123",
                "123456789",
                Role.BARBER
        );

        when(userService.createUserFromAuth(registerRequestDto))
                .thenThrow(new UserAlreadyExistsException("User already exists with provided email."));

        mockMvc.perform(post("/internal/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400WhenInternalRegisterRoleInvalid() throws Exception {
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password123",
                "123456789",
                Role.USER
        );

        when(userService.createUserFromAuth(registerRequestDto))
                .thenThrow(new UserServiceLogicException("Only barber and administrator accounts can be created."));

        mockMvc.perform(post("/internal/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only barber and administrator accounts can be created."));
    }
}
