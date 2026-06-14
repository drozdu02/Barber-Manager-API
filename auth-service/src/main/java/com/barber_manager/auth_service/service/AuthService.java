package com.barber_manager.auth_service.service;

import com.barber_manager.auth_service.dto.request.LogoutRequestDto;
import com.barber_manager.auth_service.exceptions.InvalidTokenException;
import com.barber_manager.auth_service.exceptions.UserAlreadyExistsException;
import com.barber_manager.auth_service.repository.RefreshTokenRepository;
import com.barber_manager.auth_service.client.UserClient;
import com.barber_manager.auth_service.dto.request.LoginRequestDto;
import com.barber_manager.auth_service.dto.request.RegisterRequestDto;
import com.barber_manager.auth_service.dto.response.StaffAccountResponse;
import com.barber_manager.auth_service.dto.response.TokenResponseDto;
import com.barber_manager.auth_service.dto.response.UserCredentialDto;
import com.barber_manager.auth_service.entity.RefreshToken;
import com.barber_manager.auth_service.enums.Role;
import com.barber_manager.auth_service.exceptions.InvalidRegistrationException;
import com.barber_manager.auth_service.exceptions.StaffAccessDeniedException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserClient userClient;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenResponseDto login(LoginRequestDto loginRequestDto){
        UserCredentialDto user;
        try {
            user = userClient.getCredentialsByEmail(loginRequestDto.email());

        }catch (Exception e){
            throw new BadCredentialsException("Invalid credentials.");
        }
        if (!passwordEncoder.matches(loginRequestDto.password(), user.password())){
            throw new BadCredentialsException("Invalid password.");
        }
        ensureStaffRole(user.role());
        refreshTokenRepository.revokeAllByUserId(user.id());

        return issueTokens(user.id(), user.email(), String.valueOf(user.role()));
    }

    public StaffAccountResponse register(RegisterRequestDto registerRequestDto){
        if (!isStaffRole(registerRequestDto.role())) {
            throw new InvalidRegistrationException();
        }
        UserCredentialDto user;
        try {
            user = userClient.createUser(registerRequestDto);
        }catch (FeignException.Conflict e){
            throw new UserAlreadyExistsException("User already exists with provided email.");
        }
        return new StaffAccountResponse(
                user.id(),
                user.email(),
                registerRequestDto.firstName(),
                registerRequestDto.lastName(),
                String.valueOf(user.role())
        );
    }
    public void logout(LogoutRequestDto requestDto){
        refreshTokenRepository
                .findByToken(requestDto.refreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });

    }

    public void logoutByRefreshToken(String refreshToken){
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository
                .findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }
    public void logoutAll(Long userId){
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private TokenResponseDto issueTokens(Long userId, String email, String role){
        String accessToken = jwtService.generateAccessToken(email, role);
        String refreshToken = jwtService.generateRefreshToken(email);

        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .token(refreshToken)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(token);

        return TokenResponseDto.of(accessToken, refreshToken);
    }


    public TokenResponseDto refreshToken(String refreshTokenValue){
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found."));

        if (refreshToken.isRevoked()){
            throw new InvalidTokenException("Invalid refresh token.");
        }
        if (refreshToken.getExpiresAt().isBefore(Instant.now())){
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidTokenException("Refresh token expired.");
        }
        UserCredentialDto user = userClient.getCredentialsById(refreshToken.getUserId());
        if (!isStaffRole(user.role())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new StaffAccessDeniedException();
        }
        return issueTokens(user.id(), user.email(), String.valueOf(user.role()));
    }

    private void ensureStaffRole(Role role) {
        if (!isStaffRole(role)) {
            throw new StaffAccessDeniedException();
        }
    }

    private boolean isStaffRole(Role role) {
        return role == Role.BARBER || role == Role.ADMIN;
    }

}
