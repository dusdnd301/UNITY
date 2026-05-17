package com.example.test.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public final class PaymentDtos {
    private PaymentDtos() {
    }

    public record TossPaymentRequest(
            @NotNull Long tableId,
            @NotEmpty @Valid List<OrderDtos.OrderItemRequest> items
    ) {
    }

    public record TossPaymentReadyResponse(
            Long orderId,
            String orderNumber,
            int amount,
            String orderName,
            String customerName,
            String clientKey,
            String successUrl,
            String failUrl
    ) {
    }

    public record TossSuccessRequest(
            @NotBlank String paymentKey,
            @NotBlank String orderId,
            @Min(1) int amount
    ) {
    }

    public record TossFailRequest(
            @NotBlank String orderId,
            String code,
            String message
    ) {
    }

    public record TossConfirmRequest(String paymentKey, String orderId, int amount) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TossConfirmResponse(
            String paymentKey,
            String orderId,
            String method,
            String status,
            String approvedAt,
            int totalAmount
    ) {
    }
}
