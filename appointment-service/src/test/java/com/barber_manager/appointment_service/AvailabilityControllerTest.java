package com.barber_manager.appointment_service;

import com.barber_manager.appointment_service.controller.AvailabilityController;
import com.barber_manager.appointment_service.dto.AvailabilityResponse;
import com.barber_manager.appointment_service.dto.AvailabilitySlotResponse;
import com.barber_manager.appointment_service.error.GlobalExceptionHandler;
import com.barber_manager.appointment_service.exception.BusinessRuleException;
import com.barber_manager.appointment_service.service.AvailabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvailabilityController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailabilityService availabilityService;

    @Test
    void shouldReturnAvailabilityForBarber() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 10);
        AvailabilityResponse response = new AvailabilityResponse(
                date,
                1L,
                10L,
                false,
                List.of(new AvailabilitySlotResponse(
                        LocalDateTime.of(2026, 6, 10, 9, 0),
                        LocalDateTime.of(2026, 6, 10, 9, 30),
                        List.of(10L)
                ))
        );
        when(availabilityService.getAvailability(date, 1L, 10L, false, null)).thenReturn(response);

        mockMvc.perform(get("/availability")
                        .param("date", "2026-06-10")
                        .param("serviceId", "1")
                        .param("barberId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barberId").value(10))
                .andExpect(jsonPath("$.slots[0].startTime").exists());
    }

    @Test
    void shouldReturnAvailabilityForAnyBarber() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 10);
        AvailabilityResponse response = new AvailabilityResponse(date, 1L, null, true, List.of());
        when(availabilityService.getAvailability(date, 1L, null, true, List.of(10L, 11L))).thenReturn(response);

        mockMvc.perform(get("/availability")
                        .param("date", "2026-06-10")
                        .param("serviceId", "1")
                        .param("any", "true")
                        .param("barberIds", "10", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.any").value(true));
    }

    @Test
    void shouldReturn400WhenAvailabilityBusinessRuleFails() throws Exception {
        when(availabilityService.getAvailability(
                eq(LocalDate.of(2026, 6, 10)), eq(1L), isNull(), eq(true), isNull()))
                .thenThrow(new BusinessRuleException("When any=true you must provide barberIds."));

        mockMvc.perform(get("/availability")
                        .param("date", "2026-06-10")
                        .param("serviceId", "1")
                        .param("any", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("When any=true you must provide barberIds."));
    }
}
