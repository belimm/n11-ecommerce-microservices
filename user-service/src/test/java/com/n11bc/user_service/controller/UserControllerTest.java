package com.n11bc.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bc.user_service.dto.request.UpdateUserRequest;
import com.n11bc.user_service.dto.response.UserResponse;
import com.n11bc.user_service.entity.Role;
import com.n11bc.user_service.exception.UserAlreadyExistsException;
import com.n11bc.user_service.exception.UserNotFoundException;
import com.n11bc.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private UserResponse userResponse;
    private static final String USER_ID = "user-id-1";

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id(USER_ID)
                .username("testuser")
                .email("test@example.com")
                .role(Role.CUSTOMER)
                .firstName("Test")
                .lastName("User")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ---- GET /api/users ----

    @Test
    @DisplayName("GET /api/users: ADMIN rolü ile 200 dondurur")
    void getAllUsers_asAdmin_returns200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @DisplayName("GET /api/users: ADMIN degil 403 dondurur")
    void getAllUsers_asCustomer_returns403() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());

        verify(userService, never()).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/users: kimlik dogrulama yok 401 dondurur")
    void getAllUsers_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/users: bos liste dondurur")
    void getAllUsers_empty_returns200WithEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ---- GET /api/users/{id} ----

    @Test
    @DisplayName("GET /api/users/{id}: basarili getirme 200 dondurur")
    void getUserById_success_returns200() throws Exception {
        when(userService.getUserById(USER_ID)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/{id}", USER_ID)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("GET /api/users/{id}: kullanici bulunamadi 404 dondurur")
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById("non-existent"))
                .thenThrow(new UserNotFoundException("non-existent", "id"));

        mockMvc.perform(get("/api/users/{id}", "non-existent")
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/{id}: kimlik dogrulama yok 401 dondurur")
    void getUserById_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/users/{id}", USER_ID))
                .andExpect(status().isUnauthorized());
    }

    // ---- PUT /api/users/{id} ----

    @Test
    @DisplayName("PUT /api/users/{id}: basarili guncelleme 200 dondurur")
    void updateUser_success_returns200() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("new@example.com", "NewFirst", "NewLast", "5559999999");
        when(userService.updateUser(eq(USER_ID), any(UpdateUserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/users/{id}: gecersiz email 400 dondurur")
    void updateUser_invalidEmail_returns400() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("not-an-email", null, null, null);

        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/users/{id}: kullanici bulunamadi 404 dondurur")
    void updateUser_notFound_returns404() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(null, "New", null, null);
        when(userService.updateUser(eq("non-existent"), any(UpdateUserRequest.class)))
                .thenThrow(new UserNotFoundException("non-existent", "id"));

        mockMvc.perform(put("/api/users/{id}", "non-existent")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/users/{id}: email cakismasi 409 dondurur")
    void updateUser_emailConflict_returns409() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("taken@example.com", null, null, null);
        when(userService.updateUser(eq(USER_ID), any(UpdateUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Email already exists: taken@example.com"));

        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ---- DELETE /api/users/{id} ----

    @Test
    @DisplayName("DELETE /api/users/{id}: ADMIN ile basarili silme 200 dondurur")
    void deleteUser_asAdmin_returns200() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", USER_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        verify(userService).deleteUser(USER_ID);
    }

    @Test
    @DisplayName("DELETE /api/users/{id}: ADMIN degil 403 dondurur")
    void deleteUser_asCustomer_returns403() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", USER_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(any());
    }

    @Test
    @DisplayName("DELETE /api/users/{id}: kullanici bulunamadi 404 dondurur")
    void deleteUser_notFound_returns404() throws Exception {
        doThrow(new UserNotFoundException("non-existent", "id")).when(userService).deleteUser("non-existent");

        mockMvc.perform(delete("/api/users/{id}", "non-existent")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    // ---- PATCH /api/users/{id}/activate ----

    @Test
    @DisplayName("PATCH /api/users/{id}/activate: ADMIN ile basarili aktivasyon 200 dondurur")
    void activateUser_asAdmin_returns200() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/activate", USER_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User activated successfully"));

        verify(userService).activateUser(USER_ID);
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/activate: ADMIN degil 403 dondurur")
    void activateUser_asCustomer_returns403() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/activate", USER_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());
    }

    // ---- PATCH /api/users/{id}/deactivate ----

    @Test
    @DisplayName("PATCH /api/users/{id}/deactivate: ADMIN ile basarili deaktivasyon 200 dondurur")
    void deactivateUser_asAdmin_returns200() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/deactivate", USER_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deactivated successfully"));

        verify(userService).deactivateUser(USER_ID);
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/deactivate: ADMIN degil 403 dondurur")
    void deactivateUser_asCustomer_returns403() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/deactivate", USER_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());
    }
}
