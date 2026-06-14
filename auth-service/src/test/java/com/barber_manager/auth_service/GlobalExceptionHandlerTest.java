package com.barber_manager.auth_service;

import com.barber_manager.auth_service.dto.request.RegisterRequestDto;
import com.barber_manager.auth_service.enums.Role;
import com.barber_manager.auth_service.error.GlobalExceptionHandler;
import com.barber_manager.auth_service.exceptions.InvalidRegistrationException;
import com.barber_manager.auth_service.exceptions.InvalidTokenException;
import com.barber_manager.auth_service.exceptions.StaffAccessDeniedException;
import com.barber_manager.auth_service.exceptions.UserAlreadyExistsException;
import com.barber_manager.auth_service.exceptions.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn401ForBadCredentials() throws Exception {
        mockMvc.perform(get("/test/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials."));
    }

    @Test
    void shouldReturn401ForInvalidToken() throws Exception {
        mockMvc.perform(get("/test/invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token not found."));
    }

    @Test
    void shouldReturn403ForStaffAccessDenied() throws Exception {
        mockMvc.perform(get("/test/staff-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Login is restricted to barber and administrator accounts."));
    }

    @Test
    void shouldReturn400ForInvalidRegistration() throws Exception {
        mockMvc.perform(get("/test/invalid-registration"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only barber and administrator accounts can be created."));
    }

    @Test
    void shouldReturn404ForUserNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found."));
    }

    @Test
    void shouldReturn409ForUserAlreadyExists() throws Exception {
        mockMvc.perform(get("/test/already-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists."));
    }

    @Test
    void shouldReturn400ForMethodArgumentNotValid() throws Exception {
        RegisterRequestDto invalid = new RegisterRequestDto(
                "",
                "Kowalski",
                "not-an-email",
                "short",
                "123",
                Role.BARBER
        );

        mockMvc.perform(post("/test/validate-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void shouldReturn400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/test/validate-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request."));
    }

    @Test
    void shouldReturn415ForUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/test/validate-body")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void shouldReturn400ForTypeMismatch() throws Exception {
        mockMvc.perform(get("/test/type-mismatch").param("id", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid parameter value."));
    }

    @Test
    void shouldReturn409ForDataIntegrityViolation() throws Exception {
        mockMvc.perform(get("/test/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Data integrity violation."));
    }

    @Test
    void shouldReturn400ForResponseStatusException() throws Exception {
        mockMvc.perform(get("/test/response-status"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Refresh token cookie missing."));
    }

    @Test
    void shouldReturn500ForUnexpectedError() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected server error."));
    }

    @RestController
    static class ExceptionTestController {

        @GetMapping("/test/bad-credentials")
        void badCredentials() {
            throw new BadCredentialsException("Invalid credentials.");
        }

        @GetMapping("/test/invalid-token")
        void invalidToken() {
            throw new InvalidTokenException("Refresh token not found.");
        }

        @GetMapping("/test/staff-denied")
        void staffDenied() {
            throw new StaffAccessDeniedException();
        }

        @GetMapping("/test/invalid-registration")
        void invalidRegistration() {
            throw new InvalidRegistrationException();
        }

        @GetMapping("/test/not-found")
        void notFound() {
            throw new UserNotFoundException("User not found.");
        }

        @GetMapping("/test/already-exists")
        void alreadyExists() {
            throw new UserAlreadyExistsException("User already exists.");
        }

        @PostMapping("/test/validate-body")
        void validateBody(@Valid @RequestBody RegisterRequestDto request) {
        }

        @GetMapping("/test/type-mismatch")
        void typeMismatch(@RequestParam Long id) {
        }

        @GetMapping("/test/data-integrity")
        void dataIntegrity() {
            throw new DataIntegrityViolationException("duplicate");
        }

        @GetMapping("/test/response-status")
        void responseStatus() {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token cookie missing.");
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new RuntimeException("boom");
        }
    }
}
