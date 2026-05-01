package com.n11bc.product_service.dto.response;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        String locale,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
