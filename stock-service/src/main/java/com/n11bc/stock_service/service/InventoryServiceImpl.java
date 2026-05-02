package com.n11bc.stock_service.service;

import com.n11bc.stock_service.dto.request.InventoryCreateRequest;
import com.n11bc.stock_service.dto.request.InventoryUpdateRequest;
import com.n11bc.stock_service.dto.request.StockAdjustmentRequest;
import com.n11bc.stock_service.dto.response.InventoryResponse;
import com.n11bc.stock_service.entity.Inventory;
import com.n11bc.stock_service.entity.StockReservation;
import com.n11bc.stock_service.entity.StockReservationStatus;
import com.n11bc.stock_service.event.OrderCreatedEvent;
import com.n11bc.stock_service.event.StockReleasedEvent;
import com.n11bc.stock_service.event.StockReservedEvent;
import com.n11bc.stock_service.exception.InventoryAlreadyExistsException;
import com.n11bc.stock_service.exception.InventoryNotFoundException;
import com.n11bc.stock_service.exception.InvalidStockAdjustmentException;
import com.n11bc.stock_service.mapper.InventoryMapper;
import com.n11bc.stock_service.repository.InventoryRepository;
import com.n11bc.stock_service.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository reservationRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public InventoryResponse createInventory(InventoryCreateRequest request) {
        if (inventoryRepository.existsByProductId(request.productId())) {
            throw new InventoryAlreadyExistsException(request.productId());
        }
        Inventory inventory = Inventory.builder()
                .productId(request.productId())
                .availableQuantity(request.availableQuantity())
                .reservedQuantity(0)
                .build();
        return inventoryMapper.toResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public InventoryResponse updateInventory(Long productId, InventoryUpdateRequest request) {
        Inventory inventory = getExistingInventory(productId);
        inventory.setAvailableQuantitySafely(request.availableQuantity());
        return inventoryMapper.toResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public InventoryResponse adjustStock(Long productId, StockAdjustmentRequest request) {
        if (request.delta() == null || request.delta() == 0) {
            throw new InvalidStockAdjustmentException("Stock adjustment delta cannot be zero");
        }
        Inventory inventory = getExistingInventory(productId);
        inventory.adjustAvailableQuantity(request.delta());
        return inventoryMapper.toResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(Long productId) {
        return inventoryMapper.toResponse(getExistingInventory(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryResponse> getInventories(Pageable pageable) {
        return inventoryRepository.findAll(pageable).map(inventoryMapper::toResponse);
    }

    @Override
    @Transactional
    public StockReservedEvent reserveStock(OrderCreatedEvent event) {
        List<StockReservation> existingReservations = reservationRepository.findByOrderId(event.orderId());
        if (!existingReservations.isEmpty()) {
            boolean hasReservedReservation = existingReservations.stream().anyMatch(StockReservation::isReserved);
            if (!hasReservedReservation) {
                throw new InvalidStockAdjustmentException("Stock reservation already released for order: " + event.orderId());
            }
            log.info("Order {} already has stock reservations; returning existing reserved event", event.orderId());
            return toStockReservedEvent(event, existingReservations);
        }

        List<StockReservation> reservations = event.items().stream()
                .map(item -> reserveItem(event, item))
                .toList();
        reservationRepository.saveAll(reservations);
        log.info("Reserved stock for order {}", event.orderId());
        return toStockReservedEvent(event, reservations);
    }

    @Override
    @Transactional
    public StockReleasedEvent releaseStock(Long orderId, String reason) {
        List<StockReservation> reservations = reservationRepository.findByOrderId(orderId);
        if (reservations.isEmpty()) {
            log.info("No stock reservations found for order {}; stock release is idempotently ignored", orderId);
            return new StockReleasedEvent(orderId, null, reasonOrDefault(reason), LocalDateTime.now());
        }

        reservations.stream()
                .filter(StockReservation::isReserved)
                .forEach(this::releaseReservation);
        reservationRepository.saveAll(reservations);
        StockReservation first = reservations.getFirst();
        log.info("Released stock reservations for order {}", orderId);
        return new StockReleasedEvent(orderId, first.getUserId(), reasonOrDefault(reason), LocalDateTime.now());
    }

    private StockReservation reserveItem(OrderCreatedEvent event, OrderCreatedEvent.OrderCreatedItem item) {
        Inventory inventory = getExistingInventory(item.productId());
        inventory.reserve(item.quantity());
        inventoryRepository.save(inventory);
        return StockReservation.builder()
                .orderId(event.orderId())
                .orderNumber(event.orderNumber())
                .userId(event.userId())
                .productId(item.productId())
                .productName(item.productName())
                .quantity(item.quantity())
                .status(StockReservationStatus.RESERVED)
                .build();
    }

    private void releaseReservation(StockReservation reservation) {
        Inventory inventory = getExistingInventory(reservation.getProductId());
        inventory.release(reservation.getQuantity());
        inventoryRepository.save(inventory);
        reservation.markReleased();
    }

    private Inventory getExistingInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));
    }

    private StockReservedEvent toStockReservedEvent(OrderCreatedEvent event, List<StockReservation> reservations) {
        Map<Long, OrderCreatedEvent.OrderCreatedItem> originalItems = event.items().stream()
                .collect(Collectors.toMap(OrderCreatedEvent.OrderCreatedItem::productId, Function.identity(), (first, ignored) -> first));
        List<StockReservedEvent.StockReservedItem> items = reservations.stream()
                .map(reservation -> toStockReservedItem(reservation, originalItems.get(reservation.getProductId())))
                .toList();
        return new StockReservedEvent(event.orderId(), event.orderNumber(), event.userId(), event.totalPrice(), toStockReservedPaymentCard(event.paymentCard()), items, LocalDateTime.now());
    }

    private StockReservedEvent.PaymentCard toStockReservedPaymentCard(OrderCreatedEvent.PaymentCard paymentCard) {
        if (paymentCard == null) {
            return null;
        }
        return new StockReservedEvent.PaymentCard(
                paymentCard.cardHolderName(),
                paymentCard.cardNumber(),
                paymentCard.expireMonth(),
                paymentCard.expireYear(),
                paymentCard.cvc()
        );
    }

    private StockReservedEvent.StockReservedItem toStockReservedItem(StockReservation reservation, OrderCreatedEvent.OrderCreatedItem originalItem) {
        BigDecimal unitPrice = originalItem == null ? BigDecimal.ZERO : originalItem.unitPrice();
        BigDecimal lineTotal = originalItem == null ? unitPrice.multiply(BigDecimal.valueOf(reservation.getQuantity())) : originalItem.lineTotal();
        return new StockReservedEvent.StockReservedItem(
                reservation.getProductId(),
                reservation.getProductName(),
                reservation.getQuantity(),
                unitPrice,
                lineTotal);
    }

    private String reasonOrDefault(String reason) {
        return reason == null || reason.isBlank() ? "Payment failed; stock reservation released" : reason;
    }
}
