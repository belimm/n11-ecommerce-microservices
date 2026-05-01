package com.n11bc.order_service.exception;

import com.n11bc.order_service.entity.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {

    public InvalidOrderStatusTransitionException(OrderStatus current, OrderStatus target) {
        super("Cannot transition order from " + current + " to " + target);
    }
}
