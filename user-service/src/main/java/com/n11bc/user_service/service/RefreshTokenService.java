package com.n11bc.user_service.service;

import com.n11bc.user_service.entity.RefreshToken;
import com.n11bc.user_service.entity.User;

import java.util.Optional;

public interface RefreshTokenService {

    /**
     * Finds a refresh token by its token value.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Creates or replaces the stored refresh token for a user.
     */
    RefreshToken createOrUpdateRefreshToken(User user, String refreshTokenValue);

    /**
     * Validates expiration and revoked state for a refresh token.
     */
    RefreshToken verifyExpiration(RefreshToken token);

    /**
     * Replaces an existing refresh token with a newly issued token value.
     */
    RefreshToken updateRefreshToken(RefreshToken existingToken, String newRefreshToken);

    /**
     * Revokes the refresh token owned by a user.
     */
    void revokeByUser(User user);

    /**
     * Deletes expired refresh token records.
     */
    void deleteExpiredTokens();
}
