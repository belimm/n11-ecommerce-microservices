package com.n11bc.payment_service.service;

import com.n11bc.payment_service.entity.Payment;
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

    private StockReservedEvent stockReservedEvent() {
        return new StockReservedEvent(
                100L,
                "ORD-ABC",
                "user-1",
                new BigDecimal("200.00"),
                List.of(new StockReservedEvent.StockReservedItem(10L, "Organic Tea", 2, new BigDecimal("100.00"), new BigDecimal("200.00"))),
                LocalDateTime.now()
        );
    }
}
