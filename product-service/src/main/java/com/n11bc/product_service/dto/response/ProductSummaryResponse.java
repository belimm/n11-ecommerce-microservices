package com.n11bc.product_service.dto.response;

import java.math.BigDecimal;

public record ProductSummaryResponse(
        Long id,
        String name,
        String slug,
        BigDecimal price,
        String imageUrl,
        boolean active,
        String locale,
        CategoryResponse category
) {
}
