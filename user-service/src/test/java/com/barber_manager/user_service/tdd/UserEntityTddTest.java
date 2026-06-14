package com.barber_manager.user_service.tdd;

import com.barber_manager.user_service.entity.User;
import com.barber_manager.user_service.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * TDD — encja {@link User}: model konta pracownika (barber / admin).
 */
@DisplayName("TDD: encja User — role i dane konta")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserEntityTddTest {

    @Test
    @Order(1)
    @DisplayName("Krok 1 (Red→Green): nowy użytkownik nie ma przypisanej roli domyślnie")
    void step1_newUserShouldHaveNoRoleByDefault() {
        // Given
        User user = new User();

        // Then — test zdefiniowany przed wymuszeniem roli w rejestracji
        assertNull(user.getRole());
    }

    @Test
    @Order(2)
    @DisplayName("Krok 2 (Green): konto fryzjera ma rolę BARBER")
    void step2_barberAccountShouldHaveBarberRole() {
        // Given
        User user = new User();
        user.setEmail("fryzjer@salon.pl");
        user.setRole(Role.BARBER);

        // Then
        assertEquals(Role.BARBER, user.getRole());
    }

    @Test
    @Order(3)
    @DisplayName("Krok 3 (Refactor): konto administratora ma rolę ADMIN")
    void step3_adminAccountShouldHaveAdminRole() {
        // Given
        User user = new User();
        user.setEmail("admin@salon.pl");
        user.setRole(Role.ADMIN);

        // Then
        assertEquals(Role.ADMIN, user.getRole());
    }
}
