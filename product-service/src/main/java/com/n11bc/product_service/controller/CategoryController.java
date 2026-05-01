package com.n11bc.product_service.controller;

import com.n11bc.product_service.dto.request.CategoryRequest;
import com.n11bc.product_service.dto.response.CategoryResponse;
import com.n11bc.product_service.dto.response.ErrorResponse;
import com.n11bc.product_service.dto.response.MessageResponse;
import com.n11bc.product_service.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Localized product category management")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "List categories", description = "Returns categories localized by the Accept-Language header.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        return ResponseEntity.ok(categoryService.getAllCategories(acceptLanguage));
    }

    @Operation(summary = "Get category by id", description = "Returns a single category localized by the Accept-Language header.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category detail", content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        return ResponseEntity.ok(categoryService.getCategoryById(id, acceptLanguage));
    }

    @Operation(summary = "Create category", description = "Creates a category with optional localized translations.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created", content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Slug conflict", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request, acceptLanguage));
    }

    @Operation(summary = "Update category", description = "Updates a category and replaces translations when translations are provided.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated", content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Slug conflict", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request, acceptLanguage));
    }

    @Operation(summary = "Delete category", description = "Deletes a category when no products are assigned to it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category deleted", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Category has products", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new MessageResponse("Category deleted successfully"));
    }
}
