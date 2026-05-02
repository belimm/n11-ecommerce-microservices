package com.n11bc.order_service.dto.response;

import java.time.LocalDateTime;

public record UserAddressResponse(
        String id,
        String title,
        String street,
        String city,
        String country,
        String zipCode,
        boolean defaultAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
