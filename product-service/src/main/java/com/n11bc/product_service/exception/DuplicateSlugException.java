package com.n11bc.product_service.exception;

public class DuplicateSlugException extends RuntimeException {

    public DuplicateSlugException(String resource, String slug) {
        super(resource + " already exists with slug: " + slug);
    }
}
