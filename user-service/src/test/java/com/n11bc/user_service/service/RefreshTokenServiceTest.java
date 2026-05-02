package com.n11bc.user_service.service;

import com.n11bc.user_service.entity.RefreshToken;
import com.n11bc.user_service.entity.User;
import com.n11bc.user_service.exception.TokenRefreshException;
import com.n11bc.user_service.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;
    private RefreshToken validToken;
    private RefreshToken expiredToken;
    private RefreshToken revokedToken;

    private static final String TOKEN_VALUE = "keycloak-refresh-token-xyz";
    private static final long EXPIRATION_MS = 604800000L; // 7 days

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", EXPIRATION_MS);

        user = User.builder().id("user-id-1").username("testuser").build();

        validToken = RefreshToken.builder()
                .id("token-id-1")
                .token(TOKEN_VALUE)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .revoked(false)
                .build();

        expiredToken = RefreshToken.builder()
                .id("token-id-2")
                .token("expired-token")
                .user(user)
                .expiryDate(Instant.now().minusSeconds(3600))
                .createdAt(Instant.now().minusSeconds(7200))
                .revoked(false)
                .build();

        revokedToken = RefreshToken.builder()
                .id("token-id-3")
                .token("revoked-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .revoked(true)
                .build();
    }

    // ---- findByToken ----

    @Test
    @DisplayName("findByToken: mevcut token bulunur")
    void findByToken_found() {
        when(refreshTokenRepository.findByToken(TOKEN_VALUE)).thenReturn(Optional.of(validToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken(TOKEN_VALUE);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(TOKEN_VALUE);
    }

    @Test
    @DisplayName("findByToken: token bulunamadi")
    void findByToken_notFound() {
        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        Optional<RefreshToken> result = refreshTokenService.findByToken("unknown-token");

        assertThat(result).isEmpty();
    }

    // ---- createOrUpdateRefreshToken ----

    @Test
    @DisplayName("createOrUpdateRefreshToken: mevcut token varsa ayni kayit guncellenir")
    void createOrUpdateRefreshToken_updatesExistingToken() {
        String newToken = "new-refresh-token";
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(validToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.createOrUpdateRefreshToken(user, newToken);

        verify(refreshTokenRepository, never()).deleteByUser(user);
        verify(refreshTokenRepository).save(validToken);
        assertThat(result.getId()).isEqualTo("token-id-1");
        assertThat(result.getToken()).isEqualTo(newToken);
        assertThat(result.isRevoked()).isFalse();
    }

    @Test
    @DisplayName("createOrUpdateRefreshToken: expiry date gelecekte olacak sekilde ayarlanir")
    void createOrUpdateRefreshToken_expiryDateInFuture() {
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        Instant before = Instant.now();
        RefreshToken result = refreshTokenService.createOrUpdateRefreshToken(user, TOKEN_VALUE);
        Instant after = Instant.now().plusMillis(EXPIRATION_MS);

        assertThat(result.getExpiryDate()).isAfter(before);
        assertThat(result.getExpiryDate()).isBefore(after.plusSeconds(1));
        assertThat(result.isRevoked()).isFalse();
    }

    // ---- verifyExpiration ----

    @Test
    @DisplayName("verifyExpiration: gecerli ve iptal edilmemis token gecilir")
    void verifyExpiration_valid_returnsToken() {
        RefreshToken result = refreshTokenService.verifyExpiration(validToken);

        assertThat(result).isSameAs(validToken);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("verifyExpiration: suresi dolmus token silinir ve exception firlatilir")
    void verifyExpiration_expired_throwsAndDeletes() {
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(expiredToken))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("Refresh token has expired");

        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("verifyExpiration: iptal edilmis token exception firlatilir")
    void verifyExpiration_revoked_throws() {
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(revokedToken))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("Refresh token has been revoked");

        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("verifyExpiration: suresi dolma kontrolu iptal kontrolunden once yapilir")
    void verifyExpiration_expiredTakesPrecedenceOverRevoked() {
        RefreshToken expiredAndRevoked = RefreshToken.builder()
                .token("both-expired-and-revoked")
                .user(user)
                .expiryDate(Instant.now().minusSeconds(100))
                .revoked(true)
                .build();

        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(expiredAndRevoked))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("Refresh token has expired");
    }

    // ---- updateRefreshToken ----

    @Test
    @DisplayName("updateRefreshToken: token degeri ve expiry guncellenir")
    void updateRefreshToken_updatesValues() {
        String newToken = "new-keycloak-token";
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        Instant before = Instant.now();
        RefreshToken result = refreshTokenService.updateRefreshToken(validToken, newToken);

        assertThat(result.getToken()).isEqualTo(newToken);
        assertThat(result.getExpiryDate()).isAfter(before);
        verify(refreshTokenRepository).save(validToken);
    }

    // ---- revokeByUser ----

    @Test
    @DisplayName("revokeByUser: kullanicinin tum tokenlari iptal edilir")
    void revokeByUser_callsRepository() {
        refreshTokenService.revokeByUser(user);

        verify(refreshTokenRepository).revokeByUser(user);
    }
}
