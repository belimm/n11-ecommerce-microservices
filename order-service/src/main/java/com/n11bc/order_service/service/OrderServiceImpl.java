package com.n11bc.order_service.service;

import com.n11bc.order_service.dto.request.CreateOrderRequest;
import com.n11bc.order_service.dto.request.UpdateOrderStatusRequest;
import com.n11bc.order_service.dto.response.CartSnapshotResponse;
import com.n11bc.order_service.dto.response.OrderResponse;
import com.n11bc.order_service.dto.response.UserAddressResponse;
import com.n11bc.order_service.entity.Order;
import com.n11bc.order_service.entity.OrderItem;
import com.n11bc.order_service.entity.OrderStatus;
import com.n11bc.order_service.event.OrderCancelledEvent;
import com.n11bc.order_service.event.OrderCreatedEvent;
import com.n11bc.order_service.exception.EmptyCartException;
import com.n11bc.order_service.exception.ForbiddenOrderAccessException;
import com.n11bc.order_service.exception.InvalidOrderStatusTransitionException;
import com.n11bc.order_service.exception.OrderNotFoundException;
import com.n11bc.order_service.mapper.OrderMapper;
import com.n11bc.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartClient cartClient;
    private final CurrentUserService currentUserService;
    private final UserAddressClient userAddressClient;
    private final OrderEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String userId = currentUserService.getCurrentUserId();
        String bearerToken = currentUserService.getBearerToken();
        CartSnapshotResponse cart = cartClient.getCurrentCart(bearerToken);
        validateCart(userId, cart);
        UserAddressResponse address = userAddressClient.getAddress(userId, request.addressId(), bearerToken);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalPrice(orderMapper.calculateTotal(cart.items()))
                .paymentMethod(request.normalizedPaymentMethod())
                .statusReason("Order created; waiting for stock reservation and payment")
                .shippingAddress(orderMapper.toShippingAddress(address))
                .build();
        List<OrderItem> items = orderMapper.toOrderItems(cart.items());
        order.replaceItems(items);

        Order savedOrder = orderRepository.save(order);
        eventPublisher.publishOrderCreated(toOrderCreatedEvent(savedOrder, request));
        log.info("Order {} created for user {}", savedOrder.getId(), userId);
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = getExistingOrder(orderId);
        assertVisible(order);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getCurrentUserOrders(Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(currentUserService.getCurrentUserId(), pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = getExistingOrder(orderId);
        transition(order, request.status());
        order.setStatusReason("Order status updated to " + request.status());
        Order savedOrder = orderRepository.save(order);
        if (savedOrder.getStatus() == OrderStatus.CANCELLED) {
            eventPublisher.publishOrderCancelled(toOrderCancelledEvent(savedOrder, "Order status updated to CANCELLED"));
        }
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, String reason) {
        Order order = getExistingOrder(orderId);
        assertVisible(order);
        transition(order, OrderStatus.CANCELLED);
        order.setStatusReason(reasonOrDefault(reason, "Cancelled by customer"));
        Order savedOrder = orderRepository.save(order);
        eventPublisher.publishOrderCancelled(toOrderCancelledEvent(savedOrder, reason));
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public void applySagaStatus(Long orderId, OrderStatus targetStatus, String reason) {
        Order order = getExistingOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            log.info("Saga status update ignored for final order {} with status {}", orderId, order.getStatus());
            return;
        }
        transition(order, targetStatus);
        order.setStatusReason(reasonOrDefault(reason, "Saga status updated to " + targetStatus));
        Order savedOrder = orderRepository.save(order);
        if (targetStatus == OrderStatus.CANCELLED) {
            eventPublisher.publishOrderCancelled(toOrderCancelledEvent(savedOrder, reason));
        }
    }

    private String reasonOrDefault(String reason, String fallback) {
        return reason == null || reason.isBlank() ? fallback : reason;
    }

    private void validateCart(String userId, CartSnapshotResponse cart) {
        if (!userId.equals(cart.userId())) {
            throw new ForbiddenOrderAccessException(cart.id());
        }
        if (cart.items() == null || cart.items().isEmpty()) {
            throw new EmptyCartException();
        }
    }

    private void transition(Order order, OrderStatus targetStatus) {
        if (!order.getStatus().canTransitionTo(targetStatus)) {
            throw new InvalidOrderStatusTransitionException(order.getStatus(), targetStatus);
        }
        order.setStatus(targetStatus);
    }

    private Order getExistingOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private void assertVisible(Order order) {
        if (!currentUserService.isAdmin() && !currentUserService.getCurrentUserId().equals(order.getUserId())) {
            throw new ForbiddenOrderAccessException(order.getId());
        }
    }

    private String generateOrderNumber() {
        String orderNumber;
        do {
            orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (orderRepository.existsByOrderNumber(orderNumber));
        return orderNumber;
    }

    private OrderCreatedEvent toOrderCreatedEvent(Order order, CreateOrderRequest request) {
        List<OrderCreatedEvent.OrderCreatedItem> items = order.getItems().stream()
                .map(item -> new OrderCreatedEvent.OrderCreatedItem(
                        item.getProductId(),
                        item.getProductNameSnapshot(),
                        item.getQuantity(),
                        item.getUnitPriceSnapshot(),
                        item.getLineTotal()))
                .toList();
        return new OrderCreatedEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getTotalPrice(),
                toPaymentCard(request),
                items,
                LocalDateTime.now()
        );
    }

    private OrderCreatedEvent.PaymentCard toPaymentCard(CreateOrderRequest request) {
        if (request.paymentCard() == null) {
            return null;
        }
        return new OrderCreatedEvent.PaymentCard(
                request.paymentCard().cardHolderName(),
                request.paymentCard().cardNumber(),
                request.paymentCard().expireMonth(),
                request.paymentCard().expireYear(),
                request.paymentCard().cvc()
        );
    }

    private OrderCancelledEvent toOrderCancelledEvent(Order order, String reason) {
        return new OrderCancelledEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                reason,
                LocalDateTime.now()
        );
    }
}
