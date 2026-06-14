package com.barber_manager.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Rejestracja personelu przez auth-service (adapter Feign → user-service).
 */
@DisplayName("E2E: rejestracja personelu przez auth-service")
class StaffRegistrationSystemTest {

    private static EmbeddedBarberManager environment;
    private static ObjectMapper objectMapper;
    private static RestClient authClient;

    @BeforeAll
    static void startEnvironment() {
        environment = new EmbeddedBarberManager();
        objectMapper = new ObjectMapper();
        authClient = RestClient.builder().baseUrl(environment.authBaseUrl()).build();
    }

    @AfterAll
    static void stopEnvironment() {
        if (environment != null) {
            environment.close();
        }
    }

    @Test
    void shouldRegisterBarberThroughAuthService() throws Exception {
        ObjectNode register = objectMapper.createObjectNode();
        register.put("firstName", "Ewa");
        register.put("lastName", "Admin");
        register.put("email", "ewa.admin-" + java.util.UUID.randomUUID() + "@salon.pl");
        register.put("password", "password123");
        register.put("phoneNumber", "123456789");
        register.put("role", "ADMIN");

        JsonNode account = objectMapper.readTree(authClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(register))
                .retrieve()
                .body(String.class));

        assertEquals(register.get("email").asText(), account.get("email").asText());
        assertEquals("ADMIN", account.get("role").asText());

        ObjectNode login = objectMapper.createObjectNode();
        login.put("email", register.get("email").asText());
        login.put("password", "password123");

        JsonNode tokens = objectMapper.readTree(authClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(login))
                .retrieve()
                .body(String.class));

        assertNotNull(tokens.get("accessToken").asText());
    }
}
