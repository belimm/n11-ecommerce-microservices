package com.n11bc.order_service.exception;

public class UserAddressUnavailableException extends RuntimeException {

    public UserAddressUnavailableException(String message) {
        super(message);
    }
}
