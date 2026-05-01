package com.n11bc.cart_service.exception;

public class InvalidCartOperationException extends RuntimeException {

    public InvalidCartOperationException(String message) {
        super(message);
    }
}
