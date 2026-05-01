package com.n11bc.cart_service.exception;

public class CartNotFoundException extends RuntimeException {

    public CartNotFoundException(String userId) {
        super("Active cart not found for user: " + userId);
    }
}
