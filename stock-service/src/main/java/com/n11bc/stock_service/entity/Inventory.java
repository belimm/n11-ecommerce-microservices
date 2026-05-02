package com.n11bc.stock_service.entity;

import com.n11bc.stock_service.exception.InsufficientStockException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inventories",
        indexes = @Index(name = "idx_inventories_product_id", columnList = "product_id", unique = true)
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Min(0)
    @Column(nullable = false)
    private int availableQuantity;

    @Min(0)
    @Column(nullable = false)
    private int reservedQuantity;

    @Version
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be greater than zero");
        }
        if (availableQuantity < quantity) {
            throw new InsufficientStockException(productId, quantity, availableQuantity);
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }

    public void release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Release quantity must be greater than zero");
        }
        if (reservedQuantity < quantity) {
            throw new InsufficientStockException(productId, quantity, reservedQuantity);
        }
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }

    public void setAvailableQuantitySafely(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Available quantity cannot be negative");
        }
        availableQuantity = quantity;
    }

    public void adjustAvailableQuantity(int delta) {
        int adjusted = availableQuantity + delta;
        if (adjusted < 0) {
            throw new IllegalArgumentException("Available quantity cannot become negative");
        }
        availableQuantity = adjusted;
    }
}
