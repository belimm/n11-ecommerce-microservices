package com.n11bc.cart_service.controller;

import com.n11bc.cart_service.dto.request.AddCartItemRequest;
import com.n11bc.cart_service.dto.request.UpdateCartItemRequest;
import com.n11bc.cart_service.dto.response.CartResponse;
import com.n11bc.cart_service.dto.response.ErrorResponse;
import com.n11bc.cart_service.dto.response.MessageResponse;
import com.n11bc.cart_service.service.CartService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Authenticated shopping cart management")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get current cart", description = "Returns the authenticated user's active cart or creates one.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current cart", content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CartResponse> getCurrentCart() {
        return ResponseEntity.ok(cartService.getCurrentCart());
    }

    @Operation(summary = "Add item", description = "Adds a product to cart and stores a product snapshot. Existing item quantity is incremented.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item added", content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product unavailable", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody AddCartItemRequest request,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(request, acceptLanguage));
    }

    @Operation(summary = "Update item quantity", description = "Updates an existing cart item's quantity.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item updated", content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cart item not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(productId, request));
    }

    @Operation(summary = "Remove item", description = "Removes a product from the authenticated user's active cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item removed", content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cart item not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(productId));
    }

    @Operation(summary = "Clear cart", description = "Removes all items from the authenticated user's active cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart cleared", content = @Content(schema = @Schema(implementation = CartResponse.class)))
    })
    @DeleteMapping("/items")
    public ResponseEntity<CartResponse> clearCart() {
        return ResponseEntity.ok(cartService.clearCart());
    }

    @Operation(summary = "Health message", description = "Simple authenticated cart service message for Swagger checks.")
    @ApiResponse(responseCode = "200", description = "Message", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    @GetMapping("/message")
    public ResponseEntity<MessageResponse> message() {
        return ResponseEntity.ok(new MessageResponse("Cart service is ready"));
    }
}
