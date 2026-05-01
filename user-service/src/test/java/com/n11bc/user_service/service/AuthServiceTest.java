package com.n11bc.user_service.service;

import com.n11bc.user_service.dto.request.LoginRequest;
import com.n11bc.user_service.dto.request.RefreshTokenRequest;
import com.n11bc.user_service.dto.response.JwtResponse;
import com.n11bc.user_service.dto.response.KeycloakTokenResponse;
import com.n11bc.user_service.entity.RefreshToken;
import com.n11bc.user_service.entity.Role;
import com.n11bc.user_service.entity.User;
import com.n11bc.user_service.exception.AuthenticationException;
import com.n11bc.user_service.exception.TokenRefreshException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private KeycloakAuthService keycloakAuthService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private User inactiveUser;
    private KeycloakTokenResponse keycloakResponse;
    private RefreshToken refreshToken;

    private static final String ACCESS_TOKEN = "access-token-xyz";
    private static final String REFRESH_TOKEN_STR = "refresh-token-xyz";

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id("user-id-1")
                .username("testuser")
                .email("test@example.com")
                .password("encoded-password")
                .role(Role.CUSTOMER)
                .firstName("Test")
                .lastName("User")
                .active(true)
                .build();

        inactiveUser = User.builder()
                .id("user-id-2")
                .username("inactiveuser")
                .email("inactive@example.com")
                .active(false)
                .build();

        keycloakResponse = KeycloakTokenResponse.builder()
                .accessToken(ACCESS_TOKEN)
                .refreshToken(REFRESH_TOKEN_STR)
                .tokenType("Bearer")
                .expiresIn(300)
                .build();

        refreshToken = RefreshToken.builder()
                .id("token-id-1")
                .token(REFRESH_TOKEN_STR)
                .user(activeUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
    }

    // ---- authenticateUser ----

    @Test
    @DisplayName("authenticateUser: basarili giris")
    void authenticateUser_success() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        when(userService.findByUsernameOrEmail("testuser")).thenReturn(activeUser);
        when(userService.verifyPassword(activeUser, "password123")).thenReturn(true);
        when(keycloakAuthService.authenticate("testuser", "password123")).thenReturn(keycloakResponse);
        when(refreshTokenService.createOrUpdateRefreshToken(any(User.class), anyString())).thenReturn(refreshToken);

        JwtResponse result = authService.authenticateUser(request);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.getRefreshToken()).isEqualTo(REFRESH_TOKEN_STR);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    @DisplayName("authenticateUser: hesap pasif edilmis")
    void authenticateUser_inactiveUser() {
        LoginRequest request = new LoginRequest("inactiveuser", "password123");
        when(userService.findByUsernameOrEmail("inactiveuser")).thenReturn(inactiveUser);

        assertThatThrownBy(() -> authService.authenticateUser(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("deactivated");

        verify(keycloakAuthService, never()).authenticate(anyString(), anyString());
    }

    @Test
    @DisplayName("authenticateUser: yanlis sifre")
    void authenticateUser_wrongPassword() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        when(userService.findByUsernameOrEmail("testuser")).thenReturn(activeUser);
        when(userService.verifyPassword(activeUser, "wrongpassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticateUser(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid username or password");

        verify(keycloakAuthService, never()).authenticate(anyString(), anyString());
    }

    @Test
    @DisplayName("authenticateUser: kullanici bulunamadi - UserService exception fırlatır")
    void authenticateUser_userNotFound() {
        LoginRequest request = new LoginRequest("nonexistent", "password123");
        when(userService.findByUsernameOrEmail("nonexistent"))
                .thenThrow(new com.n11bc.user_service.exception.UserNotFoundException("User not found with username or email: nonexistent"));

        assertThatThrownBy(() -> authService.authenticateUser(request))
                .isInstanceOf(com.n11bc.user_service.exception.UserNotFoundException.class);
    }

    // ---- refreshAccessToken ----

    @Test
    @DisplayName("refreshAccessToken: basarili token yenileme")
    void refreshAccessToken_success() {
        RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN_STR);
        when(refreshTokenService.findByToken(REFRESH_TOKEN_STR)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(keycloakAuthService.refreshToken(REFRESH_TOKEN_STR)).thenReturn(keycloakResponse);

        JwtResponse result = authService.refreshAccessToken(request);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        verify(refreshTokenService, never()).updateRefreshToken(any(), any());
    }

    @Test
    @DisplayName("refreshAccessToken: Keycloak yeni refresh token dondurunce guncellenir")
    void refreshAccessToken_newRefreshToken_updatesDb() {
        String newRefreshToken = "new-refresh-token-xyz";
        KeycloakTokenResponse responseWithNewToken = KeycloakTokenResponse.builder()
                .accessToken(ACCESS_TOKEN)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();

        RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN_STR);
        when(refreshTokenService.findByToken(REFRESH_TOKEN_STR)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(keycloakAuthService.refreshToken(REFRESH_TOKEN_STR)).thenReturn(responseWithNewToken);

        authService.refreshAccessToken(request);

        verify(refreshTokenService).updateRefreshToken(refreshToken, newRefreshToken);
    }

    @Test
    @DisplayName("refreshAccessToken: token DB'de bulunamadi")
    void refreshAccessToken_tokenNotFound() {
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");
        when(refreshTokenService.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshAccessToken(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Refresh token not found");
    }

    @Test
    @DisplayName("refreshAccessToken: token suresi dolmus")
    void refreshAccessToken_tokenExpired() {
        RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN_STR);
        when(refreshTokenService.findByToken(REFRESH_TOKEN_STR)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken))
                .thenThrow(new TokenRefreshException(REFRESH_TOKEN_STR, "Refresh token has expired. Please sign in again"));

        assertThatThrownBy(() -> authService.refreshAccessToken(request))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("Refresh token has expired");
    }

    @Test
    @DisplayName("refreshAccessToken: token'a ait kullanici pasif edilmis")
    void refreshAccessToken_userInactive() {
        refreshToken = RefreshToken.builder()
                .id("token-id-2")
                .token(REFRESH_TOKEN_STR)
                .user(inactiveUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN_STR);
        when(refreshTokenService.findByToken(REFRESH_TOKEN_STR)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);

        assertThatThrownBy(() -> authService.refreshAccessToken(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("deactivated");

        verify(keycloakAuthService, never()).refreshToken(anyString());
    }

    // ---- logout ----

    @Test
    @DisplayName("logout: basarili cikis")
    void logout_success() {
        when(userService.findByUsernameOrEmail("user-id-1")).thenReturn(activeUser);

        authService.logout("user-id-1");

        verify(refreshTokenService).revokeByUser(activeUser);
    }

    @Test
    @DisplayName("logout: kullanici bulunamadi")
    void logout_userNotFound() {
        when(userService.findByUsernameOrEmail("non-existent"))
                .thenThrow(new com.n11bc.user_service.exception.UserNotFoundException("User not found with username or email: non-existent"));

        assertThatThrownBy(() -> authService.logout("non-existent"))
                .isInstanceOf(com.n11bc.user_service.exception.UserNotFoundException.class);

        verify(refreshTokenService, never()).revokeByUser(any());
    }
}
