package com.barber_manager.auth_service;

import com.barber_manager.auth_service.config.SecurityConfig;
import com.barber_manager.auth_service.controller.AuthController;
import com.barber_manager.auth_service.dto.request.LoginRequestDto;
import com.barber_manager.auth_service.dto.request.LogoutRequestDto;
import com.barber_manager.auth_service.dto.request.RefreshRequestDto;
import com.barber_manager.auth_service.dto.request.RegisterRequestDto;
import com.barber_manager.auth_service.dto.response.StaffAccountResponse;
import com.barber_manager.auth_service.dto.response.TokenResponseDto;
import com.barber_manager.auth_service.enums.Role;
import com.barber_manager.auth_service.service.AuthService;
import com.barber_manager.auth_service.web.RefreshTokenCookieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RefreshTokenCookieService refreshTokenCookieService;

    @Test
    void shouldRegister() throws Exception {
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password",
                "123456789",
                Role.BARBER
        );
        StaffAccountResponse staffAccountResponse = new StaffAccountResponse(
                1L,
                "jan.kowalski@example.com",
                "Jan",
                "Kowalski",
                Role.BARBER.toString()
        );
        when(authService.register(registerRequestDto)).thenReturn(staffAccountResponse);

        mockMvc.perform(post("/auth/register")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestDto))
                .with(csrf())

        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.email").value("jan.kowalski@example.com"))
                .andExpect(jsonPath("$.role").value("BARBER"));
    }

    @Test
    void shouldLogin() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto(
                "jan.kowalski@example.com",
                "password"
        );
        TokenResponseDto tokenResponseDto = new TokenResponseDto(
                "access_token",
                "refresh-token",
                "token-type",
                1L
        );
        when(authService.login(loginRequestDto)).thenReturn(tokenResponseDto);

        mockMvc.perform(post("/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto))
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refresh_token").doesNotExist());
    }

    @Test
    void shouldLogout() throws Exception {
        LogoutRequestDto logoutRequestDto = new LogoutRequestDto(
                "refresh-token"
        );

        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequestDto))
                .with(csrf())
        )
                .andExpect(status().isNoContent());

        verify(authService, times(1)).logoutByRefreshToken("refresh-token");
        verify(refreshTokenCookieService, times(1)).clearRefreshToken(any());
        verify(refreshTokenCookieService, never()).extractRefreshToken(any());
    }

    @Test
    void shouldRefresh() throws Exception {
        RefreshRequestDto refreshRequestDto = new RefreshRequestDto(
                "refresh-token"
        );

        TokenResponseDto tokenResponseDto = new TokenResponseDto(
                "new-access-token",
                "new-refresh-token",
                "Bearer",
                1L
        );
        when(authService.refreshToken("refresh-token")).thenReturn(tokenResponseDto);

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequestDto))
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist());

        verify(refreshTokenCookieService, times(1)).setRefreshToken(
                any(HttpServletResponse.class),
                eq("new-refresh-token"),
                eq(Duration.ofDays(7)));

    }

}
