package com.n11bc.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bc.user_service.dto.request.AddressRequest;
import com.n11bc.user_service.dto.response.AddressResponse;
import com.n11bc.user_service.exception.AddressNotFoundException;
import com.n11bc.user_service.exception.UserNotFoundException;
import com.n11bc.user_service.service.AddressService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddressService addressService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private AddressResponse addressResponse;
    private AddressRequest validRequest;

    private static final String USER_ID = "user-id-1";
    private static final String ADDRESS_ID = "address-id-1";
    private static final String BASE_URL = "/api/users/{userId}/addresses";

    @BeforeEach
    void setUp() {
        addressResponse = AddressResponse.builder()
                .id(ADDRESS_ID)
                .title("Home")
                .street("Main St 123")
                .city("Istanbul")
                .country("Turkey")
                .zipCode("34000")
                .defaultAddress(true)
                .createdAt(LocalDateTime.now())
                .build();

        validRequest = new AddressRequest("Home", "Main St 123", "Istanbul", "Turkey", "34000", false);
    }

    // ---- POST /api/users/{userId}/addresses ----

    @Test
    @DisplayName("POST addresses: basarili olusturma 201 dondurur")
    void createAddress_success_returns201() throws Exception {
        when(addressService.createAddress(eq(USER_ID), any(AddressRequest.class))).thenReturn(addressResponse);

        mockMvc.perform(post(BASE_URL, USER_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ADDRESS_ID))
                .andExpect(jsonPath("$.title").value("Home"))
                .andExpect(jsonPath("$.city").value("Istanbul"));
    }

    @Test
    @DisplayName("POST addresses: kullanici bulunamadi 404 dondurur")
    void createAddress_userNotFound_returns404() throws Exception {
        when(addressService.createAddress(eq("non-existent"), any(AddressRequest.class)))
                .thenThrow(new UserNotFoundException("non-existent", "id"));

        mockMvc.perform(post(BASE_URL, "non-existent")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST addresses: bos title validation hatasi 400 dondurur")
    void createAddress_blankTitle_returns400() throws Exception {
        AddressRequest invalidRequest = new AddressRequest("", "Main St", "Istanbul", "Turkey", "34000", false);

        mockMvc.perform(post(BASE_URL, USER_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST addresses: bos street validation hatasi 400 dondurur")
    void createAddress_blankStreet_returns400() throws Exception {
        AddressRequest invalidRequest = new AddressRequest("Home", "", "Istanbul", "Turkey", "34000", false);

        mockMvc.perform(post(BASE_URL, USER_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST addresses: kimlik dogrulama yok 401 dondurur")
    void createAddress_noAuth_returns401() throws Exception {
        mockMvc.perform(post(BASE_URL, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ---- GET /api/users/{userId}/addresses ----

    @Test
    @DisplayName("GET addresses: tum adresler 200 dondurur")
    void getAllAddresses_success_returns200() throws Exception {
        AddressResponse address2 = AddressResponse.builder().id("address-id-2").title("Work").build();
        when(addressService.getAddressesByUserId(USER_ID)).thenReturn(List.of(addressResponse, address2));

        mockMvc.perform(get(BASE_URL, USER_ID)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET addresses: bos liste 200 dondurur")
    void getAllAddresses_empty_returns200() throws Exception {
        when(addressService.getAddressesByUserId(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL, USER_ID)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET addresses: kullanici bulunamadi 404 dondurur")
    void getAllAddresses_userNotFound_returns404() throws Exception {
        when(addressService.getAddressesByUserId("non-existent"))
                .thenThrow(new UserNotFoundException("non-existent", "id"));

        mockMvc.perform(get(BASE_URL, "non-existent")
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    // ---- GET /api/users/{userId}/addresses/{addressId} ----

    @Test
    @DisplayName("GET addresses/{addressId}: basarili getirme 200 dondurur")
    void getAddressById_success_returns200() throws Exception {
        when(addressService.getAddressById(USER_ID, ADDRESS_ID)).thenReturn(addressResponse);

        mockMvc.perform(get(BASE_URL + "/{addressId}", USER_ID, ADDRESS_ID)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ADDRESS_ID));
    }

    @Test
    @DisplayName("GET addresses/{addressId}: adres bulunamadi 404 dondurur")
    void getAddressById_notFound_returns404() throws Exception {
        when(addressService.getAddressById(USER_ID, "non-existent"))
                .thenThrow(new AddressNotFoundException("non-existent"));

        mockMvc.perform(get(BASE_URL + "/{addressId}", USER_ID, "non-existent")
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    // ---- GET /api/users/{userId}/addresses/default ----

    @Test
    @DisplayName("GET addresses/default: varsayilan adres 200 dondurur")
    void getDefaultAddress_success_returns200() throws Exception {
        when(addressService.getDefaultAddress(USER_ID)).thenReturn(addressResponse);

        mockMvc.perform(get(BASE_URL + "/default", USER_ID)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultAddress").value(true));
    }

    @Test
    @DisplayName("GET addresses/default: varsayilan adres yok 404 dondurur")
    void getDefaultAddress_notFound_returns404() throws Exception {
        when(addressService.getDefaultAddress(USER_ID))
                .thenThrow(new AddressNotFoundException("No default address found for user"));

        mockMvc.perform(get(BASE_URL + "/default", USER_ID)
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    // ---- PUT /api/users/{userId}/addresses/{addressId} ----

    @Test
    @DisplayName("PUT addresses/{addressId}: basarili guncelleme 200 dondurur")
    void updateAddress_success_returns200() throws Exception {
        when(addressService.updateAddress(eq(USER_ID), eq(ADDRESS_ID), any(AddressRequest.class)))
                .thenReturn(addressResponse);

        mockMvc.perform(put(BASE_URL + "/{addressId}", USER_ID, ADDRESS_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ADDRESS_ID));
    }

    @Test
    @DisplayName("PUT addresses/{addressId}: adres bulunamadi 404 dondurur")
    void updateAddress_notFound_returns404() throws Exception {
        when(addressService.updateAddress(eq(USER_ID), eq("non-existent"), any(AddressRequest.class)))
                .thenThrow(new AddressNotFoundException("non-existent"));

        mockMvc.perform(put(BASE_URL + "/{addressId}", USER_ID, "non-existent")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT addresses/{addressId}: gecersiz istek 400 dondurur")
    void updateAddress_invalidRequest_returns400() throws Exception {
        AddressRequest invalidRequest = new AddressRequest("", "Street", "City", "Country", "12345", false);

        mockMvc.perform(put(BASE_URL + "/{addressId}", USER_ID, ADDRESS_ID)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ---- DELETE /api/users/{userId}/addresses/{addressId} ----

    @Test
    @DisplayName("DELETE addresses/{addressId}: basarili silme 200 dondurur")
    void deleteAddress_success_returns200() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{addressId}", USER_ID, ADDRESS_ID)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Address deleted successfully"));

        verify(addressService).deleteAddress(USER_ID, ADDRESS_ID);
    }

    @Test
    @DisplayName("DELETE addresses/{addressId}: adres bulunamadi 404 dondurur")
    void deleteAddress_notFound_returns404() throws Exception {
        doThrow(new AddressNotFoundException("non-existent")).when(addressService).deleteAddress(USER_ID, "non-existent");

        mockMvc.perform(delete(BASE_URL + "/{addressId}", USER_ID, "non-existent")
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE addresses/{addressId}: kimlik dogrulama yok 401 dondurur")
    void deleteAddress_noAuth_returns401() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{addressId}", USER_ID, ADDRESS_ID))
                .andExpect(status().isUnauthorized());
    }

    // ---- PATCH /api/users/{userId}/addresses/{addressId}/set-default ----

    @Test
    @DisplayName("PATCH addresses/{addressId}/set-default: basarili 200 dondurur")
    void setDefaultAddress_success_returns200() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{addressId}/set-default", USER_ID, ADDRESS_ID)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Address set as default successfully"));

        verify(addressService).setDefaultAddress(USER_ID, ADDRESS_ID);
    }

    @Test
    @DisplayName("PATCH addresses/{addressId}/set-default: adres bulunamadi 404 dondurur")
    void setDefaultAddress_notFound_returns404() throws Exception {
        doThrow(new AddressNotFoundException("non-existent")).when(addressService).setDefaultAddress(USER_ID, "non-existent");

        mockMvc.perform(patch(BASE_URL + "/{addressId}/set-default", USER_ID, "non-existent")
                        .with(jwt()))
                .andExpect(status().isNotFound());
    }
}
