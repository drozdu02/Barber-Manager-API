package com.barber_manager.appointment_service;

import com.barber_manager.appointment_service.error.ApiErrorResponse;
import com.barber_manager.appointment_service.error.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerConstraintTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturn400ForConstraintViolation() {
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("phoneNumber");
        when(violation.getMessage()).thenReturn("must be 9 digits");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));
        HttpServletRequest request = new MockHttpServletRequest("POST", "/appointments");

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<ApiErrorResponse> response = handler.handleConstraintViolation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed.", response.getBody().message());
        assertEquals("phoneNumber", response.getBody().fieldErrors().getFirst().field());
    }
}
