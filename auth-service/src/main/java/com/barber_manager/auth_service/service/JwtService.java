package com.barber_manager.auth_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final KeyPair rsaKeyPair;

    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 15 * 60 * 1000;
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;
    private static final String KEY_ID = "barber-manager-key";

    public String generateAccessToken(String subject, String role) {
        return Jwts.builder()
                .header().keyId(KEY_ID).and()
                .subject(subject)
                .claim("role", role)
                .issuer("http://localhost:8082")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(rsaKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }
    public String generateRefreshToken(String subject) {
        return Jwts.builder()
                .header().keyId(KEY_ID).and()
                .subject(subject)
                .issuer("http://localhost:8082")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .signWith(rsaKeyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith((RSAPublicKey) rsaKeyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
