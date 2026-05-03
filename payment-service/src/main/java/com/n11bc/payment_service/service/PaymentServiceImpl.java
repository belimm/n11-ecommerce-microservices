package com.n11bc.payment_service.service;

import com.n11bc.payment_service.dto.response.PaymentResponse;
import com.n11bc.payment_service.entity.Payment;
import com.n11bc.payment_service.entity.PaymentItem;
import com.n11bc.payment_service.entity.PaymentStatus;
import com.n11bc.payment_service.event.OrderCancelledEvent;
import com.n11bc.payment_service.event.PaymentFailedEvent;
import com.n11bc.payment_service.event.PaymentSuccessEvent;
import com.n11bc.payment_service.event.StockReservedEvent;
import com.n11bc.payment_service.exception.PaymentNotFoundException;
import com.n11bc.payment_service.iyzico.IyzicoPaymentClient;
import com.n11bc.payment_service.iyzico.IyzicoPaymentException;
import com.n11bc.payment_service.iyzico.IyzicoPaymentResult;
import com.n11bc.payment_service.mapper.PaymentMapper;
import com.n11bc.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final IyzicoPaymentClient iyzicoPaymentClient;
    private final PaymentEventPublisher eventPublisher;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public void processStockReserved(StockReservedEvent event) {
        // A stock reservation can be redelivered; one payment decision is kept for each order.
        paymentRepository.findByOrderId(event.orderId()).ifPresentOrElse(
                existingPayment -> republishExistingResult(existingPayment, event),
                () -> processNewPayment(event)
        );
    }

    @Override
    @Transactional
    public void processOrderCancelled(OrderCancelledEvent event) {
        paymentRepository.findByOrderId(event.orderId()).ifPresentOrElse(
                payment -> compensateCancelledOrder(payment, event),
                () -> log.info("Order {} was cancelled before a payment record existed", event.orderId())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(orderId));
        validateOwnerOrAdmin(payment);
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getCurrentUserPayments(Pageable pageable) {
        return paymentRepository.findByUserId(currentUserId(), pageable).map(paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(paymentMapper::toResponse);
    }

    private void compensateCancelledOrder(Payment payment, OrderCancelledEvent event) {
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            log.info("Payment for order {} is already cancelled; ignoring duplicate order cancellation", event.orderId());
            return;
        }
        if (payment.getStatus() == PaymentStatus.FAILED) {
            log.info("Payment for order {} already failed; provider cancellation is not required", event.orderId());
            return;
        }
        if (payment.getStatus() == PaymentStatus.CANCEL_FAILED) {
            log.info("Payment cancellation for order {} already failed; ignoring duplicate order cancellation", event.orderId());
            return;
        }
        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.markCancelled("cancelled_without_provider_capture");
            paymentRepository.save(payment);
            log.info("Pending payment for order {} cancelled locally before provider capture", event.orderId());
            return;
        }

        cancelCapturedPayment(payment, event);
    }

    private void cancelCapturedPayment(Payment payment, OrderCancelledEvent event) {
        try {
            IyzicoPaymentResult result = iyzicoPaymentClient.cancelPayment(payment, event.reason());
            if (result.successful()) {
                payment.markCancelled(result.status());
                paymentRepository.save(payment);
                log.info("Iyzico payment {} cancelled for order {}", payment.getIyzicoPaymentId(), event.orderId());
                return;
            }
            markCancelFailed(payment, event, result.status(), failureReason(result.errorMessage()));
        } catch (IyzicoPaymentException ex) {
            markCancelFailed(payment, event, "failure", ex.getMessage());
        } catch (RuntimeException ex) {
            markCancelFailed(payment, event, "failure", "Unexpected payment cancellation provider error");
        }
    }

    private void markCancelFailed(Payment payment, OrderCancelledEvent event, String iyzicoStatus, String reason) {
        payment.markCancelFailed(iyzicoStatus, reason);
        paymentRepository.save(payment);
        log.warn("Payment cancellation failed for order {} paymentId={} reason={}",
                event.orderId(), payment.getIyzicoPaymentId(), reason);
    }

    private void processNewPayment(StockReservedEvent event) {
        Payment payment = buildPayment(event);
        try {
            // Provider responses are translated into domain events so the rest of the SAGA stays provider-agnostic.
            IyzicoPaymentResult result = iyzicoPaymentClient.createPayment(payment, event);
            if (result.successful()) {
                payment.markSuccess(result.paymentId(), result.status());
                Payment savedPayment = paymentRepository.save(payment);
                eventPublisher.publishPaymentSuccess(toSuccessEvent(savedPayment));
                log.info("Payment succeeded for order {}", event.orderId());
                return;
            }
            markFailedAndPublish(payment, event, result.status(), failureReason(result.errorMessage()));
        } catch (IyzicoPaymentException ex) {
            markFailedAndPublish(payment, event, "failure", ex.getMessage());
        } catch (RuntimeException ex) {
            markFailedAndPublish(payment, event, "failure", "Unexpected payment provider error");
        }
    }

    private Payment buildPayment(StockReservedEvent event) {
        BigDecimal totalPrice = normalizeTotalPrice(event);
        Payment payment = Payment.builder()
                .orderId(event.orderId())
                .orderNumber(event.orderNumber())
                .userId(event.userId())
                .conversationId("order-" + event.orderId() + "-" + UUID.randomUUID())
                .status(PaymentStatus.PENDING)
                .price(totalPrice)
                .paidPrice(totalPrice)
                .currency("TRY")
                .build();
        // Payment items mirror the reserved order snapshot for audit and provider reconciliation.
        event.items().forEach(item -> payment.addItem(PaymentItem.builder()
                .productId(item.productId())
                .productName(item.productName())
                .quantity(item.quantity())
                .unitPrice(item.unitPrice())
                .lineTotal(item.lineTotal())
                .build()));
        return payment;
    }

    private BigDecimal normalizeTotalPrice(StockReservedEvent event) {
        if (event.totalPrice() != null && event.totalPrice().compareTo(BigDecimal.ZERO) > 0) {
            return event.totalPrice();
        }
        return event.items().stream()
                .map(StockReservedEvent.StockReservedItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void markFailedAndPublish(Payment payment, StockReservedEvent event, String iyzicoStatus, String reason) {
        payment.markFailed(iyzicoStatus, reason);
        Payment savedPayment = paymentRepository.save(payment);
        eventPublisher.publishPaymentFailed(toFailedEvent(savedPayment, reason));
        log.warn("Payment failed for order {} reason={}", event.orderId(), reason);
    }

    private void republishExistingResult(Payment payment, StockReservedEvent event) {
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            eventPublisher.publishPaymentSuccess(toSuccessEvent(payment));
            log.info("Payment already succeeded for order {}; republished success event", event.orderId());
            return;
        }
        if (payment.getStatus() == PaymentStatus.FAILED) {
            eventPublisher.publishPaymentFailed(toFailedEvent(payment, payment.getFailureReason()));
            log.info("Payment already failed for order {}; republished failure event", event.orderId());
            return;
        }
        log.info("Payment is already pending for order {}; ignoring duplicate stock reservation event", event.orderId());
    }

    private PaymentSuccessEvent toSuccessEvent(Payment payment) {
        return new PaymentSuccessEvent(
                payment.getOrderId(),
                payment.getOrderNumber(),
                payment.getUserId(),
                payment.getIyzicoPaymentId(),
                payment.getConversationId(),
                payment.getPaidPrice(),
                payment.getCurrency(),
                LocalDateTime.now()
        );
    }

    private PaymentFailedEvent toFailedEvent(Payment payment, String reason) {
        return new PaymentFailedEvent(
                payment.getOrderId(),
                payment.getOrderNumber(),
                payment.getUserId(),
                failureReason(reason),
                LocalDateTime.now()
        );
    }

    private String failureReason(String reason) {
        return reason == null || reason.isBlank() ? "Payment failed" : reason;
    }

    private void validateOwnerOrAdmin(Payment payment) {
        if (!payment.getUserId().equals(currentUserId()) && !isAdmin()) {
            throw new AccessDeniedException("You cannot access this payment");
        }
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("Authentication is required");
        }
        String userId = jwt.getClaimAsString("userId");
        return userId == null || userId.isBlank() ? jwt.getSubject() : userId;
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
