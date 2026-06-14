package com.barber_manager.user_service;

import com.barber_manager.user_service.error.ApiErrorResponse;
import com.barber_manager.user_service.error.GlobalExceptionHandler;
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
        when(violation.getPropertyPath().toString()).thenReturn("value");
        when(violation.getMessage()).thenReturn("must not be blank");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));
        HttpServletRequest request = new MockHttpServletRequest("GET", "/test/constraint");

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<ApiErrorResponse> response = handler.handleConstraintViolation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed.", response.getBody().message());
        assertEquals(1, response.getBody().fieldErrors().size());
        assertEquals("value", response.getBody().fieldErrors().getFirst().field());
    }
}
