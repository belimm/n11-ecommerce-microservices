package com.n11bc.order_service.service;

import com.n11bc.order_service.dto.response.CartSnapshotResponse;
import com.n11bc.order_service.exception.CartUnavailableException;
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
public class CartClientImpl implements CartClient {

    private final RestTemplate restTemplate;

    @Value("${app.cart-service.base-url}")
    private String cartServiceBaseUrl;

    @Override
    public CartSnapshotResponse getCurrentCart(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<CartSnapshotResponse> response = restTemplate.exchange(
                    cartServiceBaseUrl + "/api/cart",
                    HttpMethod.GET,
                    entity,
                    CartSnapshotResponse.class
            );
            if (response.getBody() == null) {
                throw new CartUnavailableException("Cart service returned an empty response");
            }
            return response.getBody();
        } catch (RestClientException ex) {
            throw new CartUnavailableException("Cart service is unavailable: " + ex.getMessage());
        }
    }
}
