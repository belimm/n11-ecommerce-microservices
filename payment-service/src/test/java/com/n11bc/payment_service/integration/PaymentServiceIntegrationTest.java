package com.n11bc.payment_service.integration;

import com.n11bc.payment_service.entity.Payment;
import com.n11bc.payment_service.entity.PaymentItem;
import com.n11bc.payment_service.entity.PaymentStatus;
import com.n11bc.payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.config.import=optional:",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Testcontainers
class PaymentServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Autowired
    private PaymentRepository paymentRepository;

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
    @Transactional
    void savePayment_persistsPaymentWithItems() {
        Payment payment = Payment.builder()
                .orderId(100L)
                .orderNumber("ORD-ABC")
                .userId("user-1")
                .conversationId("conversation-1")
                .status(PaymentStatus.PENDING)
                .price(new BigDecimal("200.00"))
                .paidPrice(new BigDecimal("200.00"))
                .currency("TRY")
                .build();
        payment.addItem(PaymentItem.builder()
                .productId(10L)
                .productName("Organic Tea")
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .lineTotal(new BigDecimal("200.00"))
                .build());

        Payment saved = paymentRepository.save(payment);

        Payment found = paymentRepository.findByOrderId(100L).orElseThrow();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().getFirst().getProductName()).isEqualTo("Organic Tea");
    }
}
