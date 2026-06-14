package com.barber_manager.user_service;

import com.barber_manager.user_service.dto.request.CreateUserRequest;
import com.barber_manager.user_service.dto.request.RegisterRequestDto;
import com.barber_manager.user_service.dto.request.UpdateUserRequest;
import com.barber_manager.user_service.dto.response.BarberResponseDto;
import com.barber_manager.user_service.dto.response.UserCredentialDto;
import com.barber_manager.user_service.dto.response.UserResponseDto;
import com.barber_manager.user_service.entity.User;
import com.barber_manager.user_service.enums.Role;
import com.barber_manager.user_service.exceptions.UserAlreadyExistsException;
import com.barber_manager.user_service.exceptions.UserNotFoundException;
import com.barber_manager.user_service.exceptions.UserServiceLogicException;
import com.barber_manager.user_service.mapper.UserMapper;
import com.barber_manager.user_service.repository.UserRepository;
import com.barber_manager.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldReturnUserIfExists() {
        User user = new User();
        user.setId(1L);

        UserResponseDto userResponseDto = new UserResponseDto(
                1L,
                "jan.kowalski@example.com",
                "Jan",
                "Kowalski"
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto userResponse = userService.getUserById(1L);

        assertEquals(1L, userResponse.id());
        assertEquals("Jan", userResponse.firstName());
    }

    @Test
    void shouldThrowWhenUserNotFoundById() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void shouldReturnAllUsersIfExist() {
        List<User> users = new ArrayList<>();

        User user = new User();
        user.setId(1L);
        users.add(user);

        UserResponseDto userResponseDto = new UserResponseDto(
                1L,
                "jan.kowalski@example.com",
                "Jan",
                "Kowalski"
        );

        when(userRepository.findAllByOrderByCreatedAtDesc()).thenReturn(users);
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        List<UserResponseDto> userResponseDtos = userService.getAllUsers();

        assertEquals(1, userResponseDtos.size());
        assertEquals(1L, userResponseDtos.getFirst().id());
        assertEquals("Jan", userResponseDtos.getFirst().firstName());

        verify(userRepository).findAllByOrderByCreatedAtDesc();
        verify(userMapper).toUserResponseDto(user);
    }

    @Test
    void shouldReturnAllBarbers() {
        List<User> users = new ArrayList<>();
        User user = new User();
        user.setId(1L);
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setRole(Role.BARBER);
        users.add(user);

        when(userRepository.findAllByRoleOrderByFirstNameAscLastNameAsc(Role.BARBER)).thenReturn(users);

        List<BarberResponseDto> barberResponseDtos = userService.getBarbers();

        assertEquals(1, barberResponseDtos.size());
        assertEquals(1L, barberResponseDtos.getFirst().id());
        assertEquals("Jan", barberResponseDtos.getFirst().firstName());
        assertEquals("Kowalski", barberResponseDtos.getFirst().lastName());

        verify(userRepository).findAllByRoleOrderByFirstNameAscLastNameAsc(Role.BARBER);
    }

    @Test
    void shouldReturnEmptyBarberList() {
        when(userRepository.findAllByRoleOrderByFirstNameAscLastNameAsc(Role.BARBER)).thenReturn(List.of());

        List<BarberResponseDto> barbers = userService.getBarbers();

        assertEquals(0, barbers.size());
    }

    @Test
    void canCreateUser() {
        CreateUserRequest createUserRequest = new CreateUserRequest(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password123",
                "123456789"
        );

        User user = new User();
        user.setId(1L);
        user.setEmail("jan.kowalski@example.com");
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setPassword("encoded-password");
        user.setPhoneNumber("123456789");
        user.setRole(Role.USER);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("jan.kowalski@example.com");
        savedUser.setFirstName("Jan");
        savedUser.setLastName("Kowalski");

        UserResponseDto userResponseDto = new UserResponseDto(
                1L,
                "jan.kowalski@example.com",
                "Jan",
                "Kowalski"
        );

        when(userMapper.toEntity(createUserRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toUserResponseDto(savedUser)).thenReturn(userResponseDto);

        UserResponseDto createUserResponse = userService.createUser(createUserRequest);

        assertEquals(1L, createUserResponse.id());
        assertEquals("Jan", createUserResponse.firstName());
        assertEquals("Kowalski", createUserResponse.lastName());

        verify(userMapper).toEntity(createUserRequest);
        verify(userRepository).save(user);
        verify(userMapper).toUserResponseDto(savedUser);
    }

    @Test
    void canUpdateUser() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(
                "Jan",
                "Kowalski",
                "123456789"
        );
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("jan.kowalski@example.com");
        updatedUser.setFirstName("Jan");
        updatedUser.setLastName("Kowalski");

        UserResponseDto userResponseDto = new UserResponseDto(
                1L,
                "jan.kowalski@example.com",
                "Jan",
                "Kowalski"
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserResponseDto(updatedUser)).thenReturn(userResponseDto);

        UserResponseDto updateUserResponseDto = userService.updateUser(1L, updateUserRequest);
        assertEquals(1L, updateUserResponseDto.id());
        assertEquals("Jan", updateUserResponseDto.firstName());
        assertEquals("Kowalski", updateUserResponseDto.lastName());

        verify(userMapper).updateUserFromDto(updateUserRequest, existingUser);
        verify(userRepository).save(existingUser);
    }

    @Test
    void shouldThrowWhenUpdatingMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateUserRequest request = new UpdateUserRequest("Jan", "Kowalski", "123456789");

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(99L, request));
    }

    @Test
    void canGetCredentialsByEmail() {
        User user = new User();
        user.setId(1L);
        user.setEmail("jan.kowalski@example.com");
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("jan.kowalski@example.com")).thenReturn(Optional.of(user));

        UserCredentialDto result = userService.getCredentialsByEmail("jan.kowalski@example.com");

        assertEquals(1L, result.id());
        assertEquals("jan.kowalski@example.com", result.email());
        assertEquals("encoded-password", result.password());
        assertEquals(Role.USER, result.role());

        verify(userRepository).findByEmail("jan.kowalski@example.com");
    }

    @Test
    void shouldThrowWhenCredentialsNotFoundByEmail() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getCredentialsByEmail("missing@example.com")
        );
    }

    @Test
    void canGetCredentialsById() {
        User user = new User();
        user.setId(1L);
        user.setEmail("jan.kowalski@example.com");
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserCredentialDto result = userService.getCredentialsById(1L);
        assertEquals(1L, result.id());
        assertEquals("jan.kowalski@example.com", result.email());
        assertEquals("encoded-password", result.password());
        assertEquals(Role.USER, result.role());

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenCredentialsNotFoundById() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getCredentialsById(99L));
    }

    @Test
    void shouldCreateBarberFromAuth() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password123",
                "123456789",
                Role.BARBER
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UserCredentialDto result = userService.createUserFromAuth(request);

        assertEquals(1L, result.id());
        assertEquals("jan.kowalski@example.com", result.email());
        assertEquals("encoded-password", result.password());
        assertEquals(Role.BARBER, result.role());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User persisted = userCaptor.getValue();
        assertEquals("Jan", persisted.getFirstName());
        assertEquals("Kowalski", persisted.getLastName());
        assertEquals("123456789", persisted.getPhoneNumber());
        assertEquals(Role.BARBER, persisted.getRole());
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void shouldCreateAdminFromAuth() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Admin",
                "User",
                "admin@example.com",
                "password123",
                "987654321",
                Role.ADMIN
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        UserCredentialDto result = userService.createUserFromAuth(request);

        assertEquals(Role.ADMIN, result.role());
    }

    @Test
    void shouldRejectUserRoleInCreateUserFromAuth() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password123",
                "123456789",
                Role.USER
        );

        assertThrows(UserServiceLogicException.class, () -> userService.createUserFromAuth(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmailInCreateUserFromAuth() {
        RegisterRequestDto request = new RegisterRequestDto(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "password123",
                "123456789",
                Role.BARBER
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUserFromAuth(request));
        verify(userRepository, never()).save(any(User.class));
    }
}
