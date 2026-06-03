package com.barber_manager.auth_service.controller;

import com.barber_manager.auth_service.dto.request.LoginRequestDto;
import com.barber_manager.auth_service.dto.request.LogoutRequestDto;
import com.barber_manager.auth_service.dto.request.RefreshRequestDto;
import com.barber_manager.auth_service.dto.request.RegisterRequestDto;
import com.barber_manager.auth_service.dto.response.StaffAccountResponse;
import com.barber_manager.auth_service.dto.response.TokenResponseDto;
import com.barber_manager.auth_service.service.AuthService;
import com.barber_manager.auth_service.web.RefreshTokenCookieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/register")
    public ResponseEntity<StaffAccountResponse> register(
            @Valid @RequestBody RegisterRequestDto registerRequestDto
            ){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response
    ){
        TokenResponseDto tokens = authService.login(loginRequestDto);
        refreshTokenCookieService.setRefreshToken(response, tokens.refreshToken(), Duration.ofDays(7));
        return ResponseEntity.ok(TokenResponseDto.accessOnly(tokens.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(
            @RequestBody(required = false) RefreshRequestDto refreshRequestDto,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        String refreshToken = (refreshRequestDto != null && refreshRequestDto.refreshToken() != null && !refreshRequestDto.refreshToken().isBlank())
                ? refreshRequestDto.refreshToken()
                : refreshTokenCookieService.extractRefreshToken(request).orElse(null);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token cookie missing.");
        }

        TokenResponseDto tokens = authService.refreshToken(refreshToken);
        refreshTokenCookieService.setRefreshToken(response, tokens.refreshToken(), Duration.ofDays(7));
        return ResponseEntity.ok(TokenResponseDto.accessOnly(tokens.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) LogoutRequestDto logoutRequestDto,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        String refreshToken = (logoutRequestDto != null && logoutRequestDto.refreshToken() != null && !logoutRequestDto.refreshToken().isBlank())
                ? logoutRequestDto.refreshToken()
                : refreshTokenCookieService.extractRefreshToken(request).orElse(null);

        authService.logoutByRefreshToken(refreshToken);
        refreshTokenCookieService.clearRefreshToken(response);
        return ResponseEntity.noContent().build();
    }
}
