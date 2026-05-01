package com.n11bc.stock_service.dto.request;

import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(
        @NotNull Integer delta
) {
}
