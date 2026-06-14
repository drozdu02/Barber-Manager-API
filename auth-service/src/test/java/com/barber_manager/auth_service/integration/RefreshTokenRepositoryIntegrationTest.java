package com.barber_manager.auth_service.integration;

import com.barber_manager.auth_service.entity.RefreshToken;
import com.barber_manager.auth_service.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("integration")
@DisplayName("Integracja: RefreshTokenRepository (adapter JPA)")
class RefreshTokenRepositoryIntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindRefreshTokenByValue() {
        RefreshToken token = activeToken(1L, "refresh-token-abc");
        refreshTokenRepository.save(token);

        RefreshToken found = refreshTokenRepository.findByToken("refresh-token-abc").orElseThrow();

        assertEquals(1L, found.getUserId());
        assertFalse(found.isRevoked());
    }

    @Test
    @Transactional
    void shouldRevokeAllActiveTokensForUser() {
        refreshTokenRepository.save(activeToken(10L, "token-a"));
        refreshTokenRepository.save(activeToken(10L, "token-b"));
        refreshTokenRepository.save(activeToken(99L, "token-other"));

        refreshTokenRepository.revokeAllByUserId(10L);
        entityManager.flush();
        entityManager.clear();

        assertTrue(refreshTokenRepository.findByToken("token-a").orElseThrow().isRevoked());
        assertTrue(refreshTokenRepository.findByToken("token-b").orElseThrow().isRevoked());

        RefreshToken otherUserToken = refreshTokenRepository.findByToken("token-other").orElseThrow();
        assertFalse(otherUserToken.isRevoked());
    }

    private static RefreshToken activeToken(Long userId, String tokenValue) {
        return RefreshToken.builder()
                .userId(userId)
                .token(tokenValue)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
    }
}
