package com.n11bc.order_service.service;

import com.n11bc.order_service.entity.OrderStatus;
import com.n11bc.order_service.event.StockFailedEvent;
import com.n11bc.order_service.event.StockReleasedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderSagaListenerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderSagaListener listener;

    @Test
    @DisplayName("onStockFailed cancels the order")
    void onStockFailed_cancelsOrder() {
        StockFailedEvent event = new StockFailedEvent(1L, "user-1", "insufficient stock", LocalDateTime.now());

        listener.onStockFailed(event);

        verify(orderService).applySagaStatus(1L, OrderStatus.CANCELLED, "insufficient stock");
    }

    @Test
    @DisplayName("onStockReleased cancels the order")
    void onStockReleased_cancelsOrder() {
        StockReleasedEvent event = new StockReleasedEvent(1L, "user-1", null, LocalDateTime.now());

        listener.onStockReleased(event);

        verify(orderService).applySagaStatus(1L, OrderStatus.CANCELLED, "Stock released after payment failure");
    }
}
