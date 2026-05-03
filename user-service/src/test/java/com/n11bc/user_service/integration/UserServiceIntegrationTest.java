package com.n11bc.user_service.integration;

import com.n11bc.user_service.dto.request.AddressRequest;
import com.n11bc.user_service.dto.request.SignupRequest;
import com.n11bc.user_service.dto.response.AddressResponse;
import com.n11bc.user_service.dto.response.UserResponse;
import com.n11bc.user_service.entity.RefreshToken;
import com.n11bc.user_service.entity.Role;
import com.n11bc.user_service.entity.User;
import com.n11bc.user_service.repository.AddressRepository;
import com.n11bc.user_service.repository.RefreshTokenRepository;
import com.n11bc.user_service.repository.UserRepository;
import com.n11bc.user_service.service.AddressService;
import com.n11bc.user_service.service.RefreshTokenService;
import com.n11bc.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = {
        "spring.config.import=",
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "app.jwt.secret=test-jwt-secret-key-that-is-at-least-32-bytes",
        "app.jwt.expiration-ms=3600000",
        "app.refresh-token.expiration-ms=604800000"
})
class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("registerUser persists an active CUSTOMER regardless of request role")
    void registerUser_persistsCustomerAccount() {
        SignupRequest request = new SignupRequest(
                "berk",
                "berk@example.com",
                "secret123",
                "Berk",
                "Limoncu",
                "+905551112233",
                Role.ADMIN
        );

        UserResponse response = userService.registerUser(request);

        User persisted = userRepository.findById(response.getId()).orElseThrow();
        assertThat(persisted.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(persisted.isActive()).isTrue();
        assertThat(persisted.getPassword()).isNotEqualTo("secret123");
    }

    @Test
    @DisplayName("address service promotes first address and switches default address")
    void addressService_managesDefaultAddress() {
        UserResponse user = userService.registerUser(new SignupRequest(
                "customer",
                "customer@example.com",
                "secret123",
                "Customer",
                "One",
                null,
                Role.CUSTOMER
        ));

        AddressResponse home = addressService.createAddress(user.getId(), new AddressRequest(
                "Home",
                "Ataturk Street 10",
                "Istanbul",
                "Turkey",
                "34000",
                false
        ));
        AddressResponse office = addressService.createAddress(user.getId(), new AddressRequest(
                "Office",
                "Maslak Avenue 20",
                "Istanbul",
                "Turkey",
                "34398",
                true
        ));

        List<AddressResponse> addresses = addressService.getAddressesByUserId(user.getId());
        AddressResponse defaultAddress = addressService.getDefaultAddress(user.getId());

        assertThat(addresses).hasSize(2);
        assertThat(home.isDefaultAddress()).isTrue();
        assertThat(defaultAddress.getId()).isEqualTo(office.getId());
        assertThat(defaultAddress.isDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("refresh token service replaces the user's existing refresh token")
    void refreshTokenService_replacesExistingToken() {
        UserResponse response = userService.registerUser(new SignupRequest(
                "tokenuser",
                "token@example.com",
                "secret123",
                "Token",
                "User",
                null,
                Role.CUSTOMER
        ));
        User user = userRepository.findById(response.getId()).orElseThrow();

        RefreshToken first = refreshTokenService.createOrUpdateRefreshToken(user, "first-token");
        RefreshToken second = refreshTokenService.createOrUpdateRefreshToken(user, "second-token");

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(second.getToken()).isEqualTo("second-token");
        assertThat(refreshTokenRepository.findAll()).hasSize(1);
        assertThat(refreshTokenService.findByToken("first-token")).isEmpty();
        assertThat(refreshTokenService.findByToken("second-token")).isPresent();
    }
}
