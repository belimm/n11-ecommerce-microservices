package com.n11bc.cart_service.exception;

public class ProductUnavailableException extends RuntimeException {

    public ProductUnavailableException(Long productId) {
        super("Product is not available for cart operations: " + productId);
    }
}
