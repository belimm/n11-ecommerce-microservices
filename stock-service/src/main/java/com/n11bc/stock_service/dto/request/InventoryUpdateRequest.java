package com.n11bc.stock_service.dto.request;

import jakarta.validation.constraints.Min;

public record InventoryUpdateRequest(
        @Min(0) int availableQuantity
) {
}
