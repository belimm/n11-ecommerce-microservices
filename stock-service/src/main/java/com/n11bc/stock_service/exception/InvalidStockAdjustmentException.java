package com.n11bc.stock_service.exception;

public class InvalidStockAdjustmentException extends RuntimeException {

    public InvalidStockAdjustmentException(String message) {
        super(message);
    }
}
