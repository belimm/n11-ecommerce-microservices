package com.n11bc.order_service.dto.request;

import com.n11bc.order_service.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull OrderStatus status
) {
}
