package com.n11bc.cart_service.service;

import com.n11bc.cart_service.entity.Cart;
import com.n11bc.cart_service.entity.CartItem;
import com.n11bc.cart_service.entity.CartStatus;
import com.n11bc.cart_service.event.AbandonedCartEvent;
import com.n11bc.cart_service.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AbandonedCartScheduler {

    private final CartRepository cartRepository;
    private final RabbitMqEventPublisher eventPublisher;

    @Value("${app.cart.abandoned-after-hours}")
    private long abandonedAfterHours;

    @Scheduled(cron = "${app.cart.abandoned-scan-cron}")
    @Transactional
    public void publishAbandonedCarts() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(abandonedAfterHours);
        List<Cart> carts = cartRepository.findByStatusAndLastActivityAtBefore(CartStatus.ACTIVE, threshold);
        carts.forEach(this::markAndPublish);
    }

    private void markAndPublish(Cart cart) {
        cart.setStatus(CartStatus.ABANDONED);
        AbandonedCartEvent event = toEvent(cart);
        cartRepository.save(cart);
        eventPublisher.publishAbandonedCart(event);
        log.info("Cart {} marked as abandoned", cart.getId());
    }

    private AbandonedCartEvent toEvent(Cart cart) {
        List<AbandonedCartEvent.AbandonedCartItem> items = cart.getItems().stream()
                .map(item -> new AbandonedCartEvent.AbandonedCartItem(
                        item.getProductId(),
                        item.getProductNameSnapshot(),
                        item.getQuantity(),
                        item.getUnitPriceSnapshot(),
                        item.lineTotal()))
                .toList();
        BigDecimal totalPrice = cart.getItems().stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new AbandonedCartEvent(cart.getId(), cart.getUserId(), totalPrice, items, LocalDateTime.now());
    }
}
