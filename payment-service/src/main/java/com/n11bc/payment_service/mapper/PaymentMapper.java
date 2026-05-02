package com.n11bc.payment_service.mapper;

import com.n11bc.payment_service.dto.response.PaymentItemResponse;
import com.n11bc.payment_service.dto.response.PaymentResponse;
import com.n11bc.payment_service.entity.Payment;
import com.n11bc.payment_service.entity.PaymentItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);

    PaymentItemResponse toItemResponse(PaymentItem item);
}
