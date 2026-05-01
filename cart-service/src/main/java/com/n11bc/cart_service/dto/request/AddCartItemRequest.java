package com.n11bc.cart_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Add product to cart request")
public record AddCartItemRequest(
        @Schema(example = "10")
        @NotNull(message = "Product id is required")
        Long productId,

        @Schema(example = "2")
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
}
