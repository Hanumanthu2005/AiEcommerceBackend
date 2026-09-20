package com.hanu.AiEcommerce.payment.dto;

import com.hanu.AiEcommerce.payment.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record PaymentStatusUpdateRequest(

        @NotNull(message = "Status is required")
        PaymentStatus status,

        @NotNull(message = "Transaction id is required")
        String transactionId
) {
}
