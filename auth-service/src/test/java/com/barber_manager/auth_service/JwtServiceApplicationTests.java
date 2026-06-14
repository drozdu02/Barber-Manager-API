package com.barber_manager.auth_service;

import com.barber_manager.auth_service.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "eureka.client.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"
})
class JwtServiceApplicationTests {

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void contextLoads() {
    }
}
