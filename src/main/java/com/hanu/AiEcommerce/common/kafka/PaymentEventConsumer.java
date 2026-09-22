package com.hanu.AiEcommerce.common.kafka;

import com.hanu.AiEcommerce.common.event.PaymentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
public class PaymentEventConsumer {

    @KafkaListener(
            topics = "payment.events",
            groupId = "ecommerce-order-group"
    )
    public void consume(PaymentEvent event) {
        System.out.println("Received PaymentEvent: " + event);
    }
}
