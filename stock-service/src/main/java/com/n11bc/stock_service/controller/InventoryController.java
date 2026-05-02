package com.n11bc.stock_service.controller;

import com.n11bc.stock_service.dto.request.InventoryCreateRequest;
import com.n11bc.stock_service.dto.request.InventoryUpdateRequest;
import com.n11bc.stock_service.dto.request.StockAdjustmentRequest;
import com.n11bc.stock_service.dto.response.ErrorResponse;
import com.n11bc.stock_service.dto.response.InventoryResponse;
import com.n11bc.stock_service.dto.response.PageResponse;
import com.n11bc.stock_service.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory and stock management")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Create inventory", description = "Creates inventory for a product.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inventory created", content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Inventory already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody InventoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createInventory(request));
    }

    @Operation(summary = "Update inventory", description = "Replaces available quantity for a product while keeping reserved quantity unchanged.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory updated", content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inventory not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.updateInventory(productId, request));
    }

    @Operation(summary = "Adjust stock", description = "Applies a positive or negative delta to available stock.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock adjusted", content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid adjustment", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inventory not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{productId}/adjust")
    public ResponseEntity<InventoryResponse> adjustStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(productId, request));
    }

    @Operation(summary = "Get inventory", description = "Returns inventory by product id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory found", content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inventory not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }

    @Operation(summary = "List inventories", description = "Returns paginated inventory records.")
    @ApiResponse(responseCode = "200", description = "Inventory page")
    @GetMapping
    public ResponseEntity<PageResponse<InventoryResponse>> getInventories(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "productId"));
        return ResponseEntity.ok(PageResponse.from(inventoryService.getInventories(pageRequest)));
    }
}
