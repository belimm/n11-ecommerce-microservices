package com.n11bc.order_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PaymentCardRequest(
        @NotBlank @Size(max = 80) String cardHolderName,
        @NotBlank @Pattern(regexp = "\\d{15,19}") String cardNumber,
        @NotBlank @Pattern(regexp = "(0?[1-9]|1[0-2])") String expireMonth,
        @NotBlank @Pattern(regexp = "20\\d{2}") String expireYear,
        @NotBlank @Pattern(regexp = "\\d{3,4}") String cvc
) {
}
