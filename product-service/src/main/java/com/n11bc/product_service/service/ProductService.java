package com.n11bc.product_service.service;

import com.n11bc.product_service.dto.request.ProductCreateRequest;
import com.n11bc.product_service.dto.request.ProductUpdateRequest;
import com.n11bc.product_service.dto.response.PageResponse;
import com.n11bc.product_service.dto.response.ProductResponse;
import com.n11bc.product_service.dto.response.ProductSummaryResponse;

public interface ProductService {

    /**
     * Returns active products with server-side pagination and optional category filtering.
     */
    PageResponse<ProductSummaryResponse> getProducts(int page, int size, String categorySlug, String acceptLanguage);

    /**
     * Returns a product detail by id localized by the Accept-Language header.
     */
    ProductResponse getProductById(Long id, String acceptLanguage);

    /**
     * Returns a product detail by slug localized by the Accept-Language header.
     */
    ProductResponse getProductBySlug(String slug, String acceptLanguage);

    /**
     * Creates a product with optional localized translations.
     */
    ProductResponse createProduct(ProductCreateRequest request, String acceptLanguage);

    /**
     * Updates product fields and replaces translations when provided.
     */
    ProductResponse updateProduct(Long id, ProductUpdateRequest request, String acceptLanguage);

    /**
     * Deletes a product by id.
     */
    void deleteProduct(Long id);
}
