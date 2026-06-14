package com.barber_manager.appointment_service;

import com.barber_manager.appointment_service.controller.AppointmentController;
import com.barber_manager.appointment_service.dto.AppointmentResponse;
import com.barber_manager.appointment_service.dto.CreateAppointmentRequest;
import com.barber_manager.appointment_service.dto.StaffAppointmentResponse;
import com.barber_manager.appointment_service.dto.UpdateAppointmentStatusRequest;
import com.barber_manager.appointment_service.error.GlobalExceptionHandler;
import com.barber_manager.appointment_service.exception.BusinessRuleException;
import com.barber_manager.appointment_service.exception.NotFoundException;
import com.barber_manager.appointment_service.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    @Test
    void shouldCreateAppointment() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 6, 15, 10, 0);
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                "Jan",
                "Kowalski",
                "123456789",
                "jan@example.com",
                1L,
                start,
                10L,
                null,
                null
        );
        AppointmentResponse response = new AppointmentResponse(
                1L, 1L, 10L, start, start.plusMinutes(60), "TOKEN123", false, "BOOKED"
        );
        when(appointmentService.create(request)).thenReturn(response);

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingToken").value("TOKEN123"))
                .andExpect(jsonPath("$.status").value("BOOKED"));
    }

    @Test
    void shouldReturn400WhenCreateValidationFails() throws Exception {
        CreateAppointmentRequest invalid = new CreateAppointmentRequest(
                "",
                "Kowalski",
                "123",
                "not-an-email",
                1L,
                LocalDateTime.now().plusDays(1),
                10L,
                null,
                null
        );

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void shouldReturn400WhenPhoneBlocked() throws Exception {
        CreateAppointmentRequest request = validRequest();
        when(appointmentService.create(request)).thenThrow(new BusinessRuleException("Phone number is blocked."));

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Phone number is blocked."));
    }

    @Test
    void shouldCancelByBookingToken() throws Exception {
        mockMvc.perform(post("/appointments/cancel/{bookingToken}", "TOKEN123"))
                .andExpect(status().isNoContent());

        verify(appointmentService).cancelByBookingToken("TOKEN123");
    }

    @Test
    void shouldReturn404WhenCancelTokenMissing() throws Exception {
        doThrow(new NotFoundException("Booking token not found."))
                .when(appointmentService).cancelByBookingToken("MISSING");

        mockMvc.perform(post("/appointments/cancel/{bookingToken}", "MISSING"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnStaffDetails() throws Exception {
        StaffAppointmentResponse response = new StaffAppointmentResponse(
                1L, 1L, "Haircut", 10L,
                LocalDateTime.of(2026, 6, 15, 10, 0),
                LocalDateTime.of(2026, 6, 15, 11, 0),
                "Jan", "Kowalski", "123456789", "jan@example.com",
                "TOKEN123", false, "BOOKED"
        );
        when(appointmentService.getStaffDetails(1L)).thenReturn(response);

        mockMvc.perform(get("/appointments/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.email").value("jan@example.com"));
    }

    @Test
    void shouldUpdateStatus() throws Exception {
        UpdateAppointmentStatusRequest request = new UpdateAppointmentStatusRequest("COMPLETED");
        StaffAppointmentResponse response = new StaffAppointmentResponse(
                1L, 1L, "Haircut", 10L,
                LocalDateTime.of(2026, 6, 15, 10, 0),
                LocalDateTime.of(2026, 6, 15, 11, 0),
                "Jan", "Kowalski", "123456789", "jan@example.com",
                "TOKEN123", false, "COMPLETED"
        );
        when(appointmentService.updateStatus(eq(1L), eq(com.barber_manager.appointment_service.enums.AppointmentStatus.COMPLETED)))
                .thenReturn(response);

        mockMvc.perform(patch("/appointments/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    private CreateAppointmentRequest validRequest() {
        return new CreateAppointmentRequest(
                "Jan",
                "Kowalski",
                "123456789",
                "jan@example.com",
                1L,
                LocalDateTime.of(2026, 6, 15, 10, 0),
                10L,
                null,
                null
        );
    }
}
