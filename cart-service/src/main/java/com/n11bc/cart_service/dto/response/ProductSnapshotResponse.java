package com.n11bc.cart_service.dto.response;

import java.math.BigDecimal;

public record ProductSnapshotResponse(
        Long id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        String imageUrl,
        boolean active
) {
}
