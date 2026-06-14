package com.barber_manager.auth_service;

import com.barber_manager.auth_service.client.UserClient;
import com.barber_manager.auth_service.dto.request.LoginRequestDto;
import com.barber_manager.auth_service.dto.request.LogoutRequestDto;
import com.barber_manager.auth_service.dto.request.RegisterRequestDto;
import com.barber_manager.auth_service.dto.response.StaffAccountResponse;
import com.barber_manager.auth_service.dto.response.TokenResponseDto;
import com.barber_manager.auth_service.dto.response.UserCredentialDto;
import com.barber_manager.auth_service.entity.RefreshToken;
import com.barber_manager.auth_service.enums.Role;
import com.barber_manager.auth_service.exceptions.InvalidRegistrationException;
import com.barber_manager.auth_service.exceptions.InvalidTokenException;
import com.barber_manager.auth_service.exceptions.StaffAccessDeniedException;
import com.barber_manager.auth_service.exceptions.UserAlreadyExistsException;
import com.barber_manager.auth_service.repository.RefreshTokenRepository;
import com.barber_manager.auth_service.service.AuthService;
import com.barber_manager.auth_service.service.JwtService;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserClient userClient;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Captor
    private ArgumentCaptor<RefreshToken> refreshTokenArgumentCaptor;

    @Test
    void shouldLogin() {
        UserCredentialDto userCredentialDto = new UserCredentialDto(
                1L,
                "jan.kowalski@example.com",
                "encoded-password",
                Role.BARBER
        );

        when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh_token");

        LoginRequestDto loginRequestDto = new LoginRequestDto(
                "jan.kowalski@example.com",
                "password"
        );
        when(userClient.getCredentialsByEmail("jan.kowalski@example.com")).thenReturn(userCredentialDto);
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        TokenResponseDto token = authService.login(loginRequestDto);

        assertNotNull(token);
        assertEquals("access_token", token.accessToken());
        verify(refreshTokenRepository).revokeAllByUserId(1L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldRejectLoginWithInvalidPassword() {
        UserCredentialDto userCredentialDto = new UserCredentialDto(
                1L,
                "jan.kowalski@example.com",
                "encoded-password",
                Role.BARBER
        );
        when(userClient.getCredentialsByEmail("jan.kowalski@example.com")).thenReturn(userCredentialDto);
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        LoginRequestDto loginRequestDto = new LoginRequestDto("jan.kowalski@example.com", "wrong");

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequestDto)
        );
        assertEquals("Invalid password.", ex.getMessage());
    }

    @Test
    void shouldRejectLoginWhenUserLookupFails() {
        when(userClient.getCredentialsByEmail("missing@example.com"))
                .thenThrow(new RuntimeException("upstream error"));

        LoginRequestDto loginRequestDto = new LoginRequestDto("missing@example.com", "password");

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequestDto)
        );
        assertEquals("Invalid credentials.", ex.getMessage());
    }

    @Test
    void shouldRejectLoginForNonStaffRole() {
        UserCredentialDto userCredentialDto = new UserCredentialDto(
                1L,
                "client@example.com",
                "encoded-password",
                Role.USER
        );
        when(userClient.getCredentialsByEmail("client@example.com")).thenReturn(userCredentialDto);
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        assertThrows(
                StaffAccessDeniedException.class,
                () -> authService.login(new LoginRequestDto("client@example.com", "password"))
        );
    }

    @Test
    void shouldRegister() {
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password123",
                "123456789",
                Role.BARBER
        );
        UserCredentialDto userCredentialDto = new UserCredentialDto(
                1L,
                "jan.kowalski@example.com",
                "password",
                Role.BARBER
        );

        when(userClient.createUser(registerRequestDto)).thenReturn(userCredentialDto);

        StaffAccountResponse response = authService.register(registerRequestDto);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("jan.kowalski@example.com", response.email());
        assertEquals("BARBER", response.role());
        assertEquals("Jan", response.firstName());
        assertEquals("Kowalski", response.lastName());
    }

    @Test
    void shouldRejectRegisterForNonStaffRole() {
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password123",
                "123456789",
                Role.USER
        );

        assertThrows(InvalidRegistrationException.class, () -> authService.register(registerRequestDto));
        verify(userClient, never()).createUser(any());
    }

    @Test
    void shouldRejectRegisterWhenEmailAlreadyExists() {
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password123",
                "123456789",
                Role.BARBER
        );

        when(userClient.createUser(registerRequestDto)).thenThrow(mock(FeignException.Conflict.class));

        UserAlreadyExistsException ex = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(registerRequestDto)
        );
        assertEquals("User already exists with provided email.", ex.getMessage());
    }

    @Test
    void shouldLogoutSuccessfullyWhenTokenExists() {
        LogoutRequestDto logoutRequestDto = new LogoutRequestDto("valid-refresh-token");
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("valid-refresh-token");

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(refreshToken));

        authService.logout(logoutRequestDto);

        verify(refreshTokenRepository).save(refreshTokenArgumentCaptor.capture());
        assertTrue(refreshTokenArgumentCaptor.getValue().isRevoked());
    }

    @Test
    void shouldIgnoreLogoutByRefreshTokenWhenBlank() {
        authService.logoutByRefreshToken(null);
        authService.logoutByRefreshToken("   ");

        verify(refreshTokenRepository, never()).findByToken(any());
    }

    @Test
    void shouldLogoutByRefreshToken() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("cookie-token");
        when(refreshTokenRepository.findByToken("cookie-token")).thenReturn(Optional.of(refreshToken));

        authService.logoutByRefreshToken("cookie-token");

        verify(refreshTokenRepository).save(refreshTokenArgumentCaptor.capture());
        assertTrue(refreshTokenArgumentCaptor.getValue().isRevoked());
    }

    @Test
    void shouldLogoutAllTokensForUser() {
        authService.logoutAll(7L);
        verify(refreshTokenRepository).revokeAllByUserId(7L);
    }

    @Test
    void shouldRefreshToken() {
        String refreshTokenValue = "valid-refresh-token";

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken(refreshTokenValue);
        mockRefreshToken.setUserId(1L);
        mockRefreshToken.setRevoked(false);
        mockRefreshToken.setExpiresAt(Instant.now().plusSeconds(3600));

        UserCredentialDto userCredentialDto = new UserCredentialDto(
                1L,
                "jan.kowalski@example.com",
                "password",
                Role.BARBER
        );

        when(refreshTokenRepository.findByToken(refreshTokenValue)).thenReturn(Optional.of(mockRefreshToken));
        when(userClient.getCredentialsById(1L)).thenReturn(userCredentialDto);
        when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(anyString())).thenReturn("new_refresh_token");

        TokenResponseDto response = authService.refreshToken(refreshTokenValue);

        assertNotNull(response);
        verify(refreshTokenRepository, never()).delete(any());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldRejectRefreshWhenTokenNotFound() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> authService.refreshToken("missing"));
    }

    @Test
    void shouldRejectRefreshWhenTokenRevoked() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("revoked");
        refreshToken.setRevoked(true);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken("revoked")).thenReturn(Optional.of(refreshToken));

        assertThrows(InvalidTokenException.class, () -> authService.refreshToken("revoked"));
    }

    @Test
    void shouldRejectRefreshWhenTokenExpired() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("expired");
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(Instant.now().minusSeconds(60));

        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(refreshToken));

        assertThrows(InvalidTokenException.class, () -> authService.refreshToken("expired"));
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void shouldRejectRefreshForNonStaffUser() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("staff-check");
        refreshToken.setUserId(2L);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken("staff-check")).thenReturn(Optional.of(refreshToken));
        when(userClient.getCredentialsById(2L)).thenReturn(new UserCredentialDto(
                2L,
                "client@example.com",
                "encoded",
                Role.USER
        ));

        assertThrows(StaffAccessDeniedException.class, () -> authService.refreshToken("staff-check"));
        verify(refreshTokenRepository).save(refreshTokenArgumentCaptor.capture());
        assertTrue(refreshTokenArgumentCaptor.getValue().isRevoked());
    }
}
