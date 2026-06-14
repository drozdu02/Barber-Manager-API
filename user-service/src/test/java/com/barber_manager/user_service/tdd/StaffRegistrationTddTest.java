package com.barber_manager.user_service.tdd;

import com.barber_manager.user_service.dto.request.RegisterRequestDto;
import com.barber_manager.user_service.dto.response.UserCredentialDto;
import com.barber_manager.user_service.enums.Role;
import com.barber_manager.user_service.exceptions.UserAlreadyExistsException;
import com.barber_manager.user_service.exceptions.UserServiceLogicException;
import com.barber_manager.user_service.repository.UserRepository;
import com.barber_manager.user_service.service.UserService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("TDD: UserService — rejestracja konta pracownika")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaffRegistrationTddTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.barber_manager.user_service.mapper.UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @Order(1)
    @DisplayName("Krok 1 (Red): rola USER nie może być zarejestrowana jako konto pracownika")
    void step1_userRoleShouldBeRejected() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Klient", "Testowy", "klient@test.pl", "haslo123", "111111111", Role.USER
        );

        assertThrows(UserServiceLogicException.class, () -> userService.createUserFromAuth(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @Order(2)
    @DisplayName("Krok 2 (Green): konto BARBER jest tworzone z zahashowanym hasłem")
    void step2_barberRegistrationShouldPersistEncodedPassword() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Jan", "Barber", "jan@salon.pl", "haslo123", "222222222", Role.BARBER
        );
        when(userRepository.existsByEmail("jan@salon.pl")).thenReturn(false);
        when(passwordEncoder.encode("haslo123")).thenReturn("encoded-secret");

        UserCredentialDto credentials = userService.createUserFromAuth(request);

        assertEquals(Role.BARBER, credentials.role());
        assertEquals("encoded-secret", credentials.password());
        verify(userRepository).save(any());
    }

    @Test
    @Order(3)
    @DisplayName("Krok 3 (Refactor): duplikat e-maila zwraca UserAlreadyExistsException")
    void step3_duplicateEmailShouldBeRejected() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Admin", "Salon", "admin@salon.pl", "haslo123", "333333333", Role.ADMIN
        );
        when(userRepository.existsByEmail("admin@salon.pl")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUserFromAuth(request));
        verify(userRepository, never()).save(any());
    }
}
