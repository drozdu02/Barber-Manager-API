package com.barber_manager.user_service;

import com.barber_manager.user_service.dto.request.CreateUserRequest;
import com.barber_manager.user_service.error.GlobalExceptionHandler;
import com.barber_manager.user_service.exceptions.UserAlreadyExistsException;
import com.barber_manager.user_service.exceptions.UserNotFoundException;
import com.barber_manager.user_service.exceptions.UserServiceLogicException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    void shouldReturn404ForUserNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found."))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    void shouldReturn409ForUserAlreadyExists() throws Exception {
        mockMvc.perform(get("/test/already-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User already exists."));
    }

    @Test
    void shouldReturn400ForUserServiceLogic() throws Exception {
        mockMvc.perform(get("/test/logic-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid role."));
    }

    @Test
    void shouldReturn400ForMethodArgumentNotValid() throws Exception {
        CreateUserRequest invalid = new CreateUserRequest(
                "",
                "Kowalski",
                "not-an-email",
                "short",
                "123"
        );

        mockMvc.perform(post("/test/validate-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").exists());
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
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("Unsupported Content-Type. Use application/json."));
    }

    @Test
    void shouldReturn400ForTypeMismatch() throws Exception {
        mockMvc.perform(get("/test/type-mismatch").param("id", "not-a-number"))
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
    void shouldReturn500ForUnexpectedError() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected server error."));
    }

    @RestController
    static class ExceptionTestController {

        @GetMapping("/test/not-found")
        void notFound() {
            throw new UserNotFoundException("User not found.");
        }

        @GetMapping("/test/already-exists")
        void alreadyExists() {
            throw new UserAlreadyExistsException("User already exists.");
        }

        @GetMapping("/test/logic-error")
        void logicError() {
            throw new UserServiceLogicException("Invalid role.");
        }

        @PostMapping("/test/validate-body")
        void validateBody(@Valid @RequestBody CreateUserRequest request) {
        }

        @GetMapping("/test/type-mismatch")
        void typeMismatch(@RequestParam Long id) {
        }

        @GetMapping("/test/data-integrity")
        void dataIntegrity() {
            throw new DataIntegrityViolationException("duplicate key");
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new RuntimeException("boom");
        }
    }
}
