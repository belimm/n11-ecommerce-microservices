package com.n11bc.user_service.service;

import com.n11bc.user_service.dto.request.ChangePasswordRequest;
import com.n11bc.user_service.dto.request.SignupRequest;
import com.n11bc.user_service.dto.request.UpdateUserRequest;
import com.n11bc.user_service.dto.response.UserResponse;
import com.n11bc.user_service.entity.User;

import java.util.List;

public interface UserService {

    /**
     * Registers a new customer account and ignores any role value supplied by the client.
     */
    UserResponse registerUser(SignupRequest request);

    /**
     * Finds a user by username or throws when no matching user exists.
     */
    User findByUsername(String username);

    /**
     * Finds a user by email or throws when no matching user exists.
     */
    User findByEmail(String email);

    /**
     * Finds a user by username or email for authentication and token flows.
     */
    User findByUsernameOrEmail(String usernameOrEmail);

    /**
     * Returns a user profile by id.
     */
    UserResponse getUserById(String id);

    /**
     * Returns every user profile for admin screens.
     */
    List<UserResponse> getAllUsers();

    /**
     * Updates editable profile fields for a user.
     */
    UserResponse updateUser(String id, UpdateUserRequest request);

    /**
     * Changes a user's password after validating the current password.
     */
    void changePassword(String id, ChangePasswordRequest request);

    /**
     * Deletes a user account.
     */
    void deleteUser(String id);

    /**
     * Marks a user account as active.
     */
    void activateUser(String id);

    /**
     * Marks a user account as inactive.
     */
    void deactivateUser(String id);

    /**
     * Verifies a raw password against the stored encoded password.
     */
    boolean verifyPassword(User user, String rawPassword);
}
