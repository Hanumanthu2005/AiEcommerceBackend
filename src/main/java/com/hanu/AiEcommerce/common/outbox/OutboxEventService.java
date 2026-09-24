package com.hanu.AiEcommerce.common.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveEvent(
            String eventType,
            String aggregateType,
            String aggregateId,
            Object event
    ) {

        try {

            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .eventId(UUID.randomUUID().toString())
                    .aggregateType(aggregateType)
                    .payload(payload)
                    .eventType(eventType)
                    .published(false)
                    .build();

            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e ) {

            throw new IllegalStateException(
                    "Failed to serialize outbox event",
                    e
            );
        }
    }
}
