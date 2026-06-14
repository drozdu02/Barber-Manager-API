package com.barber_manager.auth_service.integration;

import com.barber_manager.auth_service.client.UserClient;
import com.barber_manager.auth_service.dto.response.UserCredentialDto;
import com.barber_manager.auth_service.enums.Role;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = UserClientIntegrationTest.FeignTestApplication.class,
        properties = {
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false"
        }
)
@ActiveProfiles("integration")
@DisplayName("Integracja: UserClient (adapter Feign → user-service)")
class UserClientIntegrationTest {

    private static final WireMockServer WIRE_MOCK = new WireMockServer(wireMockConfig().dynamicPort());

    static {
        WIRE_MOCK.start();
    }

    @Autowired
    private UserClient userClient;

    @AfterAll
    static void stopWireMock() {
        WIRE_MOCK.stop();
    }

    @DynamicPropertySource
    static void registerFeignUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.cloud.openfeign.client.config.user-service.url",
                () -> "http://localhost:" + WIRE_MOCK.port()
        );
    }

    @Test
    void shouldFetchCredentialsByEmailFromUserService() {
        WIRE_MOCK.resetAll();
        WIRE_MOCK.stubFor(get(urlPathMatching("/internal/users/credentials/email/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 1,
                                  "email": "jan@salon.pl",
                                  "password": "encoded-hash",
                                  "role": "BARBER"
                                }
                                """)));

        UserCredentialDto credentials = userClient.getCredentialsByEmail("jan@salon.pl");

        assertEquals(1L, credentials.id());
        assertEquals("jan@salon.pl", credentials.email());
        assertEquals("encoded-hash", credentials.password());
        assertEquals(Role.BARBER, credentials.role());
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = UserClient.class)
    @org.springframework.boot.autoconfigure.ImportAutoConfiguration(FeignAutoConfiguration.class)
    static class FeignTestApplication {
    }
}
