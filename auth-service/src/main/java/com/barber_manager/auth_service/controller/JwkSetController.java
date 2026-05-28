package com.barber_manager.auth_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.math.BigInteger;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwkSetController {

    private final KeyPair rsaKeyPair;
    private static final String KEY_ID = "barber-manager-key";

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks(){
        RSAPublicKey publicKey = (RSAPublicKey) rsaKeyPair.getPublic();
        return Map.of("keys", List.of(Map.of(
                "kty", "RSA",
                "use", "sig",
                "alg", "RS256",
                "kid", KEY_ID,
                "n", Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(toUnsignedBytes(publicKey.getModulus())),
                "e", Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(toUnsignedBytes(publicKey.getPublicExponent()))
        )));
    }

    private static byte[] toUnsignedBytes(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            return trimmed;
        }
        return bytes;
    }
}
