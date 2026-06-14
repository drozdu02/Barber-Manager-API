package com.barber_manager.auth_service;

import com.barber_manager.auth_service.error.ApiErrorResponse;
import com.barber_manager.auth_service.error.GlobalExceptionHandler;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerFeignTest {

    @Test
    void shouldReturn502ForFeignServerError() {
        FeignException feign = mock(FeignException.class);
        when(feign.status()).thenReturn(502);
        HttpServletRequest request = new MockHttpServletRequest("GET", "/internal/users");

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<ApiErrorResponse> response = handler.handleFeign(feign, request);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Upstream service error.", response.getBody().message());
    }

    @Test
    void shouldReturn400ForFeignClientError() {
        FeignException feign = mock(FeignException.class);
        when(feign.status()).thenReturn(400);
        HttpServletRequest request = new MockHttpServletRequest("GET", "/internal/users");

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<ApiErrorResponse> response = handler.handleFeign(feign, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Upstream service request rejected.", response.getBody().message());
    }
}
