package com.n11bc.order_service.service;

import com.n11bc.order_service.dto.response.CartSnapshotResponse;

/**
 * Reads the authenticated user's cart snapshot from cart-service.
 */
public interface CartClient {

    /**
     * Loads the current active cart for the token owner.
     *
     * @param bearerToken JWT bearer token value
     * @return current cart snapshot
     */
    CartSnapshotResponse getCurrentCart(String bearerToken);
}
