package com.n11bc.product_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Product update request. Null fields are ignored.")
public record ProductUpdateRequest(
        @Schema(example = "Samsung Galaxy S24 Ultra 256 GB")
        @Size(max = 180, message = "Product name must be at most 180 characters")
        String name,

        @Schema(example = "galaxy-s24-ultra-256gb")
        @Size(max = 200, message = "Product slug must be at most 200 characters")
        String slug,

        @Schema(example = "Titanium frame smartphone with AI camera tools and S Pen support.")
        String description,

        @Schema(example = "64999.00")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal price,

        @Schema(example = "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf")
        @Size(max = 1000, message = "Image URL must be at most 1000 characters")
        String imageUrl,

        @Schema(example = "true")
        Boolean active,

        @Schema(example = "electronics")
        String categorySlug,

        @Valid
        List<LocalizedContentRequest> translations
) {
}
