package com.barber_manager.user_service.integration;

import com.barber_manager.user_service.entity.User;
import com.barber_manager.user_service.enums.Role;
import com.barber_manager.user_service.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("integration")
@DisplayName("Integracja: UserRepository (adapter JPA)")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistAndFindUserByEmail() {
        User user = sampleUser("jan@salon.pl", "Jan", "Kowalski", Role.BARBER);
        userRepository.save(user);

        User found = userRepository.findByEmail("jan@salon.pl").orElseThrow();

        assertEquals("Jan", found.getFirstName());
        assertEquals(Role.BARBER, found.getRole());
    }

    @Test
    void shouldFindBarbersOrderedByFirstAndLastName() {
        userRepository.save(sampleUser("b@salon.pl", "Zosia", "Adamska", Role.BARBER));
        userRepository.save(sampleUser("a@salon.pl", "Anna", "Baran", Role.BARBER));
        userRepository.save(sampleUser("c@salon.pl", "Admin", "Salon", Role.ADMIN));

        List<User> barbers = userRepository.findAllByRoleOrderByFirstNameAscLastNameAsc(Role.BARBER);

        assertEquals(2, barbers.size());
        assertEquals("Anna", barbers.get(0).getFirstName());
        assertEquals("Zosia", barbers.get(1).getFirstName());
    }

    @Test
    void shouldReportWhetherEmailAlreadyExists() {
        userRepository.save(sampleUser("exists@salon.pl", "Ewa", "Test", Role.BARBER));

        assertTrue(userRepository.existsByEmail("exists@salon.pl"));
        assertFalse(userRepository.existsByEmail("missing@salon.pl"));
    }

    private static User sampleUser(String email, String firstName, String lastName, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber("123456789");
        user.setRole(role);
        return user;
    }
}
