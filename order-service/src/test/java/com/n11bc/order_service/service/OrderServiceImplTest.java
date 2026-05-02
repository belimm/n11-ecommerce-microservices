package com.n11bc.order_service.service;

import com.n11bc.order_service.dto.request.CreateOrderRequest;
import com.n11bc.order_service.dto.request.UpdateOrderStatusRequest;
import com.n11bc.order_service.dto.response.CartItemSnapshotResponse;
import com.n11bc.order_service.dto.response.CartSnapshotResponse;
import com.n11bc.order_service.dto.response.UserAddressResponse;
import com.n11bc.order_service.entity.Order;
import com.n11bc.order_service.entity.OrderStatus;
import com.n11bc.order_service.event.OrderCreatedEvent;
import com.n11bc.order_service.exception.EmptyCartException;
import com.n11bc.order_service.exception.ForbiddenOrderAccessException;
import com.n11bc.order_service.exception.InvalidOrderStatusTransitionException;
import com.n11bc.order_service.mapper.OrderMapper;
import com.n11bc.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartClient cartClient;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserAddressClient userAddressClient;

    @Mock
    private OrderEventPublisher eventPublisher;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        OrderMapper mapper = Mappers.getMapper(OrderMapper.class);
        orderService = new OrderServiceImpl(orderRepository, mapper, cartClient, currentUserService, userAddressClient, eventPublisher);
    }

    @Test
    @DisplayName("createOrder creates pending order from cart snapshot and publishes event")
    void createOrder_success() {
        when(currentUserService.getCurrentUserId()).thenReturn("user-1");
        when(currentUserService.getBearerToken()).thenReturn("token");
        when(cartClient.getCurrentCart("token")).thenReturn(cartSnapshot("user-1"));
        when(userAddressClient.getAddress("user-1", "addr-1", "token")).thenReturn(addressResponse());
        when(orderRepository.existsByOrderNumber(any())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        var response = orderService.createOrder(createRequest());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.totalPrice()).isEqualByComparingTo("200.00");
        assertThat(response.items()).hasSize(1);
        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher).publishOrderCreated(eventCaptor.capture());
        assertThat(eventCaptor.getValue().userId()).isEqualTo("user-1");
    }

    @Test
    @DisplayName("createOrder rejects empty carts")
    void createOrder_emptyCart_throwsException() {
        when(currentUserService.getCurrentUserId()).thenReturn("user-1");
        when(currentUserService.getBearerToken()).thenReturn("token");
        when(cartClient.getCurrentCart("token")).thenReturn(new CartSnapshotResponse(
                1L, "user-1", "ACTIVE", List.of(), BigDecimal.ZERO, LocalDateTime.now(), null, null
        ));

        assertThatThrownBy(() -> orderService.createOrder(createRequest()))
                .isInstanceOf(EmptyCartException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrder rejects access for non-owner users")
    void getOrder_forbiddenUser_throwsException() {
        Order order = orderEntity(OrderStatus.PENDING);
        order.setUserId("owner");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn("other");

        assertThatThrownBy(() -> orderService.getOrder(1L))
                .isInstanceOf(ForbiddenOrderAccessException.class);
    }

    @Test
    @DisplayName("updateStatus validates lifecycle transitions")
    void updateStatus_invalidTransition_throwsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity(OrderStatus.DELIVERED)));

        assertThatThrownBy(() -> orderService.updateStatus(1L, new UpdateOrderStatusRequest(OrderStatus.CANCELLED)))
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
    }

    @Test
    @DisplayName("applySagaStatus cancels active orders and publishes cancellation")
    void applySagaStatus_cancel_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity(OrderStatus.PENDING)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.applySagaStatus(1L, OrderStatus.CANCELLED, "stock failed");

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(eventPublisher).publishOrderCancelled(any());
    }

    @Test
    @DisplayName("getCurrentUserOrders maps paginated results")
    void getCurrentUserOrders_success() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(currentUserService.getCurrentUserId()).thenReturn("user-1");
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1", pageRequest))
                .thenReturn(new PageImpl<>(List.of(orderEntity(OrderStatus.PENDING)), pageRequest, 1));

        Page<?> page = orderService.getCurrentUserOrders(pageRequest);

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    private CreateOrderRequest createRequest() {
        return new CreateOrderRequest("addr-1", "IYZICO", null);
    }

    private UserAddressResponse addressResponse() {
        return new UserAddressResponse(
                "addr-1",
                "Home",
                "Analytical Engine Street 1",
                "London",
                "United Kingdom",
                "NW1",
                true,
                null,
                null
        );
    }

    private CartSnapshotResponse cartSnapshot(String userId) {
        CartItemSnapshotResponse item = new CartItemSnapshotResponse(
                10L,
                100L,
                "Organic Tea",
                "https://cdn.example.com/tea.jpg",
                new BigDecimal("100.00"),
                2,
                new BigDecimal("200.00")
        );
        return new CartSnapshotResponse(1L, userId, "ACTIVE", List.of(item), new BigDecimal("200.00"), LocalDateTime.now(), null, null);
    }

    private Order orderEntity(OrderStatus status) {
        Order order = Order.builder()
                .id(1L)
                .orderNumber("ORD-12345678")
                .userId("user-1")
                .status(status)
                .totalPrice(new BigDecimal("200.00"))
                .paymentMethod("IYZICO")
                .shippingAddress(null)
                .build();
        return order;
    }
}
