package com.n11bc.user_service.service;

import com.n11bc.user_service.dto.request.LoginRequest;
import com.n11bc.user_service.dto.request.RefreshTokenRequest;
import com.n11bc.user_service.dto.response.JwtResponse;

public interface AuthService {

    /**
     * Authenticates a user and returns access and refresh tokens.
     */
    JwtResponse authenticateUser(LoginRequest loginRequest);

    /**
     * Issues a new access token when the submitted refresh token is valid.
     */
    JwtResponse refreshAccessToken(RefreshTokenRequest request);

    /**
     * Revokes the refresh token associated with the given username, email, or user id input.
     */
    void logout(String usernameOrEmail);
}
