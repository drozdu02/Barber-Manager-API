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
import com.barber_manager.auth_service.repository.RefreshTokenRepository;
import com.barber_manager.auth_service.service.AuthService;
import com.barber_manager.auth_service.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

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
    void shouldLogin(){
        UserCredentialDto userCredentialDto = new UserCredentialDto(
                1L,
                "jan.kowalski@example.com",
                "encoded-password",
                Role.BARBER
        );

        when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("access_token");

        LoginRequestDto loginRequestDto = new LoginRequestDto(
                "jan.kowalski@example.com",
                "password"
        );
        when(userClient.getCredentialsByEmail("jan.kowalski@example.com")).thenReturn(userCredentialDto);
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        TokenResponseDto token = authService.login(loginRequestDto);
        assertNotNull(token);

        verify(refreshTokenRepository, times(1)).revokeAllByUserId(1L);
    }

    @Test
    void shouldRegister(){
        RegisterRequestDto registerRequestDto = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password",
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
    void shouldLogoutSuccessfullyWhenTokenExists(){
        LogoutRequestDto logoutRequestDto = new LogoutRequestDto(
                "valid-refresh-token"
        );
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("valid-refresh-token");

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(refreshToken));
        authService.logout(logoutRequestDto);
        verify(refreshTokenRepository, times(1)).save(refreshTokenArgumentCaptor.capture());

        RefreshToken savedToken = refreshTokenArgumentCaptor.getValue();

        assertTrue(savedToken.isRevoked());
    }

    @Test
    void shouldRefreshToken(){
        String refreshToken = "valid-refresh-token";

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken(refreshToken);
        mockRefreshToken.setUserId(1L);
        mockRefreshToken.setRevoked(false);
        mockRefreshToken.setExpiresAt(Instant.now().plusSeconds(3600));

        UserCredentialDto userCredentialDto = new UserCredentialDto(
                1L,
                "jan.kowalski@example.com",
                "password",
                Role.BARBER
        );

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(mockRefreshToken));
        when(userClient.getCredentialsById(1L)).thenReturn(userCredentialDto);
        when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("access_token");

        TokenResponseDto response = authService.refreshToken(refreshToken);

        assertNotNull(response);

        verify(refreshTokenRepository, never()).delete(any());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }


}
