package com.n11bc.payment_service.iyzico;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bc.payment_service.config.IyzicoProperties;
import com.n11bc.payment_service.entity.Payment;
import com.n11bc.payment_service.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IyzicoPaymentClient {

    private static final String PAYMENT_AUTH_PATH = "/payment/auth";

    private final RestClient iyzicoRestClient;
    private final ObjectMapper objectMapper;
    private final IyzicoProperties properties;
    private final IyzicoSignatureGenerator signatureGenerator;

    public IyzicoPaymentResult createPayment(Payment payment, StockReservedEvent event) {
        if (!properties.hasCredentials()) {
            throw new IyzicoPaymentException("Iyzico API credentials are not configured");
        }

        Map<String, Object> request = buildPaymentRequest(payment, event);
        String body = toJson(request);
        String authorization = signatureGenerator.generateAuthorization(
                properties.apiKey(),
                properties.secretKey(),
                PAYMENT_AUTH_PATH,
                body
        );

        String responseBody = iyzicoRestClient.post()
                .uri(PAYMENT_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", authorization)
                .body(body)
                .retrieve()
                .body(String.class);

        Map<String, Object> response = toMap(responseBody);
        if (response == null) {
            return IyzicoPaymentResult.failed("Iyzico returned an empty response");
        }
        return toPaymentResult(response);
    }

    private Map<String, Object> buildPaymentRequest(Payment payment, StockReservedEvent event) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("conversationId", payment.getConversationId());
        request.put("locale", properties.locale());
        request.put("paidPrice", payment.getPaidPrice());
        request.put("price", payment.getPrice());
        request.put("paymentGroup", properties.paymentGroup());
        request.put("currency", properties.currency());
        request.put("basketId", payment.getOrderNumber());
        request.put("paymentChannel", properties.paymentChannel());
        request.put("installment", properties.installment());
        request.put("paymentCard", paymentCard());
        request.put("buyer", buyer(event.userId()));
        request.put("shippingAddress", address());
        request.put("billingAddress", address());
        request.put("basketItems", basketItems(event.items()));
        return request;
    }

    private Map<String, Object> paymentCard() {
        IyzicoProperties.SandboxCard card = properties.sandboxCard();
        return Map.of(
                "cardHolderName", card.cardHolderName(),
                "cardNumber", card.cardNumber(),
                "expireYear", card.expireYear(),
                "expireMonth", card.expireMonth(),
                "cvc", card.cvc(),
                "registerCard", 0
        );
    }

    private Map<String, Object> buyer(String userId) {
        IyzicoProperties.Buyer buyer = properties.buyer();
        Map<String, Object> requestBuyer = new LinkedHashMap<>();
        requestBuyer.put("id", userId);
        requestBuyer.put("name", buyer.name());
        requestBuyer.put("surname", buyer.surname());
        requestBuyer.put("identityNumber", buyer.identityNumber());
        requestBuyer.put("email", buyer.email());
        requestBuyer.put("gsmNumber", buyer.gsmNumber());
        requestBuyer.put("registrationAddress", buyer.registrationAddress());
        requestBuyer.put("city", buyer.city());
        requestBuyer.put("country", buyer.country());
        requestBuyer.put("ip", buyer.ip());
        requestBuyer.put("zipCode", buyer.zipCode());
        return requestBuyer;
    }

    private Map<String, Object> address() {
        IyzicoProperties.Address address = properties.address();
        return Map.of(
                "address", address.address(),
                "contactName", address.contactName(),
                "city", address.city(),
                "country", address.country(),
                "zipCode", address.zipCode()
        );
    }

    private List<Map<String, Object>> basketItems(List<StockReservedEvent.StockReservedItem> items) {
        return items.stream()
                .map(item -> Map.<String, Object>of(
                        "id", "BI-" + item.productId(),
                        "price", item.lineTotal(),
                        "name", item.productName(),
                        "category1", "E-Commerce",
                        "itemType", "PHYSICAL"
                ))
                .toList();
    }

    private IyzicoPaymentResult toPaymentResult(Map<String, Object> response) {
        String status = stringValue(response.get("status"));
        String errorMessage = stringValue(response.get("errorMessage"));
        boolean successful = "success".equalsIgnoreCase(status);
        return new IyzicoPaymentResult(
                successful,
                status,
                stringValue(response.get("paymentId")),
                stringValue(response.get("conversationId")),
                decimalValue(response.get("price")),
                decimalValue(response.get("paidPrice")),
                stringValue(response.get("currency")),
                successful ? null : errorMessage
        );
    }

    private String toJson(Map<String, Object> request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException ex) {
            throw new IyzicoPaymentException("Unable to serialize Iyzico request", ex);
        }
    }

    private Map<String, Object> toMap(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(responseBody, new com.fasterxml.jackson.core.type.TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            throw new IyzicoPaymentException("Unable to parse Iyzico response", ex);
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private BigDecimal decimalValue(Object value) {
        if (value == null) {
            return null;
        }
        return new BigDecimal(String.valueOf(value));
    }
}
