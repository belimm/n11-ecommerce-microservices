package com.n11bc.cart_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "Update cart item quantity request")
public record UpdateCartItemRequest(
        @Schema(example = "3")
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
}
