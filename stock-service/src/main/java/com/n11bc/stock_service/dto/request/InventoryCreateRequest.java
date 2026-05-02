package com.n11bc.stock_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryCreateRequest(
        @NotNull Long productId,
        @Min(0) int availableQuantity
) {
}
