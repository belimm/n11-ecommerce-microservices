package com.n11bc.product_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Product creation request")
public record ProductCreateRequest(
        @Schema(example = "Cold-Pressed Olive Oil")
        @NotBlank(message = "Product name is required")
        @Size(max = 180, message = "Product name must be at most 180 characters")
        String name,

        @Schema(example = "cold-pressed-olive-oil")
        @NotBlank(message = "Product slug is required")
        @Size(max = 200, message = "Product slug must be at most 200 characters")
        String slug,

        @Schema(example = "Single-origin extra virgin olive oil with a bright, grassy finish.")
        @NotBlank(message = "Description is required")
        String description,

        @Schema(example = "249.90")
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal price,

        @Schema(example = "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5")
        @NotBlank(message = "Image URL is required")
        @Size(max = 1000, message = "Image URL must be at most 1000 characters")
        String imageUrl,

        @Schema(example = "true")
        Boolean active,

        @Schema(example = "herbal-pantry")
        @NotBlank(message = "Category slug is required")
        String categorySlug,

        @Valid
        List<LocalizedContentRequest> translations
) {
}
