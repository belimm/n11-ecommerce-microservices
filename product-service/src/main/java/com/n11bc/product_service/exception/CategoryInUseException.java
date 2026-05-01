package com.n11bc.product_service.exception;

public class CategoryInUseException extends RuntimeException {

    public CategoryInUseException(Long categoryId) {
        super("Category cannot be deleted while products are assigned to it: " + categoryId);
    }
}
