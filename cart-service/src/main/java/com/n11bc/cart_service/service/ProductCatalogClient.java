package com.n11bc.cart_service.service;

import com.n11bc.cart_service.dto.response.ProductSnapshotResponse;
import com.n11bc.cart_service.exception.ProductUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ProductCatalogClient {

    private final RestTemplate restTemplate;

    @Value("${app.product-service.base-url}")
    private String productServiceBaseUrl;

    public ProductSnapshotResponse getProductSnapshot(Long productId, String acceptLanguage) {
        HttpHeaders headers = new HttpHeaders();
        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            headers.set(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage);
        }
        ResponseEntity<ProductSnapshotResponse> response = restTemplate.exchange(
                productServiceBaseUrl + "/api/products/" + productId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ProductSnapshotResponse.class
        );
        ProductSnapshotResponse product = response.getBody();
        if (product == null || !product.active()) {
            throw new ProductUnavailableException(productId);
        }
        return product;
    }
}
