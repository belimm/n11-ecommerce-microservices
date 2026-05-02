package com.n11bc.order_service.service;

import com.n11bc.order_service.dto.request.CreateOrderRequest;
import com.n11bc.order_service.dto.request.UpdateOrderStatusRequest;
import com.n11bc.order_service.dto.response.OrderResponse;
import com.n11bc.order_service.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Manages order creation, lookup, lifecycle updates, and SAGA compensation.
 */
public interface OrderService {

    /**
     * Creates a pending order from the authenticated user's current cart snapshot.
     *
     * @param request checkout details
     * @return created order
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Returns an order visible to the current user.
     *
     * @param orderId order id
     * @return order details
     */
    OrderResponse getOrder(Long orderId);

    /**
     * Lists orders owned by the current user.
     *
     * @param pageable pagination request
     * @return current user's orders
     */
    Page<OrderResponse> getCurrentUserOrders(Pageable pageable);

    /**
     * Lists all orders for administrator workflows.
     *
     * @param pageable pagination request
     * @return all orders
     */
    Page<OrderResponse> getAllOrders(Pageable pageable);

    /**
     * Updates an order status after validating lifecycle transitions.
     *
     * @param orderId order id
     * @param request target status
     * @return updated order
     */
    OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest request);

    /**
     * Cancels an order after validating the caller can access it.
     *
     * @param orderId order id
     * @param reason cancellation reason
     * @return cancelled order
     */
    OrderResponse cancelOrder(Long orderId, String reason);

    /**
     * Applies a status change from an internal SAGA event.
     *
     * @param orderId order id
     * @param targetStatus target status
     * @param reason event reason
     */
    void applySagaStatus(Long orderId, OrderStatus targetStatus, String reason);
}
