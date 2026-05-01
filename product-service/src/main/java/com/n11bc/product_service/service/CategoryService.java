package com.n11bc.product_service.service;

import com.n11bc.product_service.dto.request.CategoryRequest;
import com.n11bc.product_service.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    /**
     * Returns all categories localized by the Accept-Language header.
     */
    List<CategoryResponse> getAllCategories(String acceptLanguage);

    /**
     * Returns a single category by id localized by the Accept-Language header.
     */
    CategoryResponse getCategoryById(Long id, String acceptLanguage);

    /**
     * Creates a category and its optional localized translations.
     */
    CategoryResponse createCategory(CategoryRequest request, String acceptLanguage);

    /**
     * Updates category fields and replaces translations when provided.
     */
    CategoryResponse updateCategory(Long id, CategoryRequest request, String acceptLanguage);

    /**
     * Deletes a category if no products are assigned to it.
     */
    void deleteCategory(Long id);
}
