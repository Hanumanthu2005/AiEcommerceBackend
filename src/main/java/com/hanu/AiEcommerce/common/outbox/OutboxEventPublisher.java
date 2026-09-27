package com.hanu.AiEcommerce.common.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanu.AiEcommerce.common.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private static final int MAX_RETRIES = 5;
    private static final long PROCESSING_LEASE_MINUTES = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxClaimService outboxClaimService;

    @Value("${app.kafka.topics.payment-events}")
    private String paymentEventsTopic;

    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {

        recoverStuckEvents();

        var events = outboxEventRepository.findRetryableEvents(
                LocalDateTime.now()
        );

        for (OutboxEvent event : events) {

            if (!outboxClaimService.claim(event.getId())) {
                continue;
            }

            try {

                PaymentEvent payload = objectMapper.readValue(
                        event.getPayload(),
                        PaymentEvent.class
                );

                kafkaTemplate.send(
                        paymentEventsTopic,
                        event.getAggregateId(),
                        payload
                ).get();

                event.setNextAttemptAt(null);
                event.setLastError(null);
                event.setStatus(OutboxStatus.PUBLISHED);
                event.setProcessingStartedAt(null);

                outboxEventRepository.save(event);

            } catch (Exception e) {

                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastAttemptAt(LocalDateTime.now());
                event.setLastError(e.getMessage());

                /*
                 * The event was being processed, but Kafka failed.
                 * Return it to PENDING so it can be retried later.
                 */
                event.setStatus(OutboxStatus.PENDING);
                event.setProcessingStartedAt(null);

                if (event.getRetryCount() >= MAX_RETRIES) {

                    event.setStatus(OutboxStatus.DEAD);
                    event.setNextAttemptAt(null);
                    event.setProcessingStartedAt(null);

                    System.err.println(
                            "Outbox event permanently failed: "
                                    + event.getEventId()
                                    + " | retryCount=" + event.getRetryCount()
                                    + " | error=" + e.getMessage()
                    );

                    outboxEventRepository.save(event);
                    continue;
                }

                long delaySeconds;

                switch (event.getRetryCount()) {
                    case 1 -> delaySeconds = 5;
                    case 2 -> delaySeconds = 15;
                    case 3 -> delaySeconds = 30;
                    default -> delaySeconds = 60;
                }

                event.setNextAttemptAt(
                        LocalDateTime.now().plusSeconds(delaySeconds)
                );

                outboxEventRepository.save(event);

                System.err.println(
                        "Failed to publish outbox event: "
                                + event.getEventId()
                                + " | retryCount=" + event.getRetryCount()
                                + " | nextAttemptAt=" + event.getNextAttemptAt()
                                + " | error=" + e.getMessage()
                );
            }
        }
    }

    private void recoverStuckEvents() {

        LocalDateTime cutoff = LocalDateTime.now()
                .minusMinutes(PROCESSING_LEASE_MINUTES);

        var stuckEvents =
                outboxEventRepository.findStuckProcessingEvents(cutoff);

        for (OutboxEvent event : stuckEvents) {

            event.setStatus(OutboxStatus.PENDING);
            event.setProcessingStartedAt(null);
            event.setNextAttemptAt(LocalDateTime.now());
            event.setLastError("Recovered from stuck PROCESSING state");

            outboxEventRepository.save(event);

            System.out.println(
                    "Recovered stuck outbox event: "
                            + event.getEventId()
            );
        }
    }
}