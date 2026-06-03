package com.barber_manager.user_service;

import com.barber_manager.user_service.controller.UserController;
import com.barber_manager.user_service.dto.request.CreateUserRequest;
import com.barber_manager.user_service.dto.request.UpdateUserRequest;
import com.barber_manager.user_service.dto.response.UserResponseDto;
import com.barber_manager.user_service.entity.User;
import com.barber_manager.user_service.enums.Role;
import com.barber_manager.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnUserById() throws Exception {
        UserResponseDto user = new UserResponseDto(
                1L,
                "jan.kowalski@example.com",
                "Jan",
                "Kowalski"
        );

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.email").value("jan.kowalski@example.com"));
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        List<UserResponseDto> users = new ArrayList<>();
        UserResponseDto user1 = new UserResponseDto(
                1L,
                "jan.kowalski@example.com",
                "Jan",
                "Kowalski"
        );
        users.add(user1);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("Jan"))
                .andExpect(jsonPath("$[0].lastName").value("Kowalski"))
                .andExpect(jsonPath("$[0].email").value("jan.kowalski@example.com"));

    }
    @Test
    void shouldCreateUser() throws Exception {
        CreateUserRequest user = new CreateUserRequest(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "encoded-password",
                "123456789"
        );
        UserResponseDto userResponseDto = new UserResponseDto(
                1L,
                "jan.kowalski@example.com",
                "Jan",
                "Kowalski"
        );
        when(userService.createUser(user)).thenReturn(userResponseDto);
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.email").value("jan.kowalski@example.com"));

    }

    @Test
    void shouldUpdateUser() throws Exception {
        UpdateUserRequest user = new UpdateUserRequest(
                "Jan",
                "Kowalski",
                "123456789"
        );
        UserResponseDto userResponseDto = new UserResponseDto(
                1L,
                "jan.kowalski@example.com",
                "Jan",
                "Kowalski"
        );
        when(userService.updateUser(1L, user)).thenReturn(userResponseDto);

        mockMvc.perform(patch("/api/v1/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.email").value("jan.kowalski@example.com"));
    }
}
