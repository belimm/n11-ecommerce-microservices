package com.n11bc.payment_service.service;

import com.n11bc.payment_service.entity.Payment;
import com.n11bc.payment_service.entity.PaymentStatus;
import com.n11bc.payment_service.event.OrderCancelledEvent;
import com.n11bc.payment_service.event.StockReservedEvent;
import com.n11bc.payment_service.iyzico.IyzicoPaymentClient;
import com.n11bc.payment_service.iyzico.IyzicoPaymentResult;
import com.n11bc.payment_service.mapper.PaymentMapper;
import com.n11bc.payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private IyzicoPaymentClient iyzicoPaymentClient;

    @Mock
    private PaymentEventPublisher eventPublisher;

    @Mock
    private PaymentMapper paymentMapper;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentRepository, iyzicoPaymentClient, eventPublisher, paymentMapper);
    }

    @Test
    void processStockReserved_whenProviderSucceeds_marksPaymentSuccessAndPublishesEvent() {
        StockReservedEvent event = stockReservedEvent();
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(iyzicoPaymentClient.createPayment(any(Payment.class), any(StockReservedEvent.class)))
                .thenReturn(new IyzicoPaymentResult(true, "success", "24511629", "conversation", new BigDecimal("200.00"), new BigDecimal("200.00"), "TRY", null));

        paymentService.processStockReserved(event);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getIyzicoPaymentId()).isEqualTo("24511629");
        verify(eventPublisher).publishPaymentSuccess(any());
        verify(eventPublisher, never()).publishPaymentFailed(any());
    }

    @Test
    void processStockReserved_whenProviderFails_marksPaymentFailedAndPublishesEvent() {
        StockReservedEvent event = stockReservedEvent();
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(iyzicoPaymentClient.createPayment(any(Payment.class), any(StockReservedEvent.class)))
                .thenReturn(IyzicoPaymentResult.failed("Insufficient funds"));

        paymentService.processStockReserved(event);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getFailureReason()).isEqualTo("Insufficient funds");
        verify(eventPublisher).publishPaymentFailed(any());
        verify(eventPublisher, never()).publishPaymentSuccess(any());
    }

    @Test
    void processOrderCancelled_whenPaymentSucceeded_cancelsProviderPayment() {
        Payment payment = successfulPayment();
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(iyzicoPaymentClient.cancelPayment(any(Payment.class), eq("Cancelled by customer")))
                .thenReturn(new IyzicoPaymentResult(true, "success", "24511629", "conversation", null, null, "TRY", null));

        paymentService.processOrderCancelled(orderCancelledEvent());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(paymentCaptor.getValue().getFailureReason()).isNull();
        verify(iyzicoPaymentClient).cancelPayment(payment, "Cancelled by customer");
    }

    @Test
    void processOrderCancelled_whenProviderCancellationFails_marksCancelFailed() {
        Payment payment = successfulPayment();
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(iyzicoPaymentClient.cancelPayment(any(Payment.class), eq("Cancelled by customer")))
                .thenReturn(IyzicoPaymentResult.failed("Payment cannot be cancelled"));

        paymentService.processOrderCancelled(orderCancelledEvent());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.CANCEL_FAILED);
        assertThat(paymentCaptor.getValue().getFailureReason()).isEqualTo("Payment cannot be cancelled");
    }

    @Test
    void processOrderCancelled_whenPaymentAlreadyFailed_doesNotCallProvider() {
        Payment payment = Payment.builder()
                .orderId(100L)
                .orderNumber("ORD-ABC")
                .userId("user-1")
                .conversationId("conversation")
                .status(PaymentStatus.FAILED)
                .price(new BigDecimal("200.00"))
                .paidPrice(new BigDecimal("200.00"))
                .currency("TRY")
                .build();
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(payment));

        paymentService.processOrderCancelled(orderCancelledEvent());

        verify(iyzicoPaymentClient, never()).cancelPayment(any(), any());
        verify(paymentRepository, never()).save(any());
    }

    private Payment successfulPayment() {
        return Payment.builder()
                .orderId(100L)
                .orderNumber("ORD-ABC")
                .userId("user-1")
                .conversationId("conversation")
                .iyzicoPaymentId("24511629")
                .status(PaymentStatus.SUCCESS)
                .price(new BigDecimal("200.00"))
                .paidPrice(new BigDecimal("200.00"))
                .currency("TRY")
                .build();
    }

    private OrderCancelledEvent orderCancelledEvent() {
        return new OrderCancelledEvent(100L, "ORD-ABC", "user-1", "Cancelled by customer", LocalDateTime.now());
    }

    private StockReservedEvent stockReservedEvent() {
        return new StockReservedEvent(
                100L,
                "ORD-ABC",
                "user-1",
                new BigDecimal("200.00"),
                null,
                List.of(new StockReservedEvent.StockReservedItem(10L, "Organic Tea", 2, new BigDecimal("100.00"), new BigDecimal("200.00"))),
                LocalDateTime.now()
        );
    }
}
