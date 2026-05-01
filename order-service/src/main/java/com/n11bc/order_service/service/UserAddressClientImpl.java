package com.n11bc.order_service.service;

import com.n11bc.order_service.dto.response.UserAddressResponse;
import com.n11bc.order_service.exception.UserAddressUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserAddressClientImpl implements UserAddressClient {

    private final RestTemplate restTemplate;

    @Value("${app.user-service.base-url}")
    private String userServiceBaseUrl;

    @Override
    public UserAddressResponse getAddress(String userId, String addressId, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<UserAddressResponse> response = restTemplate.exchange(
                    userServiceBaseUrl + "/api/users/{userId}/addresses/{addressId}",
                    HttpMethod.GET,
                    entity,
                    UserAddressResponse.class,
                    userId,
                    addressId
            );
            if (response.getBody() == null) {
                throw new UserAddressUnavailableException("User address service returned an empty response");
            }
            return response.getBody();
        } catch (RestClientException ex) {
            throw new UserAddressUnavailableException("User address is unavailable: " + ex.getMessage());
        }
    }
}
