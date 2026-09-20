package com.hanu.AiEcommerce.payment.event;

public record PaymentFailedEvent(

        Long paymentId,
        Long orderId,
        String transactionId
) {
}
