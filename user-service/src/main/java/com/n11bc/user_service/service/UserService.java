package com.n11bc.user_service.service;

import com.n11bc.user_service.dto.request.ChangePasswordRequest;
import com.n11bc.user_service.dto.request.SignupRequest;
import com.n11bc.user_service.dto.request.UpdateUserRequest;
import com.n11bc.user_service.dto.response.UserResponse;
import com.n11bc.user_service.entity.Role;
import com.n11bc.user_service.entity.User;
import com.n11bc.user_service.exception.InvalidPasswordException;
import com.n11bc.user_service.exception.UserAlreadyExistsException;
import com.n11bc.user_service.exception.UserNotFoundException;
import com.n11bc.user_service.mapper.UserMapper;
import com.n11bc.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse registerUser(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        User user = userMapper.signupRequestToUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setActive(true);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        return userMapper.userToUserResponse(savedUser);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username, "username"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email, "email"));
    }

    public User findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with username or email: " + usernameOrEmail));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id, "id"));
        return userMapper.userToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::userToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id, "id"));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
            }
        }

        userMapper.updateUserFromRequest(request, user);

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getUsername());

        return userMapper.userToUserResponse(updatedUser);
    }


    @Transactional
    public void changePassword(String id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id, "id"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", user.getUsername());
    }

    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id, "id"));

        userRepository.delete(user);
        log.info("User deleted successfully: {}", user.getUsername());
    }

    @Transactional
    public void activateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id, "id"));
        user.setActive(true);
        userRepository.save(user);
        log.info("User activated: {}", user.getUsername());
    }

    @Transactional
    public void deactivateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id, "id"));
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", user.getUsername());
    }

    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}
