package com.hanu.AiEcommerce.common.kafka;

import com.hanu.AiEcommerce.common.event.PaymentEvent;
import com.hanu.AiEcommerce.common.event.ProcessedEvent;
import com.hanu.AiEcommerce.common.event.ProcessedEventRepository;
import com.hanu.AiEcommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    @KafkaListener(
            topics = "payment.events",
            groupId = "ecommerce-order-group"
    )
    public void consume(PaymentEvent event) {

        System.out.println("Received PaymentEvent: " + event);

        if(processedEventRepository.existsByEventId(event.eventId())) {
            System.out.println(
                    "Skipping duplicate event : " + event.eventId()
            );
            return;
        }

        if("PAYMENT_SUCCEEDED".equals(event.eventType())) {

            orderService.confirmOrder(event.orderId());
        } else if("PAYMENT_FAILED".equals(event.eventType())) {

            orderService.cancelOrderAfterPaymentFailure(event.orderId());
        }

        processedEventRepository.save(
                ProcessedEvent.builder()
                        .eventId(event.eventId())
                        .build()
        );
    }
}
