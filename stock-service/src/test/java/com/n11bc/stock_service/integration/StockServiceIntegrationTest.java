package com.n11bc.stock_service.integration;

import com.n11bc.stock_service.dto.request.InventoryCreateRequest;
import com.n11bc.stock_service.event.OrderCreatedEvent;
import com.n11bc.stock_service.entity.StockReservationStatus;
import com.n11bc.stock_service.repository.InventoryRepository;
import com.n11bc.stock_service.repository.StockReservationRepository;
import com.n11bc.stock_service.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
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

@SpringBootTest(properties = {
        "spring.config.import=optional:",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
class StockServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockReservationRepository reservationRepository;

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
    void reserveAndReleaseStock_persistsInventoryAndReservationChanges() {
        inventoryService.createInventory(new InventoryCreateRequest(10L, 20));

        inventoryService.reserveStock(orderCreatedEvent());

        var reservedInventory = inventoryRepository.findByProductId(10L).orElseThrow();
        assertThat(reservedInventory.getAvailableQuantity()).isEqualTo(18);
        assertThat(reservedInventory.getReservedQuantity()).isEqualTo(2);
        assertThat(reservationRepository.findByOrderId(100L)).hasSize(1);

        inventoryService.releaseStock(100L, "payment failed");

        var releasedInventory = inventoryRepository.findByProductId(10L).orElseThrow();
        assertThat(releasedInventory.getAvailableQuantity()).isEqualTo(20);
        assertThat(releasedInventory.getReservedQuantity()).isZero();
        assertThat(reservationRepository.findByOrderId(100L).getFirst().getStatus()).isEqualTo(StockReservationStatus.RELEASED);
    }

    private OrderCreatedEvent orderCreatedEvent() {
        return new OrderCreatedEvent(
                100L,
                "ORD-ABC",
                "user-1",
                new BigDecimal("200.00"),
                null,
                List.of(new OrderCreatedEvent.OrderCreatedItem(10L, "Organic Tea", 2, new BigDecimal("100.00"), new BigDecimal("200.00"))),
                LocalDateTime.now()
        );
    }
}
