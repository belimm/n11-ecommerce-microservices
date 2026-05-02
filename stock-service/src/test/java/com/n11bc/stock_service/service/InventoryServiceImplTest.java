package com.n11bc.stock_service.service;

import com.n11bc.stock_service.dto.request.InventoryCreateRequest;
import com.n11bc.stock_service.dto.request.StockAdjustmentRequest;
import com.n11bc.stock_service.entity.Inventory;
import com.n11bc.stock_service.entity.StockReservation;
import com.n11bc.stock_service.entity.StockReservationStatus;
import com.n11bc.stock_service.event.OrderCreatedEvent;
import com.n11bc.stock_service.exception.InsufficientStockException;
import com.n11bc.stock_service.exception.InvalidStockAdjustmentException;
import com.n11bc.stock_service.mapper.InventoryMapper;
import com.n11bc.stock_service.repository.InventoryRepository;
import com.n11bc.stock_service.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockReservationRepository reservationRepository;

    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        InventoryMapper inventoryMapper = Mappers.getMapper(InventoryMapper.class);
        inventoryService = new InventoryServiceImpl(inventoryRepository, reservationRepository, inventoryMapper);
    }

    @Test
    @DisplayName("createInventory creates inventory with zero reserved quantity")
    void createInventory_success() {
        when(inventoryRepository.existsByProductId(10L)).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inventory = invocation.getArgument(0);
            inventory.setId(1L);
            return inventory;
        });

        var response = inventoryService.createInventory(new InventoryCreateRequest(10L, 20));

        assertThat(response.productId()).isEqualTo(10L);
        assertThat(response.availableQuantity()).isEqualTo(20);
        assertThat(response.reservedQuantity()).isZero();
    }

    @Test
    @DisplayName("reserveStock reserves available stock and creates reservations")
    void reserveStock_success() {
        Inventory inventory = inventory(10L, 20, 0);
        when(reservationRepository.findByOrderId(100L)).thenReturn(List.of());
        when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.of(inventory));

        var event = inventoryService.reserveStock(orderCreatedEvent());

        assertThat(inventory.getAvailableQuantity()).isEqualTo(18);
        assertThat(inventory.getReservedQuantity()).isEqualTo(2);
        assertThat(event.orderId()).isEqualTo(100L);
        assertThat(event.items()).hasSize(1);
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(reservationRepository).saveAll(captor.capture());
        StockReservation savedReservation = (StockReservation) captor.getValue().getFirst();
        assertThat(savedReservation.getStatus()).isEqualTo(StockReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("reserveStock rejects insufficient stock without writing reservations")
    void reserveStock_insufficientStock_throwsException() {
        Inventory inventory = inventory(10L, 1, 0);
        when(reservationRepository.findByOrderId(100L)).thenReturn(List.of());
        when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.reserveStock(orderCreatedEvent()))
                .isInstanceOf(InsufficientStockException.class);
        verify(reservationRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("reserveStock is idempotent for already reserved orders")
    void reserveStock_existingReservation_returnsExistingEvent() {
        StockReservation reservation = reservation(100L, 10L, StockReservationStatus.RESERVED);
        when(reservationRepository.findByOrderId(100L)).thenReturn(List.of(reservation));

        var event = inventoryService.reserveStock(orderCreatedEvent());

        assertThat(event.items()).hasSize(1);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("releaseStock releases reserved quantities once")
    void releaseStock_success() {
        Inventory inventory = inventory(10L, 18, 2);
        StockReservation reservation = reservation(100L, 10L, StockReservationStatus.RESERVED);
        when(reservationRepository.findByOrderId(100L)).thenReturn(List.of(reservation));
        when(inventoryRepository.findByProductId(10L)).thenReturn(Optional.of(inventory));

        var event = inventoryService.releaseStock(100L, "payment failed");

        assertThat(inventory.getAvailableQuantity()).isEqualTo(20);
        assertThat(inventory.getReservedQuantity()).isZero();
        assertThat(reservation.getStatus()).isEqualTo(StockReservationStatus.RELEASED);
        assertThat(event.reason()).isEqualTo("payment failed");
    }

    @Test
    @DisplayName("adjustStock rejects zero delta")
    void adjustStock_zeroDelta_throwsException() {
        assertThatThrownBy(() -> inventoryService.adjustStock(10L, new StockAdjustmentRequest(0)))
                .isInstanceOf(InvalidStockAdjustmentException.class)
                .hasMessageContaining("delta");
    }

    private Inventory inventory(Long productId, int availableQuantity, int reservedQuantity) {
        return Inventory.builder()
                .id(1L)
                .productId(productId)
                .availableQuantity(availableQuantity)
                .reservedQuantity(reservedQuantity)
                .build();
    }

    private StockReservation reservation(Long orderId, Long productId, StockReservationStatus status) {
        return StockReservation.builder()
                .id(1L)
                .orderId(orderId)
                .orderNumber("ORD-ABC")
                .userId("user-1")
                .productId(productId)
                .productName("Organic Tea")
                .quantity(2)
                .status(status)
                .build();
    }

    private OrderCreatedEvent orderCreatedEvent() {
        return new OrderCreatedEvent(
                100L,
                "ORD-ABC",
                "user-1",
                new BigDecimal("200.00"),
                null,
                List.of(new OrderCreatedEvent.OrderCreatedItem(
                        10L,
                        "Organic Tea",
                        2,
                        new BigDecimal("100.00"),
                        new BigDecimal("200.00")
                )),
                LocalDateTime.now()
        );
    }
}
