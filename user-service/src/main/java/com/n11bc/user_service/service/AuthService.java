package com.n11bc.user_service.service;

import com.n11bc.user_service.dto.request.LoginRequest;
import com.n11bc.user_service.dto.request.RefreshTokenRequest;
import com.n11bc.user_service.dto.response.JwtResponse;
import com.n11bc.user_service.dto.response.KeycloakTokenResponse;
import com.n11bc.user_service.entity.RefreshToken;
import com.n11bc.user_service.entity.User;
import com.n11bc.user_service.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final KeycloakAuthService keycloakAuthService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        // Find user by username or email
        User user = userService.findByUsernameOrEmail(loginRequest.getUsernameOrEmail());

        // Check if user is active
        if (!user.isActive()) {
            throw new AuthenticationException("User account is deactivated");
        }

        // Verify password
        if (!userService.verifyPassword(user, loginRequest.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        // Authenticate with Keycloak using username (not email)
        KeycloakTokenResponse keycloakResponse = keycloakAuthService.authenticate(
                user.getUsername(),
                loginRequest.getPassword()
        );

        // Store refresh token in database
        refreshTokenService.createOrUpdateRefreshToken(user, keycloakResponse.getRefreshToken());

        log.info("User authenticated successfully: {}", user.getUsername());

        // Build JWT response
        return JwtResponse.builder()
                .accessToken(keycloakResponse.getAccessToken())
                .refreshToken(keycloakResponse.getRefreshToken())
                .tokenType(keycloakResponse.getTokenType())
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    @Transactional
    public JwtResponse refreshAccessToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // Find refresh token in database
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> new AuthenticationException("Refresh token not found"));

        // Verify token expiration
        refreshToken = refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        // Check if user is active
        if (!user.isActive()) {
            throw new AuthenticationException("User account is deactivated");
        }

        // Call Keycloak to refresh token
        KeycloakTokenResponse keycloakResponse = keycloakAuthService.refreshToken(requestRefreshToken);

        // Update refresh token in database if Keycloak returned a new one
        if (!keycloakResponse.getRefreshToken().equals(requestRefreshToken)) {
            refreshTokenService.updateRefreshToken(refreshToken, keycloakResponse.getRefreshToken());
        }

        log.info("Token refreshed successfully for user: {}", user.getUsername());

        // Build JWT response
        return JwtResponse.builder()
                .accessToken(keycloakResponse.getAccessToken())
                .refreshToken(keycloakResponse.getRefreshToken())
                .tokenType(keycloakResponse.getTokenType())
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    @Transactional
    public void logout(String userId) {
        User user = userService.findByUsernameOrEmail(userId);
        refreshTokenService.revokeByUser(user);
        log.info("User logged out: {}", user.getUsername());
    }
}
