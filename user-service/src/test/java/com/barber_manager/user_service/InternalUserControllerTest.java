package com.barber_manager.user_service;

import com.barber_manager.user_service.controller.InternalUserController;
import com.barber_manager.user_service.dto.request.RegisterRequestDto;
import com.barber_manager.user_service.dto.response.UserCredentialDto;
import com.barber_manager.user_service.enums.Role;
import com.barber_manager.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalUserController.class)
public class InternalUserControllerTest {

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
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCredentialDto))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jan.kowalski@example.com"))
                .andExpect(jsonPath("$.password").value("encoded-password"))
                .andExpect(jsonPath("$.role").value(Role.USER.toString()))
                .andExpect(jsonPath("$.id").value(1L));

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
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCredentialDto)
                ))
                .andExpect(jsonPath("$.email").value("jan.kowalski@example.com"))
                .andExpect(jsonPath("$.password").value("encoded-password"))
                .andExpect(jsonPath("$.role").value(Role.USER.toString()))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldCreateInternalUser() throws Exception {
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "encoded-password",
                "123456789",
                Role.USER
        );

        UserCredentialDto userCredentialDto = new UserCredentialDto(
                1L,
                "jan.kowalski@example.com",
                "encoded-password",
                Role.USER
        );

        when(userService.createUserFromAuth(registerRequestDto)).thenReturn(userCredentialDto);

        mockMvc.perform(post("/internal/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jan.kowalski@example.com"))
                .andExpect(jsonPath("$.password").value("encoded-password"))
                .andExpect(jsonPath("$.role").value(Role.USER.toString()))
                .andExpect(jsonPath("$.id").value(1L));

    }
}
