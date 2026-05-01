package com.n11bc.order_service.integration;

import com.n11bc.order_service.dto.request.CreateOrderRequest;
import com.n11bc.order_service.dto.response.CartItemSnapshotResponse;
import com.n11bc.order_service.dto.response.CartSnapshotResponse;
import com.n11bc.order_service.dto.response.UserAddressResponse;
import com.n11bc.order_service.entity.OrderStatus;
import com.n11bc.order_service.repository.OrderRepository;
import com.n11bc.order_service.service.CartClient;
import com.n11bc.order_service.service.CurrentUserService;
import com.n11bc.order_service.service.OrderEventPublisher;
import com.n11bc.order_service.service.OrderService;
import com.n11bc.order_service.service.UserAddressClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.config.import=optional:",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
class OrderServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @MockBean
    private CartClient cartClient;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private UserAddressClient userAddressClient;

    @MockBean
    private OrderEventPublisher eventPublisher;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
        registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBITMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ::getAdminPassword);
    }

    @TestConfiguration
    static class SecurityTestConfig {
        @Bean
        JwtDecoder jwtDecoder() {
            return token -> null;
        }
    }

    @Test
    void createOrder_persistsSnapshot() {
        when(currentUserService.getCurrentUserId()).thenReturn("user-1");
        when(currentUserService.getBearerToken()).thenReturn("token");
        when(cartClient.getCurrentCart("token")).thenReturn(cartSnapshot());
        when(userAddressClient.getAddress("user-1", "addr-1", "token")).thenReturn(addressResponse());

        var response = orderService.createOrder(createRequest());

        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(orderRepository.findById(response.id())).isPresent();
    }


    private CreateOrderRequest createRequest() {
        return new CreateOrderRequest("addr-1", "IYZICO");
    }

    private UserAddressResponse addressResponse() {
        return new UserAddressResponse("addr-1", "Home", "Main Street 1", "London", "United Kingdom", "NW1", true, null, null);
    }

    private CartSnapshotResponse cartSnapshot() {
        CartItemSnapshotResponse item = new CartItemSnapshotResponse(
                10L,
                100L,
                "Organic Tea",
                "https://cdn.example.com/tea.jpg",
                new BigDecimal("100.00"),
                2,
                new BigDecimal("200.00")
        );
        return new CartSnapshotResponse(1L, "user-1", "ACTIVE", List.of(item), new BigDecimal("200.00"), LocalDateTime.now(), null, null);
    }
}
