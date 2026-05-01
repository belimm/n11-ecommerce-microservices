package com.n11bc.product_service.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }

    public ProductNotFoundException(String slug) {
        super("Product not found with slug: " + slug);
    }
}
