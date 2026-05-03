package com.n11bc.cart_service.integration;

import com.n11bc.cart_service.dto.request.AddCartItemRequest;
import com.n11bc.cart_service.dto.response.CartResponse;
import com.n11bc.cart_service.dto.response.ProductSnapshotResponse;
import com.n11bc.cart_service.entity.CartStatus;
import com.n11bc.cart_service.repository.CartRepository;
import com.n11bc.cart_service.service.CartService;
import com.n11bc.cart_service.service.CurrentUserService;
import com.n11bc.cart_service.service.ProductCatalogClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "app.cart.abandoned-scan-cron=0 0 0 31 12 *",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost/realms/test",
        "spring.main.allow-bean-definition-overriding=true",
        "app.jwt.secret=test-jwt-secret-key-that-is-at-least-32-bytes",
        "app.product-service.base-url=http://localhost:8082",
        "app.cart.abandoned-after-hours=24",
        "app.rabbitmq.cart-exchange=cart.exchange",
        "app.rabbitmq.abandoned-cart-queue=abandoned.cart.queue",
        "app.rabbitmq.abandoned-cart-routing-key=abandoned.cart"
})
class CartServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cart_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private ProductCatalogClient productCatalogClient;

    @BeforeEach
    void cleanDatabase() {
        cartRepository.deleteAll();
        when(currentUserService.getCurrentUserId()).thenReturn("user-1");
    }

    @Test
    @DisplayName("addItem persists product snapshot and increments existing item")
    void addItem_persistsSnapshotAndIncrements() {
        ProductSnapshotResponse product = new ProductSnapshotResponse(
                10L,
                "Calm Herbal Tea",
                "calm-herbal-tea",
                "Caffeine-free blend",
                new BigDecimal("99.90"),
                "https://example.com/tea.jpg",
                true
        );
        when(productCatalogClient.getProductSnapshot(10L, "en")).thenReturn(product);

        CartResponse first = cartService.addItem(new AddCartItemRequest(10L, 1), "en");
        CartResponse second = cartService.addItem(new AddCartItemRequest(10L, 2), "en");

        assertThat(first.id()).isNotNull();
        assertThat(second.items()).hasSize(1);
        assertThat(second.items().get(0).quantity()).isEqualTo(3);
        assertThat(second.totalPrice()).isEqualByComparingTo("299.70");
        assertThat(cartRepository.findByUserIdAndStatus("user-1", CartStatus.ACTIVE)).isPresent();
    }

    @TestConfiguration
    static class SecurityTestConfig {

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test-user")
                    .build();
        }
    }
}
