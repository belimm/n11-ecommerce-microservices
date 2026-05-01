package com.n11bc.product_service.controller;

import com.n11bc.product_service.dto.request.ProductCreateRequest;
import com.n11bc.product_service.dto.request.ProductUpdateRequest;
import com.n11bc.product_service.dto.response.ErrorResponse;
import com.n11bc.product_service.dto.response.MessageResponse;
import com.n11bc.product_service.dto.response.PageResponse;
import com.n11bc.product_service.dto.response.ProductResponse;
import com.n11bc.product_service.dto.response.ProductSummaryResponse;
import com.n11bc.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Localized product catalog management")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "List products", description = "Returns active products with pagination, category filtering, and Accept-Language localization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product page", content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PageResponse<ProductSummaryResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String categorySlug,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        return ResponseEntity.ok(productService.getProducts(page, size, categorySlug, acceptLanguage));
    }

    @Operation(summary = "Get product by id", description = "Returns product detail localized by the Accept-Language header.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product detail", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        return ResponseEntity.ok(productService.getProductById(id, acceptLanguage));
    }

    @Operation(summary = "Get product by slug", description = "Returns product detail by stable slug localized by the Accept-Language header.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product detail", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(
            @PathVariable String slug,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        return ResponseEntity.ok(productService.getProductBySlug(slug, acceptLanguage));
    }

    @Operation(summary = "Create product", description = "Creates a product with optional localized translations.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Slug conflict", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request, acceptLanguage));
    }

    @Operation(summary = "Update product", description = "Updates a product and replaces translations when translations are provided.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product or category not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Slug conflict", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        return ResponseEntity.ok(productService.updateProduct(id, request, acceptLanguage));
    }

    @Operation(summary = "Delete product", description = "Deletes a product by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product deleted", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new MessageResponse("Product deleted successfully"));
    }
}
