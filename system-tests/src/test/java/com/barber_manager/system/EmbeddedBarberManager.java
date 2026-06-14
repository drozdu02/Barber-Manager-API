package com.barber_manager.system;

import com.barber_manager.appointment_service.AppointmentServiceApplication;
import com.barber_manager.auth_service.AuthServiceApplication;
import com.barber_manager.user_service.UserServiceApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Uruchamia mikroserwisy na losowych portach (H2 in-memory) do testów systemowych.
 */
public final class EmbeddedBarberManager implements AutoCloseable {

    private final List<ConfigurableApplicationContext> contexts = new ArrayList<>();

    private final int userPort;
    private final int authPort;
    private final int appointmentPort;

    public EmbeddedBarberManager() {
        userPort = startUserService();
        authPort = startAuthService(userPort);
        appointmentPort = startAppointmentService(authPort);
    }

    public int userPort() {
        return userPort;
    }

    public int authPort() {
        return authPort;
    }

    public int appointmentPort() {
        return appointmentPort;
    }

    public String userBaseUrl() {
        return "http://localhost:" + userPort;
    }

    public String authBaseUrl() {
        return "http://localhost:" + authPort;
    }

    public String appointmentBaseUrl() {
        return "http://localhost:" + appointmentPort;
    }

    @Override
    public void close() {
        for (int i = contexts.size() - 1; i >= 0; i--) {
            contexts.get(i).close();
        }
        contexts.clear();
    }

    private int startUserService() {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(UserServiceApplication.class)
                .properties(withoutExtraSecurity(commonProperties()))
                .properties(
                        "spring.datasource.url=jdbc:h2:mem:user_system;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                        "spring.application.name=user-service"
                )
                .run();
        contexts.add(context);
        return requiredPort(context);
    }

    private int startAuthService(int userPort) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(AuthServiceApplication.class)
                .properties(withoutExtraSecurity(commonProperties()))
                .properties(
                        "spring.datasource.url=jdbc:h2:mem:auth_system;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                        "spring.application.name=auth-service",
                        "spring.cloud.openfeign.client.config.user-service.url=http://localhost:" + userPort,
                        "auth.refresh-cookie.name=refreshToken",
                        "auth.refresh-cookie.path=/auth",
                        "auth.refresh-cookie.secure=false",
                        "auth.refresh-cookie.same-site=Lax"
                )
                .run();
        contexts.add(context);
        return requiredPort(context);
    }

    private int startAppointmentService(int authPort) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(AppointmentServiceApplication.class)
                .properties(commonProperties())
                .properties(
                        "spring.datasource.url=jdbc:h2:mem:appointment_system;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                        "spring.application.name=appointment-service",
                        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:"
                                + authPort + "/.well-known/jwks.json",
                        "email.username=system-test@example.com",
                        "email.password=system-test-password",
                        "appointment.reminder.enabled=false"
                )
                .run();
        contexts.add(context);
        return requiredPort(context);
    }

    private static String[] commonProperties() {
        return new String[]{
                "server.port=0",
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.config.import=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "management.tracing.enabled=false",
                "management.otlp.metrics.export.enabled=false"
        };
    }

    private static final String NON_APPOINTMENT_SECURITY_EXCLUDES =
            "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration,"
                    + "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration,"
                    + "org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration,"
                    + "org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration";

    private static String[] withoutExtraSecurity(String... properties) {
        String[] merged = new String[properties.length + 1];
        merged[0] = "spring.autoconfigure.exclude=" + NON_APPOINTMENT_SECURITY_EXCLUDES;
        System.arraycopy(properties, 0, merged, 1, properties.length);
        return merged;
    }

    private static int requiredPort(ConfigurableApplicationContext context) {
        Integer port = context.getEnvironment().getProperty("local.server.port", Integer.class);
        if (port == null) {
            throw new IllegalStateException("Could not resolve local.server.port for " + context.getId());
        }
        return port;
    }
}
