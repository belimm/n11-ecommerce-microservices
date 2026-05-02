package com.n11bc.order_service.service;

import com.n11bc.order_service.dto.response.UserAddressResponse;

/**
 * Reads and validates user addresses from user-service.
 */
public interface UserAddressClient {

    /**
     * Loads an address owned by the given user.
     *
     * @param userId address owner id
     * @param addressId selected address id
     * @param bearerToken JWT bearer token value
     * @return selected address
     */
    UserAddressResponse getAddress(String userId, String addressId, String bearerToken);
}
