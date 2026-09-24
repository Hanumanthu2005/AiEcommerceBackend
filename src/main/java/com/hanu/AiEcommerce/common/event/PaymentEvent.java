package com.hanu.AiEcommerce.common.event;

public record PaymentEvent(

        String eventId,
        String eventType,
        Long paymentId,
        Long orderId,
        String transactionId
) {
}
