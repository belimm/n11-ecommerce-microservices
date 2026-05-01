package com.n11bc.user_service.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String value, String field) {
        super(String.format("User not found with %s: %s", field, value));
    }
}
