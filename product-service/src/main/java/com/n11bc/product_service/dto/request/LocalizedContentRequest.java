package com.n11bc.product_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Localized text for product or category content")
public record LocalizedContentRequest(
        @Schema(example = "tr")
        @NotBlank(message = "Locale is required")
        @Size(max = 10, message = "Locale must be at most 10 characters")
        String locale,

        @Schema(example = "Soguk Sikim Zeytinyagi")
        @NotBlank(message = "Localized name is required")
        String name,

        @Schema(example = "Tek bahceden uretilmis naturel sizma zeytinyagi.")
        @NotBlank(message = "Localized description is required")
        String description
) {
}
