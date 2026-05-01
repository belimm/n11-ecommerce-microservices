package com.n11bc.cart_service.service;

import com.n11bc.cart_service.entity.Cart;
import com.n11bc.cart_service.entity.CartItem;
import com.n11bc.cart_service.entity.CartStatus;
import com.n11bc.cart_service.event.AbandonedCartEvent;
import com.n11bc.cart_service.repository.CartRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbandonedCartSchedulerTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RabbitMqEventPublisher eventPublisher;

    @InjectMocks
    private AbandonedCartScheduler scheduler;

    @Test
    @DisplayName("publishAbandonedCarts marks old active carts as abandoned and publishes event")
    void publishAbandonedCarts_success() {
        ReflectionTestUtils.setField(scheduler, "abandonedAfterHours", 24L);
        Cart cart = Cart.builder()
                .id(1L)
                .userId("user-1")
                .status(CartStatus.ACTIVE)
                .lastActivityAt(LocalDateTime.now().minusHours(25))
                .build();
        cart.addItem(CartItem.builder()
                .productId(10L)
                .productNameSnapshot("Tea")
                .productImageUrlSnapshot("tea.jpg")
                .unitPriceSnapshot(new BigDecimal("50.00"))
                .quantity(2)
                .build());
        when(cartRepository.findByStatusAndLastActivityAtBefore(eq(CartStatus.ACTIVE), any())).thenReturn(List.of(cart));

        scheduler.publishAbandonedCarts();

        assertThat(cart.getStatus()).isEqualTo(CartStatus.ABANDONED);
        verify(cartRepository).save(cart);
        ArgumentCaptor<AbandonedCartEvent> captor = ArgumentCaptor.forClass(AbandonedCartEvent.class);
        verify(eventPublisher).publishAbandonedCart(captor.capture());
        assertThat(captor.getValue().totalPrice()).isEqualByComparingTo("100.00");
        assertThat(captor.getValue().items()).hasSize(1);
    }
}
