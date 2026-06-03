package com.barber_manager.user_service;

import com.barber_manager.user_service.controller.BarberController;
import com.barber_manager.user_service.dto.response.BarberResponseDto;
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

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

@WebMvcTest(BarberController.class)
public class BarberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnAllBarbers() throws Exception {
        List<BarberResponseDto> barbers = new ArrayList<>();
        BarberResponseDto barberResponseDto = new BarberResponseDto(
                1L,
                "Jan",
                "Kowalski"
        );
        barbers.add(barberResponseDto);

        when(userService.getBarbers()).thenReturn(barbers);

        mockMvc.perform(get("/api/v1/barbers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("Jan"))
                .andExpect(jsonPath("$[0].lastName").value("Kowalski"));

    }
}
