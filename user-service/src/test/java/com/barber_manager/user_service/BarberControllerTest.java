package com.barber_manager.user_service;

import com.barber_manager.user_service.controller.BarberController;
import com.barber_manager.user_service.dto.response.BarberResponseDto;
import com.barber_manager.user_service.error.GlobalExceptionHandler;
import com.barber_manager.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BarberController.class)
@Import(GlobalExceptionHandler.class)
class BarberControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    void shouldReturnEmptyBarberList() throws Exception {
        when(userService.getBarbers()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/barbers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
