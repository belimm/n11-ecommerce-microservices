package com.n11bc.stock_service.service;

import com.n11bc.stock_service.dto.request.InventoryCreateRequest;
import com.n11bc.stock_service.dto.request.InventoryUpdateRequest;
import com.n11bc.stock_service.dto.request.StockAdjustmentRequest;
import com.n11bc.stock_service.dto.response.InventoryResponse;
import com.n11bc.stock_service.event.OrderCreatedEvent;
import com.n11bc.stock_service.event.StockReleasedEvent;
import com.n11bc.stock_service.event.StockReservedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Manages product inventories and SAGA stock reservations.
 */
public interface InventoryService {

    /**
     * Creates inventory for a product.
     *
     * @param request inventory creation request
     * @return created inventory
     */
    InventoryResponse createInventory(InventoryCreateRequest request);

    /**
     * Updates available quantity for a product.
     *
     * @param productId product id
     * @param request update request
     * @return updated inventory
     */
    InventoryResponse updateInventory(Long productId, InventoryUpdateRequest request);

    /**
     * Applies a positive or negative available-stock adjustment.
     *
     * @param productId product id
     * @param request adjustment request
     * @return updated inventory
     */
    InventoryResponse adjustStock(Long productId, StockAdjustmentRequest request);

    /**
     * Returns inventory for a product.
     *
     * @param productId product id
     * @return inventory
     */
    InventoryResponse getInventory(Long productId);

    /**
     * Returns all inventories with pagination.
     *
     * @param pageable pagination request
     * @return inventory page
     */
    Page<InventoryResponse> getInventories(Pageable pageable);

    /**
     * Reserves stock for an order-created event.
     *
     * @param event order event
     * @return stock reserved event
     */
    StockReservedEvent reserveStock(OrderCreatedEvent event);

    /**
     * Releases reserved stock for a failed payment.
     *
     * @param orderId order id
     * @param reason release reason
     * @return stock released event
     */
    StockReleasedEvent releaseStock(Long orderId, String reason);
}
