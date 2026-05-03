package com.n11bc.payment_service.service;

import com.n11bc.payment_service.dto.response.PaymentResponse;
import com.n11bc.payment_service.event.OrderCancelledEvent;
import com.n11bc.payment_service.event.StockReservedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    /**
     * Processes a stock reservation event by creating an Iyzico payment attempt and publishing the SAGA result.
     *
     * @param event stock reservation event emitted by stock-service
     */
    void processStockReserved(StockReservedEvent event);

    /**
     * Compensates a successfully captured provider payment when the owning order is cancelled.
     * Failed or not-yet-captured payments are completed locally without calling Iyzico.
     *
     * @param event order cancellation event emitted by order-service
     */
    void processOrderCancelled(OrderCancelledEvent event);

    /**
     * Returns a payment by order id if the current user owns it or has admin role.
     *
     * @param orderId order identifier
     * @return payment response
     */
    PaymentResponse getPaymentByOrderId(Long orderId);

    /**
     * Returns payments for the authenticated user.
     *
     * @param pageable pagination settings
     * @return paginated payment responses
     */
    Page<PaymentResponse> getCurrentUserPayments(Pageable pageable);

    /**
     * Returns all payments for administrators.
     *
     * @param pageable pagination settings
     * @return paginated payment responses
     */
    Page<PaymentResponse> getAllPayments(Pageable pageable);
}
