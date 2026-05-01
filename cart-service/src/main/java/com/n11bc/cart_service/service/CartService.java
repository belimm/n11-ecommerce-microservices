package com.n11bc.cart_service.service;

import com.n11bc.cart_service.dto.request.AddCartItemRequest;
import com.n11bc.cart_service.dto.request.UpdateCartItemRequest;
import com.n11bc.cart_service.dto.response.CartResponse;

public interface CartService {

    /**
     * Returns the authenticated user's active cart or creates a new active cart.
     */
    CartResponse getCurrentCart();

    /**
     * Adds a product to the current user's cart, incrementing quantity when the item already exists.
     */
    CartResponse addItem(AddCartItemRequest request, String acceptLanguage);

    /**
     * Updates an existing cart item's quantity.
     */
    CartResponse updateItem(Long productId, UpdateCartItemRequest request);

    /**
     * Removes an existing item from the current user's cart.
     */
    CartResponse removeItem(Long productId);

    /**
     * Clears all items from the current user's active cart.
     */
    CartResponse clearCart();
}
