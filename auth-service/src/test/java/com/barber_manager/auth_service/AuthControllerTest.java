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
import com.barber_manager.auth_service.error.GlobalExceptionHandler;
import com.barber_manager.auth_service.exceptions.InvalidRegistrationException;
import com.barber_manager.auth_service.exceptions.StaffAccessDeniedException;
import com.barber_manager.auth_service.exceptions.UserAlreadyExistsException;
import com.barber_manager.auth_service.service.AuthService;
import com.barber_manager.auth_service.web.RefreshTokenCookieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

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
                "password123",
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
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"))
                .andExpect(jsonPath("$.email").value("jan.kowalski@example.com"))
                .andExpect(jsonPath("$.role").value("BARBER"));
    }

    @Test
    void shouldReturn400WhenRegisterValidationFails() throws Exception {
        RegisterRequestDto invalid = new RegisterRequestDto(
                "",
                "Kowalski",
                "not-an-email",
                "short",
                "123",
                Role.BARBER
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void shouldReturn400WhenRegisterRoleInvalid() throws Exception {
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password123",
                "123456789",
                Role.USER
        );
        when(authService.register(registerRequestDto)).thenThrow(new InvalidRegistrationException());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDto))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only barber and administrator accounts can be created."));
    }

    @Test
    void shouldReturn409WhenRegisterEmailExists() throws Exception {
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password123",
                "123456789",
                Role.BARBER
        );
        when(authService.register(registerRequestDto))
                .thenThrow(new UserAlreadyExistsException("User already exists with provided email."));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDto))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldLogin() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto(
                "jan.kowalski@example.com",
                "password123"
        );
        TokenResponseDto tokenResponseDto = new TokenResponseDto(
                "access_token",
                "refresh-token",
                "Bearer",
                900L
        );
        when(authService.login(loginRequestDto)).thenReturn(tokenResponseDto);

        mockMvc.perform(post("/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist());

        verify(refreshTokenCookieService).setRefreshToken(any(), eq("refresh-token"), eq(Duration.ofDays(7)));
    }

    @Test
    void shouldReturn400WhenLoginValidationFails() throws Exception {
        LoginRequestDto invalid = new LoginRequestDto("", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void shouldReturn401WhenLoginFails() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("jan.kowalski@example.com", "wrong");
        when(authService.login(loginRequestDto)).thenThrow(new BadCredentialsException("Invalid password."));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid password."));
    }

    @Test
    void shouldReturn403WhenStaffAccessDenied() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("client@example.com", "password123");
        when(authService.login(loginRequestDto)).thenThrow(new StaffAccessDeniedException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Login is restricted to barber and administrator accounts."));
    }

    @Test
    void shouldLogoutWithBodyToken() throws Exception {
        LogoutRequestDto logoutRequestDto = new LogoutRequestDto("refresh-token");

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequestDto))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(authService).logoutByRefreshToken("refresh-token");
        verify(refreshTokenCookieService).clearRefreshToken(any());
        verify(refreshTokenCookieService, never()).extractRefreshToken(any());
    }

    @Test
    void shouldLogoutWithCookieToken() throws Exception {
        when(refreshTokenCookieService.extractRefreshToken(any())).thenReturn(Optional.of("cookie-token"));

        mockMvc.perform(post("/auth/logout").with(csrf()))
                .andExpect(status().isNoContent());

        verify(authService).logoutByRefreshToken("cookie-token");
        verify(refreshTokenCookieService).clearRefreshToken(any());
    }

    @Test
    void shouldRefreshWithBodyToken() throws Exception {
        RefreshRequestDto refreshRequestDto = new RefreshRequestDto("refresh-token");
        TokenResponseDto tokenResponseDto = new TokenResponseDto(
                "new-access-token",
                "new-refresh-token",
                "Bearer",
                900L
        );
        when(authService.refreshToken("refresh-token")).thenReturn(tokenResponseDto);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequestDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist());

        verify(refreshTokenCookieService).setRefreshToken(any(), eq("new-refresh-token"), eq(Duration.ofDays(7)));
    }

    @Test
    void shouldRefreshWithCookieToken() throws Exception {
        when(refreshTokenCookieService.extractRefreshToken(any())).thenReturn(Optional.of("cookie-token"));
        when(authService.refreshToken("cookie-token")).thenReturn(TokenResponseDto.of("access", "refresh"));

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refreshToken", "cookie-token"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));

        verify(authService).refreshToken("cookie-token");
    }

    @Test
    void shouldReturn400WhenRefreshTokenMissing() throws Exception {
        when(refreshTokenCookieService.extractRefreshToken(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/refresh").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Refresh token cookie missing."));
    }
}
