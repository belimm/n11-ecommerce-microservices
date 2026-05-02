package com.n11bc.payment_service.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payments_order_id", columnNames = "order_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_number", nullable = false, length = 80)
    private String orderNumber;

    @Column(name = "user_id", nullable = false, length = 80)
    private String userId;

    @Column(name = "conversation_id", nullable = false, length = 120)
    private String conversationId;

    @Column(name = "iyzico_payment_id", length = 80)
    private String iyzicoPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "paid_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidPrice;

    @Column(nullable = false, length = 8)
    private String currency;

    @Column(name = "iyzico_status", length = 40)
    private String iyzicoStatus;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PaymentItem> items = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void addItem(PaymentItem item) {
        items.add(item);
        item.attachTo(this);
    }

    public void markSuccess(String paymentId, String iyzicoStatus) {
        this.iyzicoPaymentId = paymentId;
        this.iyzicoStatus = iyzicoStatus;
        this.status = PaymentStatus.SUCCESS;
        this.failureReason = null;
    }

    public void markFailed(String iyzicoStatus, String reason) {
        this.iyzicoStatus = iyzicoStatus;
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }
}
