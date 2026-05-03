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
import org.springframework.security.oauth2.jwt.Jwt;
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
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.main.allow-bean-definition-overriding=true",
        "app.jwt.secret=test-jwt-secret-key-that-is-at-least-32-bytes",
        "app.rabbitmq.stock-exchange=stock.exchange",
        "app.rabbitmq.payment-exchange=payment.exchange",
        "app.rabbitmq.order-exchange=order.exchange",
        "app.rabbitmq.stock-reserved-queue=stock.reserved.queue",
        "app.rabbitmq.payment-success-queue=payment.success.queue",
        "app.rabbitmq.payment-failed-queue=payment.failed.queue",
        "app.rabbitmq.order-cancelled-queue=payment.order-cancelled.queue",
        "app.rabbitmq.stock-reserved-routing-key=stock.reserved",
        "app.rabbitmq.payment-success-routing-key=payment.success",
        "app.rabbitmq.payment-failed-routing-key=payment.failed",
        "app.rabbitmq.order-cancelled-routing-key=order.cancelled",
        "app.iyzico.base-url=https://sandbox-api.iyzipay.com",
        "app.iyzico.locale=tr",
        "app.iyzico.currency=TRY",
        "app.iyzico.payment-channel=WEB",
        "app.iyzico.payment-group=PRODUCT",
        "app.iyzico.installment=1",
        "app.iyzico.sandbox-card.card-holder-name=John Doe",
        "app.iyzico.sandbox-card.card-number=5526080000000006",
        "app.iyzico.sandbox-card.expire-year=2030",
        "app.iyzico.sandbox-card.expire-month=12",
        "app.iyzico.sandbox-card.cvc=123",
        "app.iyzico.buyer.name=John",
        "app.iyzico.buyer.surname=Doe",
        "app.iyzico.buyer.identity-number=11111111111",
        "app.iyzico.buyer.email=john@example.com",
        "app.iyzico.buyer.gsm-number=+905551112233",
        "app.iyzico.buyer.ip=127.0.0.1",
        "app.iyzico.buyer.registration-address=Test Street",
        "app.iyzico.buyer.city=Istanbul",
        "app.iyzico.buyer.country=Turkey",
        "app.iyzico.buyer.zip-code=34000",
        "app.iyzico.address.contact-name=John Doe",
        "app.iyzico.address.address=Test Street",
        "app.iyzico.address.city=Istanbul",
        "app.iyzico.address.country=Turkey",
        "app.iyzico.address.zip-code=34000"
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
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test-user")
                    .build();
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
