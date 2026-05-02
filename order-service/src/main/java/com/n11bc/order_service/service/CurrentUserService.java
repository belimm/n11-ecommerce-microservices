package com.n11bc.order_service.service;

/**
 * Resolves the authenticated user from the current JWT security context.
 */
public interface CurrentUserService {

    /**
     * Returns the authenticated Keycloak subject.
     *
     * @return current user id
     */
    String getCurrentUserId();

    /**
     * Returns the current bearer token value for downstream service calls.
     *
     * @return bearer token value
     */
    String getBearerToken();

    /**
     * Checks whether the current user has administrator authority.
     *
     * @return true when current user is an administrator
     */
    boolean isAdmin();
}
