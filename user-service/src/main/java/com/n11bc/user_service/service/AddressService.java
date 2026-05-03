package com.n11bc.user_service.service;

import com.n11bc.user_service.dto.request.AddressRequest;
import com.n11bc.user_service.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {

    /**
     * Creates an address for a user and marks it as default when needed.
     */
    AddressResponse createAddress(String userId, AddressRequest request);

    /**
     * Returns all addresses that belong to a user.
     */
    List<AddressResponse> getAddressesByUserId(String userId);

    /**
     * Returns a single address owned by a user.
     */
    AddressResponse getAddressById(String userId, String addressId);

    /**
     * Returns the user's default address.
     */
    AddressResponse getDefaultAddress(String userId);

    /**
     * Updates an existing user address.
     */
    AddressResponse updateAddress(String userId, String addressId, AddressRequest request);

    /**
     * Deletes an address and promotes another address as default when necessary.
     */
    void deleteAddress(String userId, String addressId);

    /**
     * Marks one user address as default and clears the previous default flag.
     */
    void setDefaultAddress(String userId, String addressId);
}
