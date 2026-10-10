
package com.hanu.AiEcommerce.payment.dto;

import com.hanu.AiEcommerce.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        BigDecimal amount,
        PaymentStatus status,
        String transactionId,
        String razorpayOrderId,
        String razorpayKeyId,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
