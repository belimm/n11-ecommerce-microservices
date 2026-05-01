package com.n11bc.cart_service.exception;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(Long productId) {
        super("Cart item not found for product id: " + productId);
    }
}
