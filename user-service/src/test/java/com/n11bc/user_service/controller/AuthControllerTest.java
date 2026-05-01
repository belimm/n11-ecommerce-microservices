package com.n11bc.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bc.user_service.dto.request.LoginRequest;
import com.n11bc.user_service.dto.request.RefreshTokenRequest;
import com.n11bc.user_service.dto.request.SignupRequest;
import com.n11bc.user_service.dto.response.JwtResponse;
import com.n11bc.user_service.dto.response.MessageResponse;
import com.n11bc.user_service.dto.response.UserResponse;
import com.n11bc.user_service.entity.Role;
import com.n11bc.user_service.exception.AuthenticationException;
import com.n11bc.user_service.exception.UserAlreadyExistsException;
import com.n11bc.user_service.service.AuthService;
import com.n11bc.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private UserResponse userResponse;
    private JwtResponse jwtResponse;

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id("user-id-1")
                .username("testuser")
                .email("test@example.com")
                .role(Role.CUSTOMER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        jwtResponse = JwtResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .id("user-id-1")
                .username("testuser")
                .email("test@example.com")
                .role(Role.CUSTOMER)
                .build();
    }

    // ---- POST /api/auth/signup ----

    @Test
    @DisplayName("POST /api/auth/signup: basarili kayit 201 dondurur")
    void signup_success_returns201() throws Exception {
        SignupRequest request = new SignupRequest("testuser", "test@example.com", "password123",
                "Test", "User", "5551234567", Role.CUSTOMER);
        when(userService.registerUser(any(SignupRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/signup: validation hatasi 400 dondurur")
    void signup_validationError_returns400() throws Exception {
        SignupRequest invalidRequest = new SignupRequest("ab", "not-email", "123", null, null, null, Role.CUSTOMER);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/signup: username bos ise 400 dondurur")
    void signup_blankUsername_returns400() throws Exception {
        SignupRequest invalidRequest = new SignupRequest("", "test@example.com", "password123",
                null, null, null, Role.CUSTOMER);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/signup: kullanici adi zaten mevcut 409 dondurur")
    void signup_usernameConflict_returns409() throws Exception {
        SignupRequest request = new SignupRequest("testuser", "test@example.com", "password123",
                null, null, null, Role.CUSTOMER);
        when(userService.registerUser(any(SignupRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Username already exists: testuser"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ---- POST /api/auth/signin ----

    @Test
    @DisplayName("POST /api/auth/signin: basarili giris 200 ve JWT dondurur")
    void signin_success_returns200WithJwt() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password123");
        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/auth/signin: gecersiz kimlik bilgileri 401 dondurur")
    void signin_invalidCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        when(authService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/signin: bos alan validation 400 dondurur")
    void signin_blankFields_returns400() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ---- POST /api/auth/refresh ----

    @Test
    @DisplayName("POST /api/auth/refresh: basarili token yenileme 200 dondurur")
    void refresh_success_returns200() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        when(authService.refreshAccessToken(any(RefreshTokenRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh: gecersiz token 401 dondurur")
    void refresh_invalidToken_returns401() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");
        when(authService.refreshAccessToken(any(RefreshTokenRequest.class)))
                .thenThrow(new AuthenticationException("Refresh token not found"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/refresh: bos refresh token 400 dondurur")
    void refresh_blankToken_returns400() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ---- POST /api/auth/logout ----

    @Test
    @DisplayName("POST /api/auth/logout: basarili cikis 200 dondurur")
    void logout_success_returns200() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .with(jwt())
                        .header("X-User-Id", "user-id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User logged out successfully"));
    }

    @Test
    @DisplayName("POST /api/auth/logout: header eksikse 400 dondurur")
    void logout_missingHeader_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .with(jwt()))
                .andExpect(status().isBadRequest());
    }
}
