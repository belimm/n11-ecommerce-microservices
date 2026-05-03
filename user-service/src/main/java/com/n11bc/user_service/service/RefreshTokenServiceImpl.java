package com.n11bc.user_service.service;

import com.n11bc.user_service.entity.RefreshToken;
import com.n11bc.user_service.entity.User;
import com.n11bc.user_service.exception.TokenRefreshException;
import com.n11bc.user_service.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${app.refresh-token.expiration-ms}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public RefreshToken createOrUpdateRefreshToken(User user, String refreshTokenValue) {
        Instant now = Instant.now();

        return refreshTokenRepository.findByUser(user)
                .map(existingToken -> {
                    existingToken.setToken(refreshTokenValue);
                    existingToken.setExpiryDate(now.plusMillis(refreshTokenDurationMs));
                    existingToken.setRevoked(false);
                    return refreshTokenRepository.save(existingToken);
                })
                .orElseGet(() -> refreshTokenRepository.save(RefreshToken.builder()
                        .user(user)
                        .token(refreshTokenValue)
                        .expiryDate(now.plusMillis(refreshTokenDurationMs))
                        .createdAt(now)
                        .revoked(false)
                        .build()));
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token has expired. Please sign in again");
        }

        if (token.isRevoked()) {
            throw new TokenRefreshException(token.getToken(), "Refresh token has been revoked");
        }

        return token;
    }

    @Override
    @Transactional
    public RefreshToken updateRefreshToken(RefreshToken existingToken, String newRefreshToken) {
        existingToken.setToken(newRefreshToken);
        existingToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        return refreshTokenRepository.save(existingToken);
    }

    @Override
    @Transactional
    public void revokeByUser(User user) {
        refreshTokenRepository.revokeByUser(user);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteExpiredTokens() {
        log.info("Cleaning up expired refresh tokens");
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
    }
}
