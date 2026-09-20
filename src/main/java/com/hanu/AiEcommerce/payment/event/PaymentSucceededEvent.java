package com.hanu.AiEcommerce.payment.event;

public record PaymentSucceededEvent (
        Long paymentId,
        Long orderId,
        String transactionId
) {
}
