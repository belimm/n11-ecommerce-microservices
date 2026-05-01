package com.n11bc.stock_service.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long productId, int requestedQuantity, int availableQuantity) {
        super("Insufficient stock for product " + productId + ". Requested: " + requestedQuantity + ", available: " + availableQuantity);
    }
}
