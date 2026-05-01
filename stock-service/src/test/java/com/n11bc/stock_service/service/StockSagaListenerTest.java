package com.n11bc.stock_service.service;

import com.n11bc.stock_service.event.OrderCreatedEvent;
import com.n11bc.stock_service.event.PaymentFailedEvent;
import com.n11bc.stock_service.event.StockFailedEvent;
import com.n11bc.stock_service.event.StockReleasedEvent;
import com.n11bc.stock_service.event.StockReservedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockSagaListenerTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private StockEventPublisher eventPublisher;

    @InjectMocks
    private StockSagaListener listener;

    @Test
    @DisplayName("onOrderCreated publishes reserved event on successful reservation")
    void onOrderCreated_success() {
        OrderCreatedEvent orderCreated = orderCreatedEvent();
        StockReservedEvent reserved = new StockReservedEvent(orderCreated.orderId(), orderCreated.orderNumber(), orderCreated.userId(), orderCreated.totalPrice(), List.of(), LocalDateTime.now());
        when(inventoryService.reserveStock(orderCreated)).thenReturn(reserved);

        listener.onOrderCreated(orderCreated);

        verify(eventPublisher).publishStockReserved(reserved);
    }

    @Test
    @DisplayName("onOrderCreated publishes failed event when reservation throws")
    void onOrderCreated_failure() {
        OrderCreatedEvent orderCreated = orderCreatedEvent();
        when(inventoryService.reserveStock(orderCreated)).thenThrow(new IllegalStateException("stock failed"));

        listener.onOrderCreated(orderCreated);

        ArgumentCaptor<StockFailedEvent> captor = ArgumentCaptor.forClass(StockFailedEvent.class);
        verify(eventPublisher).publishStockFailed(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(100L);
        assertThat(captor.getValue().reason()).contains("stock failed");
    }

    @Test
    @DisplayName("onPaymentFailed releases stock and publishes released event")
    void onPaymentFailed_success() {
        PaymentFailedEvent paymentFailed = new PaymentFailedEvent(100L, "ORD-ABC", "user-1", "payment rejected", LocalDateTime.now());
        StockReleasedEvent released = new StockReleasedEvent(100L, "user-1", "payment rejected", LocalDateTime.now());
        when(inventoryService.releaseStock(100L, "payment rejected")).thenReturn(released);

        listener.onPaymentFailed(paymentFailed);

        verify(eventPublisher).publishStockReleased(released);
    }

    private OrderCreatedEvent orderCreatedEvent() {
        return new OrderCreatedEvent(
                100L,
                "ORD-ABC",
                "user-1",
                new BigDecimal("200.00"),
                List.of(new OrderCreatedEvent.OrderCreatedItem(10L, "Organic Tea", 2, new BigDecimal("100.00"), new BigDecimal("200.00"))),
                LocalDateTime.now()
        );
    }
}
