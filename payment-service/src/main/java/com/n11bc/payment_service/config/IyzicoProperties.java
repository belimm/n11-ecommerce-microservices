package com.n11bc.payment_service.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.iyzico")
public record IyzicoProperties(
        String apiKey,
        String secretKey,
        @NotBlank String baseUrl,
        @NotBlank String locale,
        @NotBlank String currency,
        @NotBlank String paymentChannel,
        @NotBlank String paymentGroup,
        int installment,
        @Valid @NotNull SandboxCard sandboxCard,
        @Valid @NotNull Buyer buyer,
        @Valid @NotNull Address address
) {
    public boolean hasCredentials() {
        return apiKey != null && !apiKey.isBlank() && secretKey != null && !secretKey.isBlank();
    }

    public record SandboxCard(
            @NotBlank String cardHolderName,
            @NotBlank String cardNumber,
            @NotBlank String expireYear,
            @NotBlank String expireMonth,
            @NotBlank String cvc
    ) {
    }

    public record Buyer(
            @NotBlank String name,
            @NotBlank String surname,
            @NotBlank String identityNumber,
            @NotBlank String email,
            @NotBlank String gsmNumber,
            @NotBlank String ip,
            @NotBlank String registrationAddress,
            @NotBlank String city,
            @NotBlank String country,
            @NotBlank String zipCode
    ) {
    }

    public record Address(
            @NotBlank String contactName,
            @NotBlank String address,
            @NotBlank String city,
            @NotBlank String country,
            @NotBlank String zipCode
    ) {
    }
}
