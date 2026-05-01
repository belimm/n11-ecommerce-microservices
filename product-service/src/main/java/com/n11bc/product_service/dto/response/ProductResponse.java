package com.n11bc.product_service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        String imageUrl,
        boolean active,
        String locale,
        CategoryResponse category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
