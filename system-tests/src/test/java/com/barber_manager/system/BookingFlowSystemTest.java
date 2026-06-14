package com.barber_manager.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test systemowy: rejestracja personelu → logowanie → lista fryzjerów → rezerwacja → anulowanie.
 * Ścieżka: adapter HTTP (REST) → serwisy → adaptery JPA / Feign.
 */
@DisplayName("E2E: pełna ścieżka rezerwacji wizyty")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingFlowSystemTest {

    private static EmbeddedBarberManager environment;
    private static ObjectMapper objectMapper;
    private static RestClient userClient;
    private static RestClient authClient;
    private static RestClient appointmentClient;

    @BeforeAll
    static void startEnvironment() {
        environment = new EmbeddedBarberManager();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        userClient = RestClient.builder().baseUrl(environment.userBaseUrl()).build();
        authClient = RestClient.builder().baseUrl(environment.authBaseUrl()).build();
        appointmentClient = RestClient.builder().baseUrl(environment.appointmentBaseUrl()).build();
    }

    @AfterAll
    static void stopEnvironment() {
        if (environment != null) {
            environment.close();
        }
    }

    @Test
    @Order(1)
    void shouldCompletePublicBookingAndCancellationFlow() throws Exception {
        BarberSession session = registerBarberSession(uniqueEmail(), "Jan", "Fryzjer");

        JsonNode barbers = objectMapper.readTree(userClient.get()
                .uri("/api/v1/barbers")
                .retrieve()
                .body(String.class));
        assertTrue(barbers.isArray());
        assertTrue(containsBarberId(barbers, session.barberId()));

        JsonNode services = objectMapper.readTree(appointmentClient.get()
                .uri("/services")
                .retrieve()
                .body(String.class));
        assertTrue(services.isArray() && !services.isEmpty());

        long serviceId = services.get(0).get("id").asLong();
        LocalDateTime startTime = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).withSecond(0).withNano(0);

        ObjectNode bookingRequest = objectMapper.createObjectNode();
        bookingRequest.put("firstName", "Anna");
        bookingRequest.put("lastName", "Klient");
        bookingRequest.put("phoneNumber", "987654321");
        bookingRequest.put("email", "anna.klient@test.pl");
        bookingRequest.put("serviceId", serviceId);
        bookingRequest.put("barberId", session.barberId());
        bookingRequest.putPOJO("startTime", startTime);

        JsonNode created = objectMapper.readTree(appointmentClient.post()
                .uri("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(bookingRequest))
                .retrieve()
                .body(String.class));

        assertNotNull(created);
        assertFalse(created.get("canceled").asBoolean());
        String bookingToken = created.get("bookingToken").asText();

        appointmentClient.post()
                .uri("/appointments/cancel/{token}", bookingToken)
                .retrieve()
                .toBodilessEntity();
    }

    @Test
    @Order(2)
    void shouldAllowStaffToReadAppointmentAfterLogin() throws Exception {
        BarberSession session = registerBarberSession(uniqueEmail(), "Marek", "Fryzjer");

        JsonNode services = objectMapper.readTree(appointmentClient.get()
                .uri("/services")
                .retrieve()
                .body(String.class));
        long serviceId = services.get(0).get("id").asLong();
        LocalDateTime startTime = LocalDateTime.now().plusDays(4).withHour(11).withMinute(0).withSecond(0).withNano(0);

        ObjectNode bookingRequest = objectMapper.createObjectNode();
        bookingRequest.put("firstName", "Piotr");
        bookingRequest.put("lastName", "Klient");
        bookingRequest.put("phoneNumber", "111222333");
        bookingRequest.put("email", "piotr.klient@test.pl");
        bookingRequest.put("serviceId", serviceId);
        bookingRequest.put("barberId", session.barberId());
        bookingRequest.putPOJO("startTime", startTime);

        JsonNode created = objectMapper.readTree(appointmentClient.post()
                .uri("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(bookingRequest))
                .retrieve()
                .body(String.class));
        long appointmentId = created.get("id").asLong();

        JsonNode details = objectMapper.readTree(appointmentClient.get()
                .uri("/appointments/{id}", appointmentId)
                .header("Authorization", "Bearer " + session.accessToken())
                .retrieve()
                .body(String.class));

        assertEquals(appointmentId, details.get("id").asLong());
        assertEquals(session.barberId(), details.get("barberId").asLong());
        assertEquals("111222333", details.get("phoneNumber").asText());
    }

    private static BarberSession registerBarberSession(String email, String firstName, String lastName) throws Exception {
        long barberId = registerStaff(firstName, lastName, email, "BARBER", uniquePhone());
        String accessToken = login(email, "password123");
        return new BarberSession(barberId, accessToken);
    }

    private static long registerStaff(String firstName, String lastName, String email, String role, String phone) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("email", email);
        body.put("password", "password123");
        body.put("phoneNumber", phone);
        body.put("role", role);

        JsonNode response = objectMapper.readTree(userClient.post()
                .uri("/internal/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(body))
                .retrieve()
                .body(String.class));

        return response.get("id").asLong();
    }

    private static String login(String email, String password) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("email", email);
        body.put("password", password);

        JsonNode response = objectMapper.readTree(authClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(body))
                .retrieve()
                .body(String.class));

        return response.get("accessToken").asText();
    }

    private static boolean containsBarberId(JsonNode barbers, long barberId) {
        for (JsonNode barber : barbers) {
            if (barber.get("id").asLong() == barberId) {
                return true;
            }
        }
        return false;
    }

    private static String uniqueEmail() {
        return "barber-" + java.util.UUID.randomUUID() + "@salon.pl";
    }

    private static String uniquePhone() {
        String digits = java.util.UUID.randomUUID().toString().replaceAll("\\D", "");
        return digits.substring(0, 9);
    }

    private record BarberSession(long barberId, String accessToken) {
    }
}
