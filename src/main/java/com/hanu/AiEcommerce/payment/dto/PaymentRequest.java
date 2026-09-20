package com.hanu.AiEcommerce.payment.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(

        @NotNull(message = "Order id is required")
        Long orderId
) {
}
