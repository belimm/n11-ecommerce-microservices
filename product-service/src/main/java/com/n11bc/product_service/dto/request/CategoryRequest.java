package com.n11bc.product_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Category create or update request")
public record CategoryRequest(
        @Schema(example = "Electronics")
        @NotBlank(message = "Category name is required")
        @Size(max = 120, message = "Category name must be at most 120 characters")
        String name,

        @Schema(example = "electronics")
        @NotBlank(message = "Category slug is required")
        @Size(max = 140, message = "Category slug must be at most 140 characters")
        String slug,

        @Schema(example = "Phones, computers, wearables and smart devices")
        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        @Valid
        List<LocalizedContentRequest> translations
) {
}
