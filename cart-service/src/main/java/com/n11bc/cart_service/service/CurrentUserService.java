package com.n11bc.cart_service.service;

public interface CurrentUserService {

    /**
     * Returns the authenticated user's stable Keycloak subject.
     */
    String getCurrentUserId();
}
