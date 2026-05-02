package com.n11bc.stock_service.dto.response;

import java.time.LocalDateTime;

public record InventoryResponse(
        Long id,
        Long productId,
        int availableQuantity,
        int reservedQuantity,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
