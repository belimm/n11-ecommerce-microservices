package com.n11bc.order_service.exception;

public class ForbiddenOrderAccessException extends RuntimeException {

    public ForbiddenOrderAccessException(Long orderId) {
        super("Current user cannot access order: " + orderId);
    }
}
