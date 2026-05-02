package com.n11bc.order_service.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    @Override
    public String getCurrentUserId() {
        Authentication authentication = currentAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            String userId = jwtAuthentication.getToken().getClaimAsString("userId");
            return userId == null || userId.isBlank() ? jwtAuthentication.getToken().getSubject() : userId;
        }
        return authentication.getName();
    }

    @Override
    public String getBearerToken() {
        Authentication authentication = currentAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken().getTokenValue();
        }
        throw new IllegalStateException("JWT authentication is required");
    }

    @Override
    public boolean isAdmin() {
        return currentAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN"));
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return authentication;
    }
}
