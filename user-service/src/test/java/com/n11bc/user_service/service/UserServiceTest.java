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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse userResponse;
    private SignupRequest signupRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("user-id-1")
                .username("testuser")
                .email("test@example.com")
                .password("encoded-password")
                .role(Role.CUSTOMER)
                .firstName("Test")
                .lastName("User")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        userResponse = UserResponse.builder()
                .id("user-id-1")
                .username("testuser")
                .email("test@example.com")
                .role(Role.CUSTOMER)
                .firstName("Test")
                .lastName("User")
                .active(true)
                .build();

        signupRequest = new SignupRequest("testuser", "test@example.com", "password123",
                "Test", "User", "5551234567", Role.CUSTOMER);
    }

    // ---- registerUser ----

    @Test
    @DisplayName("registerUser: basarili kayit")
    void registerUser_success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userMapper.signupRequestToUser(signupRequest)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.userToUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.registerUser(signupRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).save(argThat(savedUser -> savedUser.getRole() == Role.CUSTOMER && savedUser.isActive()));
    }

    @Test
    @DisplayName("registerUser: request role ADMIN olsa bile CUSTOMER olarak kaydedilir")
    void registerUser_adminRoleInRequest_ignored() {
        SignupRequest adminSignupRequest = new SignupRequest("adminlike", "adminlike@example.com", "password123",
                "Admin", "Like", "5551234567", Role.ADMIN);
        User mappedUser = User.builder()
                .username("adminlike")
                .email("adminlike@example.com")
                .password("encoded-password")
                .role(Role.ADMIN)
                .active(false)
                .build();

        when(userRepository.existsByUsername("adminlike")).thenReturn(false);
        when(userRepository.existsByEmail("adminlike@example.com")).thenReturn(false);
        when(userMapper.signupRequestToUser(adminSignupRequest)).thenReturn(mappedUser);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.userToUserResponse(any(User.class))).thenReturn(userResponse);

        userService.registerUser(adminSignupRequest);

        verify(userRepository).save(argThat(savedUser -> savedUser.getRole() == Role.CUSTOMER && savedUser.isActive()));
    }

    @Test
    @DisplayName("registerUser: kullanici adi zaten mevcut")
    void registerUser_usernameAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(signupRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser: email zaten mevcut")
    void registerUser_emailAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(signupRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
    }

    // ---- getUserById ----

    @Test
    @DisplayName("getUserById: basarili getirme")
    void getUserById_success() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(user));
        when(userMapper.userToUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById("user-id-1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("user-id-1");
    }

    @Test
    @DisplayName("getUserById: kullanici bulunamadi")
    void getUserById_notFound() {
        when(userRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById("non-existent-id"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("id");
    }

    // ---- getAllUsers ----

    @Test
    @DisplayName("getAllUsers: tum kullanicilari dondurur")
    void getAllUsers_success() {
        User user2 = User.builder().id("user-id-2").username("user2").build();
        when(userRepository.findAll()).thenReturn(List.of(user, user2));
        when(userMapper.userToUserResponse(any(User.class))).thenReturn(userResponse);

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getAllUsers: kayitli kullanici yoksa bos liste")
    void getAllUsers_empty() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).isEmpty();
    }

    // ---- updateUser ----

    @Test
    @DisplayName("updateUser: basarili guncelleme")
    void updateUser_success() {
        UpdateUserRequest request = new UpdateUserRequest("new@example.com", "NewName", "NewLast", "5559876543");
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.userToUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.updateUser("user-id-1", request);

        assertThat(result).isNotNull();
        verify(userMapper).updateUserFromRequest(request, user);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateUser: kullanici bulunamadi")
    void updateUser_notFound() {
        UpdateUserRequest request = new UpdateUserRequest("new@example.com", null, null, null);
        when(userRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser("non-existent-id", request))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser: ayni email ile guncelleme yapilabilir (kendi emaili)")
    void updateUser_sameEmail_noConflict() {
        UpdateUserRequest request = new UpdateUserRequest("test@example.com", null, null, null);
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.userToUserResponse(user)).thenReturn(userResponse);

        userService.updateUser("user-id-1", request);

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("updateUser: yeni email baska kullanicida mevcut")
    void updateUser_emailAlreadyExists() {
        UpdateUserRequest request = new UpdateUserRequest("taken@example.com", null, null, null);
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser("user-id-1", request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("updateUser: email null ise email kontrolu yapilmaz")
    void updateUser_nullEmail_skipsEmailCheck() {
        UpdateUserRequest request = new UpdateUserRequest(null, "NewFirst", null, null);
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.userToUserResponse(user)).thenReturn(userResponse);

        userService.updateUser("user-id-1", request);

        verify(userRepository, never()).existsByEmail(anyString());
    }


    // ---- changePassword ----

    @Test
    @DisplayName("changePassword: mevcut sifre dogruysa yeni sifre kaydedilir")
    void changePassword_success() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword123");
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-encoded-password");
        when(userRepository.save(user)).thenReturn(user);

        userService.changePassword("user-id-1", request);

        assertThat(user.getPassword()).isEqualTo("new-encoded-password");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("changePassword: mevcut sifre hataliysa exception firlatilir")
    void changePassword_wrongCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrongPassword", "newPassword123");
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("user-id-1", request))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword: kullanici bulunamadi")
    void changePassword_userNotFound() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword123");
        when(userRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword("missing-id", request))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ---- deleteUser ----

    @Test
    @DisplayName("deleteUser: basarili silme")
    void deleteUser_success() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(user));

        userService.deleteUser("user-id-1");

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("deleteUser: kullanici bulunamadi")
    void deleteUser_notFound() {
        when(userRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser("non-existent-id"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }

    // ---- activateUser ----

    @Test
    @DisplayName("activateUser: basarili aktivasyon")
    void activateUser_success() {
        user.setActive(false);
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.activateUser("user-id-1");

        assertThat(user.isActive()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("activateUser: kullanici bulunamadi")
    void activateUser_notFound() {
        when(userRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.activateUser("non-existent-id"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ---- deactivateUser ----

    @Test
    @DisplayName("deactivateUser: basarili deaktivasyon")
    void deactivateUser_success() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.deactivateUser("user-id-1");

        assertThat(user.isActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("deactivateUser: kullanici bulunamadi")
    void deactivateUser_notFound() {
        when(userRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser("non-existent-id"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ---- findByUsername ----

    @Test
    @DisplayName("findByUsername: basarili arama")
    void findByUsername_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = userService.findByUsername("testuser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("findByUsername: kullanici bulunamadi")
    void findByUsername_notFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("unknown"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("username");
    }


    // ---- findByEmail ----

    @Test
    @DisplayName("findByEmail: basarili arama")
    void findByEmail_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("findByEmail: kullanici bulunamadi")
    void findByEmail_notFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("unknown@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("email");
    }

    // ---- findByUsernameOrEmail ----

    @Test
    @DisplayName("findByUsernameOrEmail: username ile basarili arama")
    void findByUsernameOrEmail_byUsername_success() {
        when(userRepository.findByUsernameOrEmail("testuser", "testuser")).thenReturn(Optional.of(user));

        User result = userService.findByUsernameOrEmail("testuser");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("findByUsernameOrEmail: bulunamadi")
    void findByUsernameOrEmail_notFound() {
        when(userRepository.findByUsernameOrEmail("unknown", "unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsernameOrEmail("unknown"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ---- verifyPassword ----

    @Test
    @DisplayName("verifyPassword: dogru sifre")
    void verifyPassword_correct() {
        when(passwordEncoder.matches("rawPassword", "encoded-password")).thenReturn(true);

        boolean result = userService.verifyPassword(user, "rawPassword");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verifyPassword: yanlis sifre")
    void verifyPassword_wrong() {
        when(passwordEncoder.matches("wrongPassword", "encoded-password")).thenReturn(false);

        boolean result = userService.verifyPassword(user, "wrongPassword");

        assertThat(result).isFalse();
    }
}
