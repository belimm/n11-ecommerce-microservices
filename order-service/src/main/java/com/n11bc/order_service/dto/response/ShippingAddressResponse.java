package com.n11bc.order_service.dto.response;

public record ShippingAddressResponse(
        String sourceAddressId,
        String title,
        String street,
        String city,
        String country,
        String zipCode
) {
}
