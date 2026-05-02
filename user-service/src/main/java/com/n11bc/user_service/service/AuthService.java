package com.n11bc.user_service.service;

import com.n11bc.user_service.dto.request.LoginRequest;
import com.n11bc.user_service.dto.request.RefreshTokenRequest;
import com.n11bc.user_service.dto.response.JwtResponse;
import com.n11bc.user_service.entity.RefreshToken;
import com.n11bc.user_service.entity.User;
import com.n11bc.user_service.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        User user = userService.findByUsernameOrEmail(loginRequest.getUsernameOrEmail());

        if (!user.isActive()) {
            throw new AuthenticationException("User account is deactivated");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        String accessToken = generateAccessToken(user);
        String refreshTokenValue = UUID.randomUUID().toString();
        refreshTokenService.createOrUpdateRefreshToken(user, refreshTokenValue);

        log.info("User authenticated successfully: {}", user.getUsername());

        return buildResponse(accessToken, refreshTokenValue, user);
    }

    @Transactional
    public JwtResponse refreshAccessToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthenticationException("Refresh token not found"));

        refreshToken = refreshTokenService.verifyExpiration(refreshToken);
        User user = refreshToken.getUser();

        if (!user.isActive()) {
            throw new AuthenticationException("User account is deactivated");
        }

        String newAccessToken = generateAccessToken(user);
        String newRefreshTokenValue = UUID.randomUUID().toString();
        refreshTokenService.updateRefreshToken(refreshToken, newRefreshTokenValue);

        log.info("Token refreshed for user: {}", user.getUsername());

        return buildResponse(newAccessToken, newRefreshTokenValue, user);
    }

    @Transactional
    public void logout(String usernameOrEmail) {
        User user = userService.findByUsernameOrEmail(usernameOrEmail);
        refreshTokenService.revokeByUser(user);
        log.info("User logged out: {}", user.getUsername());
    }

    private String generateAccessToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("n11-user-service")
                .subject(user.getId())
                .issuedAt(now)
                .expiresAt(now.plusMillis(jwtExpirationMs))
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("roles", List.of(user.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private JwtResponse buildResponse(String accessToken, String refreshToken, User user) {
        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
