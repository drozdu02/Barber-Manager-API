package com.barber_manager.auth_service.tdd;

import com.barber_manager.auth_service.client.UserClient;
import com.barber_manager.auth_service.dto.request.RegisterRequestDto;
import com.barber_manager.auth_service.dto.response.StaffAccountResponse;
import com.barber_manager.auth_service.dto.response.UserCredentialDto;
import com.barber_manager.auth_service.enums.Role;
import com.barber_manager.auth_service.exceptions.InvalidRegistrationException;
import com.barber_manager.auth_service.repository.RefreshTokenRepository;
import com.barber_manager.auth_service.service.AuthService;
import com.barber_manager.auth_service.service.JwtService;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD — rejestracja konta pracownika w auth-service (No-Account Policy dla klientów).
 */
@DisplayName("TDD: AuthService — rejestracja tylko dla personelu")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaffRegistrationTddTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserClient userClient;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @Order(1)
    @DisplayName("Krok 1 (Red): klient (rola USER) nie może się zarejestrować")
    void step1_clientRoleShouldBeRejectedAtRegistration() {
        // Given — wymaganie No-Account Policy
        RegisterRequestDto request = new RegisterRequestDto(
                "Anna", "Klient", "anna@test.pl", "haslo123", "444444444", Role.USER
        );

        // When / Then — test napisany przed implementacją isStaffRole()
        assertThrows(InvalidRegistrationException.class, () -> authService.register(request));
        verify(userClient, never()).createUser(request);
    }

    @Test
    @Order(2)
    @DisplayName("Krok 2 (Green): fryzjer otrzymuje konto po udanej rejestracji")
    void step2_barberRegistrationShouldReturnStaffAccount() {
        // Given
        RegisterRequestDto request = new RegisterRequestDto(
                "Jan", "Fryzjer", "jan@salon.pl", "haslo123", "555555555", Role.BARBER
        );
        UserCredentialDto credentials = new UserCredentialDto(1L, "jan@salon.pl", "hash", Role.BARBER);
        when(userClient.createUser(request)).thenReturn(credentials);

        // When
        StaffAccountResponse response = authService.register(request);

        // Then
        assertEquals(1L, response.id());
        assertEquals("jan@salon.pl", response.email());
        assertEquals("BARBER", response.role());
    }

    @Test
    @Order(3)
    @DisplayName("Krok 3 (Refactor): konflikt e-maila w user-service mapuje się na UserAlreadyExistsException")
    void step3_duplicateEmailFromUserServiceShouldPropagateAsConflict() {
        // Given
        RegisterRequestDto request = new RegisterRequestDto(
                "Admin", "Salon", "admin@salon.pl", "haslo123", "666666666", Role.ADMIN
        );
        when(userClient.createUser(request)).thenThrow(mock(FeignException.Conflict.class));

        // When / Then
        assertThrows(
                com.barber_manager.auth_service.exceptions.UserAlreadyExistsException.class,
                () -> authService.register(request)
        );
    }
}
