package com.barber_manager.auth_service;

import com.barber_manager.auth_service.service.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import io.jsonwebtoken.security.SignatureException;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @Mock
    private KeyPair keyPair;

    @InjectMocks
    private JwtService jwtService;

    private KeyPair keyPairForTests;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        keyPairForTests = keyPairGenerator.generateKeyPair();
    }

    @Test
    void shouldGenerateTokenAndParseValidAccessToken() throws Exception {
        String email = "jan.kowalski@example.com";
        String role = "BARBER";
        when(keyPair.getPrivate()).thenReturn(keyPairForTests.getPrivate());
        when(keyPair.getPublic()).thenReturn(keyPairForTests.getPublic());

        String token = jwtService.generateAccessToken(email, role);

        assertNotNull(token);
        assertFalse(token.isBlank());

        Claims claims = jwtService.parseToken(token);

        assertEquals(email, claims.getSubject());
        assertEquals(role, claims.get("role", String.class));
        assertEquals("http://localhost:8082", claims.getIssuer());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void shouldThrowSignatureExceptionWhenTokenIsTampered() throws Exception {
        when(keyPair.getPrivate()).thenReturn(keyPairForTests.getPrivate());
        when(keyPair.getPublic()).thenReturn(keyPairForTests.getPublic());

        String validToken = jwtService.generateAccessToken("jan.kowalski@example.com", "BARBER");
        String tamperedToken = validToken.substring(0, validToken.length() - 1) + "X";

        assertThrows(SignatureException.class, () -> jwtService.parseToken(tamperedToken));
    }
}
