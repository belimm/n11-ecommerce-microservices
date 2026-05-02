package com.n11bc.stock_service.exception;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(Long productId) {
        super("Inventory not found for product: " + productId);
    }
}
