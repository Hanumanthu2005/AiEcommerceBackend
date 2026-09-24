package com.hanu.AiEcommerce.common.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {

        var events = outboxEventRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();

        for(OutboxEvent event : events) {

            try {

                Object payload = objectMapper.readValue(
                        event.getPayload(),
                        Object.class
                );

                kafkaTemplate.send(
                        "payment.events",
                        event.getAggregateId(),
                        payload
                ).get();

                event.setPublished(true);

                outboxEventRepository.save(event);
            } catch (Exception e) {

                System.err.println(
                        "Failed to publish outbox event: "
                                + event.getEventId()
                                + " - "
                                + e.getMessage()
                );

                e.printStackTrace();
            }
        }
    }
}
